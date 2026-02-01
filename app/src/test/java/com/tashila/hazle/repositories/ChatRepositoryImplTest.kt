package com.tashila.hazle.repositories

import app.cash.turbine.test
import com.tashila.hazle.api.ApiServiceImpl
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.messages.MessageEntity
import com.tashila.hazle.db.threads.ThreadDao
import com.tashila.hazle.db.threads.ThreadEntity
import com.tashila.hazle.features.chat.ChatRepositoryImpl
import com.tashila.hazle.features.chat.Message
import com.tashila.hazle.features.settings.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ChatRepositoryImplTest {

    private lateinit var repository: ChatRepositoryImpl
    private val messageDao: MessageDao = mockk(relaxUnitFun = true)
    private val threadDao: ThreadDao = mockk(relaxUnitFun = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val jsonDecoder = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val testClock = object : Clock {
        private var now = Instant.parse("2023-01-01T00:00:00Z")
        override fun now(): Instant = now
        fun advance(duration: Duration) {
            now += duration
        }
    }
    private val responseQueue =
        ConcurrentLinkedQueue<suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData>()

    @Before
    fun setUp() {
        responseQueue.clear() // Clear queue for test isolation

        // Create a MockEngine that pulls from our queue
        val mockEngine = MockEngine { request ->
            responseQueue.poll()?.invoke(this, request)
                ?: respondError(HttpStatusCode.NotFound, "No response in queue for ${request.url}")
        }

        // Create a REAL HttpClient that uses our MockEngine
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(jsonDecoder)
            }
        }

        // ApiServiceImpl that uses mock settingsRepo and test HttpClient
        coEvery { settingsRepository.getBaseUrl() } returns "http://localhost/"
        val realApiService = ApiServiceImpl(settingsRepository, httpClient)

        // The repository uses the ApiService instance
        repository = ChatRepositoryImpl(realApiService, messageDao, threadDao, jsonDecoder, testClock)
    }

    @Test
    fun `sendUserMessage stores user message, calls API, stores AI response, and returns AI text`() =
        runTest {
            // Given
            val localThreadId = 1L
            val userMessageText = "Hello AI"
            val aiResponseText = "Hello user"
            val aiThreadId = "ai-thread-123"
            val userMessageTime = testClock.now()
            val aiMessageTime = userMessageTime + 5.seconds

            val mockThread = ThreadEntity(
                id = localThreadId,
                name = "Test Thread",
                isPinned = false,
                aiThreadId = null,
                lastMessageText = "",
                lastMessageTime = userMessageTime
            )
            coEvery { threadDao.getThreadById(localThreadId) } returns mockThread

            // Instead of mocking HttpResponse, queue up a response for MockEngine
            responseQueue.add {
                respond(
                    content = """
                        {
                            "id": 2,
                            "text": "$aiResponseText",
                            "isFromMe": false,
                            "aiThreadId": "$aiThreadId",
                            "timestamp": "$aiMessageTime"
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            // When
            val result = repository.sendUserMessage(localThreadId, null, userMessageText)

            // Then
            Assert.assertEquals(aiResponseText, result)

            coVerify(exactly = 1) { messageDao.insertMessage(match { it.text == userMessageText && it.isFromMe }) }
            coVerify(exactly = 1) { messageDao.insertMessage(match { it.text == aiResponseText && !it.isFromMe }) }
            coVerify(exactly = 2) { threadDao.updateThread(any()) }
        }

    @Test
    fun `storeAiMessage inserts message and updates thread`() = runTest {
        // Given
        val localThreadId = 1L
        val now = testClock.now()
        val aiMessage = Message(id = 1, text = "AI message", isFromMe = false, timestamp = now)
        coEvery { threadDao.getThreadById(localThreadId) } returns ThreadEntity(
            id = localThreadId,
            name = "Test Thread",
            isPinned = false,
            lastMessageText = "",
            lastMessageTime = now
        )

        // When
        repository.storeAiMessage(localThreadId, aiMessage)

        // Then
        coVerify(exactly = 1) { messageDao.insertMessage(any()) }
        coVerify(exactly = 1) { threadDao.updateThread(any()) }
    }

    @Test
    fun `getChatMessages returns flow of domain messages`() = runTest {
        // Given
        val localThreadId = 1L
        val now = testClock.now()
        val messageEntities = listOf(
            MessageEntity(
                id = 1,
                localThreadId = localThreadId,
                aiThreadId = null,
                text = "Hello",
                isFromMe = true,
                timestamp = now
            ),
            MessageEntity(
                id = 2,
                localThreadId = localThreadId,
                aiThreadId = null,
                text = "Hi",
                isFromMe = false,
                timestamp = now + 1.seconds
            )
        )
        coEvery { messageDao.getMessagesByThreadId(localThreadId) } returns flowOf(messageEntities)

        // When
        val flow = repository.getChatMessages(localThreadId)

        // Then
        flow.test {
            val list = awaitItem()
            Assert.assertEquals(2, list.size)
            Assert.assertEquals("Hello", list[0].text)
            Assert.assertEquals("Hi", list[1].text)
            awaitComplete()
        }
    }

    @Test
    fun `deleteAllMessages calls DAO to delete messages`() = runTest {
        // Given
        val localThreadId = 1L
        coEvery { messageDao.deleteAllMessagesByThreadId(localThreadId) } just runs

        // When
        repository.deleteAllMessages(localThreadId)

        // Then
        coVerify(exactly = 1) { messageDao.deleteAllMessagesByThreadId(localThreadId) }
    }
}
package com.tashila.hazle.viewmodels

import android.app.Application
import com.tashila.hazle.features.chat.ChatRepository
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.thread.ThreadRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ChatViewModelTest {

    private lateinit var chatViewModel: ChatViewModel
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val threadRepository: ThreadRepository = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        chatViewModel = ChatViewModel(chatRepository, threadRepository, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage when no active thread should create new thread and start service`() = runTest {
        // Given
        val message = "Hello, world!"
        val newThreadId = 1L
        chatViewModel.onMessageChange(message)
        coEvery { threadRepository.createThread(any()) } returns newThreadId

        // When
        chatViewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { threadRepository.createThread(message) }
    }

    @Test
    fun `sendMessage when active thread exists should not create new thread`() = runTest {
        // Given
        val message = "Hello, again!"
        val activeThreadId = 123L
        chatViewModel.setActiveThread(activeThreadId)
        chatViewModel.onMessageChange(message)

        // When
        chatViewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { threadRepository.createThread(any()) }
    }
}

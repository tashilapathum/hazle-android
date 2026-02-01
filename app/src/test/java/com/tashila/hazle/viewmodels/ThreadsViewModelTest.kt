package com.tashila.hazle.viewmodels

import com.tashila.hazle.db.threads.ThreadEntity
import com.tashila.hazle.features.thread.ThreadRepository
import com.tashila.hazle.features.thread.ThreadsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ExperimentalCoroutinesApi
@OptIn(ExperimentalTime::class)
class ThreadsViewModelTest {

    private lateinit var viewModel: ThreadsViewModel
    private val mockThreadRepository: ThreadRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockThreadRepository.getAllThreads() } returns flowOf(emptyList()) // Default mock
        viewModel = ThreadsViewModel(mockThreadRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createNewThread should create and select a new thread`() = runTest {
        // Given
        val threadName = "New Thread"
        val newThreadId = 1L
        coEvery { mockThreadRepository.createThread(threadName, false) } returns newThreadId

        // When
        viewModel.createNewThread(threadName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThreadRepository.createThread(threadName, false) }
        assertEquals(newThreadId, viewModel.selectedThreadId.value)
    }

    @Test
    fun `selectThread should update the selected thread id`() {
        // Given
        val threadId = 123L

        // When
        viewModel.selectThread(threadId)

        // Then
        assertEquals(threadId, viewModel.selectedThreadId.value)
    }

    @Test
    fun `deleteThread should delete the thread and clear selection if it was selected`() = runTest {
        // Given
        val threadId = 123L
        viewModel.selectThread(threadId)

        // When
        viewModel.deleteThread(threadId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThreadRepository.deleteThread(threadId) }
        assertNull(viewModel.selectedThreadId.value)
    }

    @Test
    fun `updateThread should call repository`() = runTest {
        // Given
        val thread = ThreadEntity(
            id = 1L,
            name = "Original Title",
            isPinned = false,
            createdAt = Instant.fromEpochMilliseconds(0)
        )

        // When
        viewModel.updateThread(thread)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThreadRepository.updateThread(thread) }
    }

    @Test
    fun `toggleThreadPin should update thread with inverted isPinned value`() = runTest {
        // Given
        val thread = ThreadEntity(
            id = 1L,
            name = "A Thread",
            isPinned = false,
            createdAt = Instant.fromEpochMilliseconds(0)
        )
        val expectedUpdatedThread = thread.copy(isPinned = true)

        // When
        viewModel.toggleThreadPin(thread)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThreadRepository.updateThread(expectedUpdatedThread) }
    }
}
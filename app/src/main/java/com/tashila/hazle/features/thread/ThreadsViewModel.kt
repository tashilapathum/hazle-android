package com.tashila.hazle.features.thread

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tashila.hazle.db.threads.ThreadEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ThreadsViewModel(
    private val threadRepository: ThreadRepository
) : ViewModel() {

    // StateFlow to hold the list of all chat threads
    val allThreads: StateFlow<List<ThreadEntity>> =
        threadRepository.getAllThreads()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5 seconds after last collector
                initialValue = emptyList()
            )

    // StateFlow to hold the currently selected thread ID
    private val _selectedThreadId = MutableStateFlow<Long?>(null)
    val selectedThreadId: StateFlow<Long?> = _selectedThreadId.asStateFlow()

    /**
     * Creates a new chat thread.
     * @param name The name of the new thread.
     * @param isPinned Whether the new thread should be pinned.
     * @return The ID of the newly created thread, or null if creation failed.
     */
    fun createNewThread(name: String, isPinned: Boolean = false) {
        viewModelScope.launch {
            try {
                val newThreadId = threadRepository.createThread(name, isPinned)
                _selectedThreadId.value = newThreadId // Automatically select the new thread
                Log.d(TAG, "Created new thread with ID: $newThreadId")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating new thread: ${e.localizedMessage}", e)
                // Handle error (e.g., show a Toast)
            }
        }
    }

    /**
     * Selects an existing thread to make it active.
     * @param localThreadId The ID of the thread to select.
     */
    fun selectThread(localThreadId: Long) {
        _selectedThreadId.value = localThreadId
        Log.d(TAG, "Selected thread with ID: $localThreadId")
    }

    /**
     * Deletes a specific thread and deselects it if it was the active one.
     * @param localThreadId The ID of the thread to delete.
     */
    fun deleteThread(localThreadId: Long) {
        viewModelScope.launch {
            try {
                threadRepository.deleteThread(localThreadId)
                Log.d(TAG, "Deleted thread with ID: $localThreadId")
                if (_selectedThreadId.value == localThreadId) {
                    _selectedThreadId.value = null // Deselect if the active thread was deleted
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting thread with ID $localThreadId: ${e.localizedMessage}", e)
                // Handle error
            }
        }
    }

    /**
     * Updates a thread.
     * @param thread The thread to update.
     */
    fun updateThread(thread: ThreadEntity) {
        viewModelScope.launch {
            threadRepository.updateThread(thread)
        }
    }

    /**
     * Toggles the pinned status of a thread.
     * @param thread The thread to update.
     */
    fun toggleThreadPin(thread: ThreadEntity) {
        viewModelScope.launch {
            try {
                val updatedThread = thread.copy(isPinned = !thread.isPinned)
                threadRepository.updateThread(updatedThread)
                Log.d(TAG, "Toggled pinned status for thread ID: ${thread.id} to ${updatedThread.isPinned}")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling pinned status for thread ID ${thread.id}: ${e.localizedMessage}", e)
                // Handle error
            }
        }
    }

    companion object {
        private const val TAG = "ThreadsViewModel"
    }
}
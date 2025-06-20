package com.tashila.hazle.features.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tashila.hazle.features.thread.ThreadRepository
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository
) : ViewModel() {

    // The active thread ID, managed by the ViewModel
    private val _activeThreadId = MutableStateFlow<Long?>(null)
    val activeThreadId: StateFlow<Long?> = _activeThreadId

    // Messages flow, now dependent on the activeThreadId
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<Message>> = _activeThreadId.flatMapLatest { threadId ->
        if (threadId != null) {
            chatRepository.getChatMessages(threadId) // Pass the threadId
        } else {
            emptyFlow() // No active thread, no messages
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage

    private val _chatTitle = MutableStateFlow("")
    val chatTitle: StateFlow<String> = _chatTitle

    private val _chatSubtitle = MutableStateFlow("")
    val chatSubtitle: StateFlow<String> = _chatSubtitle

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _closeChat = MutableSharedFlow<Unit>()
    val closeChat = _closeChat.asSharedFlow()

    init {
        // Observe _activeThreadId to automatically load messages
        // This is handled by the `messages` StateFlow using `flatMapLatest`
        // You might want to load the last active thread here, or prompt the user to create/select one.
        // For now, it will start with no active thread.
    }

    // Function to update the current message text
    fun onMessageChange(newMessage: String) {
        _currentMessage.value = newMessage
    }

    // Function to send the current message
    fun sendMessage() {
        _chatSubtitle.value = "Sending..."
        viewModelScope.launch {
            delay((500..2000).random().toLong())
            _chatSubtitle.value = "Thinking..." // delay because of the runs
        }
        val messageText = _currentMessage.value.trim()
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                // Ensure we have an active thread. If not, create a new one.
                val currentThreadId = _activeThreadId.value ?: run {
                    Log.d(TAG, "No active thread found, creating a new one.")
                    // Use a short snippet of the message as the thread name
                    val newThreadId = threadRepository.createThread(messageText)
                    _activeThreadId.value = newThreadId
                    newThreadId
                }

                _currentMessage.value = "" // Clear the input field

                Log.i(TAG, "sendMessage: Sending message to thread $currentThreadId")
                sendMessageInternal(currentThreadId, messageText)
            }
        }
    }

    // Function to start a new chat (which effectively means creating a new thread)
    fun startNewChat() {
        _activeThreadId.value = null // Clear active thread, a new one will be created on next message
        _currentMessage.value = "" // Clear any input
        _errorMessage.value = "" // Clear any errors
        _chatSubtitle.value = getRandomMessagePrompt()
        // messages flow will become empty if activeThreadId is null
        Log.d(TAG, "Starting a new chat.")
    }

    fun setActiveThread(threadId: Long?) {
        viewModelScope.launch {
            _activeThreadId.value = threadId
            if (threadId != null)
                _chatTitle.value = threadRepository.getThreadById(threadId)?.name ?: "Chat with Hazle"
        }
    }

    fun resetChat() {
        viewModelScope.launch {
            _activeThreadId.value?.let { threadId ->
                chatRepository.deleteAllMessages(threadId) // Delete messages for the active thread
                // You might also want to delete the thread itself, depending on your app's logic
                // threadRepository.deleteThread(threadId)
                // _activeThreadId.value = null // If thread is deleted, clear active
            }
            Log.d(TAG, "Resetting chat for active thread.")
        }
    }

    fun closeChat() {
        viewModelScope.launch { _closeChat.emit(Unit) }
        _activeThreadId.value = null
    }

    private fun sendMessageInternal(threadId: Long, messageContent: String) {
        viewModelScope.launch {
            try {
                // `sendUserMessage` now takes `threadId`
                val response = chatRepository.sendUserMessage(threadId, messageContent)
                if (response.status.isSuccess()) {
                    // `storeAiMessage` now takes `threadId`
                    chatRepository.storeAiMessage(threadId, response.bodyAsText())
                    Log.i(TAG, "sendMessageInternal: AI response stored for thread $threadId.")
                    _chatSubtitle.value = getRandomMessagePrompt()
                } else {
                    _errorMessage.value = "Failed to send: ${response.status.value} - ${response.bodyAsText()}"
                    Log.e(TAG, "sendMessageInternal: API error: ${response.status.value} - ${response.bodyAsText()}")
                    _chatSubtitle.value = "An error occurred"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error sending: ${e.localizedMessage}"
                Log.e(TAG, "sendMessageInternal: Exception: ${e.localizedMessage}", e)
                _chatSubtitle.value = "An error occurred"
            }
        }
    }

    fun getRandomMessagePrompt(): String {
        val prompts = listOf(
            "What's up?",
            "Followup?",
            "What's on your mind?",
            "Got a thought to share?",
            "Feeling chatty?"
        )
        return prompts.random()
    }

    companion object {
        const val TAG = "ChatViewModel"
    }
}
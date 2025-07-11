package com.tashila.hazle.features.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tashila.hazle.features.thread.ThreadRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository
) : ViewModel() {

    // The active thread ID, managed by the ViewModel
    private val _activeThreadId = MutableStateFlow<Long?>(null)
    private val _activeAiThreadId = MutableStateFlow<String?>(null)
    val activeThreadId: StateFlow<Long?> = _activeThreadId

    // Messages flow, now dependent on the activeThreadId
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _messages: StateFlow<List<Message>> = _activeThreadId.flatMapLatest { localThreadId ->
        if (localThreadId != null) {
            chatRepository.getChatMessages(localThreadId) // Pass the localThreadId
        } else {
            getGreeting()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage

    private val _chatTitle = MutableStateFlow("Chat with Hazle")
    val chatTitle: StateFlow<String> = _chatTitle

    private val _chatSubtitle = MutableStateFlow("")
    val chatSubtitle: StateFlow<String> = _chatSubtitle

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _closeChat = MutableSharedFlow<Unit>()
    val closeChat = _closeChat.asSharedFlow()

    init {
        _chatSubtitle.value = getRandomMessagePrompt()
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
        _activeAiThreadId.value = null
        _currentMessage.value = "" // Clear any input
        _errorMessage.value = "" // Clear any errors
        _chatTitle.value = "New chat"
        _chatSubtitle.value = "What's up?"
        Log.d(TAG, "Starting a new chat.")
    }

    fun setActiveThread(localThreadId: Long?) {
        viewModelScope.launch {
            _activeThreadId.value = localThreadId
            if (localThreadId != null) {
                val thread = threadRepository.getThreadById(localThreadId)
                thread?.let {
                    _chatTitle.value = it.name
                    _activeAiThreadId.value = it.aiThreadId
                }
            }
        }
    }

    fun closeChat() {
        viewModelScope.launch { _closeChat.emit(Unit) }
        _activeThreadId.value = null
    }

    /**
     * @param localThreadId is used to differentiate threads when showing notifications
     * */
    private fun sendMessageInternal(localThreadId: Long, messageContent: String) {
        viewModelScope.launch {
            try {
                chatRepository.sendUserMessage(localThreadId, _activeAiThreadId.value, messageContent)
                _chatSubtitle.value = getRandomMessagePrompt()
                // repository updates are automatically reflected on UI using _messages
            } catch (e: Exception) {
                _errorMessage.value = "Error sending: ${e.localizedMessage}"
                Log.e(TAG, "sendMessageInternal: Exception: ${e.localizedMessage}", e)
                _chatSubtitle.value = "Something went wrong"
            }
        }
    }

    private fun getGreeting(): Flow<List<Message>> {
        return flowOf(listOf(Message(
            text = "Hi, I'm Hazle, your AI assistant. I'll always try to give concise answers.",
            isFromMe = false
        )))
    }

    private fun getRandomMessagePrompt(): String {
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
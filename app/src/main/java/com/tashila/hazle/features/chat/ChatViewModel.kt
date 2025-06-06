package com.tashila.hazle.features.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// ViewModel to hold and manage chat data
class ChatViewModel(
    private val application: Application,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _closeChat = MutableSharedFlow<Unit>()
    val closeChat = _closeChat.asSharedFlow()

    init {
        loadMessages()
    }

    // Function to update the current message text
    fun onMessageChange(newMessage: String) {
        _currentMessage.value = newMessage
    }

    // Function to send the current message
    fun sendMessage() {
        val messageText = _currentMessage.value.trim()
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                val newMessage = Message(text = messageText, isFromMe = true)
                _messages.value = _messages.value + newMessage
                _currentMessage.value = "" // Clear the input field

                Log.i(TAG, "sendMessage: SENT")
                sendMessage(messageText)
            }
        }
    }

    // Helper function to load some initial mock messages for demonstration
    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getChatMessages().collect {
                _messages.value = it
            }
        }
    }

    fun resetChat() {
        viewModelScope.launch {
            chatRepository.deleteAllMessages()
        }
    }

    fun closeChat() {
        viewModelScope.launch { _closeChat.emit(Unit) }
    }

    private fun fetchGreeting() {
        viewModelScope.launch {
            try {
                // Call repository, which returns HttpResponse from ApiService
                val response = chatRepository.getGreetingMessage()
                // Extract the String content from the HttpResponse
                val message = response.bodyAsText()
                val responseMessage = Message(text = message, isFromMe = false)
                _messages.value = _messages.value + responseMessage
            } catch (e: Exception) {
                _errorMessage.value = e.message.toString()
                Log.e(TAG, "fetchGreeting: ${e.message}", e)
            }
        }
    }

    private fun sendMessage(messageContent: String) {
        viewModelScope.launch {
            try {
                val response = chatRepository.sendUserMessage(messageContent)
                if (response.status.isSuccess()) {
                    val responseMessage = Message(text = response.bodyAsText(), isFromMe = false)
                    _messages.value = _messages.value + responseMessage
                    chatRepository.storeAiMessage(response.bodyAsText())
                } else {
                    _errorMessage.value = "Failed to send: ${response.status.value} - ${response.bodyAsText()}"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error sending: ${e.localizedMessage}"
            }
        }
    }

    companion object {
        const val TAG = "ChatViewModel"
    }
}
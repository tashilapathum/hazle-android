package com.tashila.hazle.features.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**Stores raw ByteArray, uses our custom EncryptedBytesSerializer*/
private val Context.encryptedAuthDataStore: DataStore<ByteArray> by dataStore(
    fileName = "auth_data.pb", // The actual filename for the encrypted data
    serializer = EncryptionHandler()
)

class TokenStorageImpl(context: Context) : TokenStorage {

    private val dataStore: DataStore<ByteArray> = context.encryptedAuthDataStore
    private val json = Json { ignoreUnknownKeys = true } // Kotlinx Serialization JSON parser

    // Note: These methods still use runBlocking to match the non-suspend interface.
    // For a production app, it's a best practice to make TokenStorage methods `suspend`
    // and call them from coroutines (e.g., in your ViewModel's viewModelScope).
    override fun saveTokens(accessToken: String, refreshToken: String) {
        runBlocking {
            val tokenMap = mapOf(
                "accessToken" to accessToken,
                "refreshToken" to refreshToken
            )
            val jsonString = json.encodeToString(tokenMap)
            dataStore.updateData { jsonString.toByteArray(Charsets.UTF_8) } // Convert string to bytes
        }
    }

    override fun getAccessToken(): String? {
        return runBlocking {
            val bytes = dataStore.data.first()
            if (bytes.isEmpty()) return@runBlocking null // Handle empty/uninitialized data

            val jsonString = bytes.toString(Charsets.UTF_8) // Convert bytes to string
            val tokenMap = try {
                json.decodeFromString<Map<String, String>>(jsonString)
            } catch (e: Exception) {
                // Handle deserialization errors (e.g., corrupted JSON)
                println("Error deserializing token data: ${e.message}")
                return@runBlocking null
            }
            tokenMap["accessToken"]
        }?.takeIf { it.isNotBlank() }
    }

    override fun getRefreshToken(): String? {
        return runBlocking {
            val bytes = dataStore.data.first()
            if (bytes.isEmpty()) return@runBlocking null // Handle empty/uninitialized data

            val jsonString = bytes.toString(Charsets.UTF_8)
            val tokenMap = try {
                json.decodeFromString<Map<String, String>>(jsonString)
            } catch (e: Exception) {
                println("Error deserializing token data: ${e.message}")
                return@runBlocking null
            }
            tokenMap["refreshToken"]
        }?.takeIf { it.isNotBlank() }
    }

    override fun clearTokens() {
        runBlocking {
            dataStore.updateData { ByteArray(0) } // Clear data by setting to empty byte array
        }
    }
}
package com.tashila.hazle.features.auth

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/**Stores raw ByteArray, uses our custom EncryptedBytesSerializer*/
private val Context.encryptedAuthDataStore: DataStore<ByteArray> by dataStore(
    fileName = "auth_data.pb", // The actual filename for the encrypted data
    serializer = EncryptionHandler()
)

class TokenRepositoryImpl(context: Context) : TokenRepository {

    private val dataStore: DataStore<ByteArray> = context.encryptedAuthDataStore
    private val json = Json { ignoreUnknownKeys = true } // Kotlinx Serialization JSON parser

    override suspend fun saveTokens(accessToken: String?, refreshToken: String?) {
        dataStore.updateData { currentBytes ->
            // 1. Decode existing data
            val currentJson = if (currentBytes.isNotEmpty()) {
                json.decodeFromString<Map<String, String?>>(currentBytes.decodeToString())
            } else {
                emptyMap()
            }

            // 2. Merge values: Use new value if non-null, otherwise keep old
            val updatedMap = mapOf(
                "accessToken" to (accessToken ?: currentJson["accessToken"]),
                "refreshToken" to (refreshToken ?: currentJson["refreshToken"])
            )

            // 3. Encode and return
            json.encodeToString(updatedMap).toByteArray(Charsets.UTF_8)
        }
    }

    override suspend fun getAccessToken(): String? {
        val bytes = dataStore.data.first()
        if (bytes.isEmpty()) return null // Handle empty/uninitialized data

        val jsonString = bytes.toString(Charsets.UTF_8) // Convert bytes to string
        val tokenMap = try {
            json.decodeFromString<Map<String, String?>>(jsonString)
        } catch (e: Exception) {
            // Handle deserialization errors (e.g., corrupted JSON)
            println("Error deserializing token data: ${e.message}")
            return null
        }
        return tokenMap["accessToken"]?.takeIf {
            Log.i(TAG, "getAccessToken: $it")
            it.isNotBlank() 
        }
    }

    override suspend fun getRefreshToken(): String? {
        val bytes = dataStore.data.first()
        if (bytes.isEmpty()) return null // Handle empty/uninitialized data

        val jsonString = bytes.toString(Charsets.UTF_8)
        val tokenMap = try {
            json.decodeFromString<Map<String, String?>>(jsonString)
        } catch (e: Exception) {
            println("Error deserializing token data: ${e.message}")
            return null
        }
        return tokenMap["refreshToken"]?.takeIf { it.isNotBlank() }
    }

    override suspend fun clearTokens() {
        dataStore.updateData { ByteArray(0) }
    }

    companion object {
        private const val TAG = "TokenStorageImpl"
    }
}
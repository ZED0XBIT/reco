package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.UserDao
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("protect_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Restore session on app start
        val savedUserId = prefs.getString("active_user_id", null)
        if (savedUserId != null) {
            scope.launch {
                val user = userDao.getUserById(savedUserId)
                _currentUser.value = user
            }
        }
    }

    suspend fun register(username: String, password: String): AuthResult {
        val trimmedUsername = username.trim()
        if (trimmedUsername.length < 3) {
            return AuthResult.Error("Username must be at least 3 characters")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }

        val existing = userDao.getUserByUsername(trimmedUsername)
        if (existing != null) {
            return AuthResult.Error("Username is already taken. Please choose another.")
        }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        val user = UserEntity(
            userId = UUID.randomUUID().toString(),
            username = trimmedUsername,
            passwordHash = hash,
            salt = salt,
            createdAt = System.currentTimeMillis()
        )

        return try {
            userDao.insertUser(user)
            saveSession(user)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.localizedMessage}")
        }
    }

    suspend fun login(username: String, password: String): AuthResult {
        val trimmedUsername = username.trim()
        val user = userDao.getUserByUsername(trimmedUsername)
            ?: return AuthResult.Error("Account not found. Please check username or register.")

        val computedHash = hashPassword(password, user.salt)
        if (computedHash != user.passwordHash) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        saveSession(user)
        return AuthResult.Success(user)
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().remove("active_user_id").apply()
    }

    suspend fun changePassword(currentPass: String, newPass: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        if (hashPassword(currentPass, user.salt) != user.passwordHash) {
            return Result.failure(Exception("Current password does not match"))
        }
        if (newPass.length < 6) {
            return Result.failure(Exception("New password must be at least 6 characters"))
        }

        val newSalt = generateSalt()
        val newHash = hashPassword(newPass, newSalt)
        userDao.updatePassword(user.userId, newHash, newSalt)

        val updatedUser = user.copy(passwordHash = newHash, salt = newSalt)
        _currentUser.value = updatedUser
        return Result.success(Unit)
    }

    suspend fun deleteAccount(): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        userDao.deleteUser(user.userId)
        logout()
        return Result.success(Unit)
    }

    private fun saveSession(user: UserEntity) {
        _currentUser.value = user
        prefs.edit().putString("active_user_id", user.userId).apply()
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$password:$salt"
        val hash = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}

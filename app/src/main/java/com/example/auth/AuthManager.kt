package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AppUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isAdmin: Boolean
)

object AuthManager {
    private const val TAG = "AuthManager"
    val ADMIN_EMAILS = setOf(
        "realverizon@gmail.com",
        "admin@serenime.id"
    )

    private val auth: FirebaseAuth get() = Firebase.auth

    // Unified AppUser state that handles both Google Sign-In and Emulator/Quick Login
    private val _currentUser = MutableStateFlow<AppUser?>(
        auth.currentUser?.let { user ->
            AppUser(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: "Pengguna",
                photoUrl = user.photoUrl?.toString(),
                isAdmin = isEmailAdmin(user.email)
            )
        }
    )
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _isAdmin = MutableStateFlow(_currentUser.value?.isAdmin ?: false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                val admin = isEmailAdmin(user.email)
                _currentUser.value = AppUser(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Pengguna",
                    photoUrl = user.photoUrl?.toString(),
                    isAdmin = admin
                )
                _isAdmin.value = admin
            }
        }
    }

    fun isEmailAdmin(email: String?): Boolean {
        if (email == null) return false
        val lower = email.lowercase()
        return lower in ADMIN_EMAILS || lower.contains("admin")
    }

    // Google Sign-In via Credential Manager
    fun signInWithGoogle(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (AppUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val activity = context as? Activity
        if (activity == null) {
            onError("Context is not an Activity")
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "Missing default_web_client_id", e)
            onError("Google Sign-In configuration error: missing web client id")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch(Dispatchers.IO) {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val firebaseUser = authResult.user

                    if (firebaseUser != null) {
                        val admin = isEmailAdmin(firebaseUser.email)
                        val appUser = AppUser(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName ?: "Pengguna Serenime",
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            isAdmin = admin
                        )
                        _currentUser.value = appUser
                        _isAdmin.value = admin
                        syncUserProfile(context, firebaseUser)
                        launch(Dispatchers.Main) { onSuccess(appUser) }
                    } else {
                        launch(Dispatchers.Main) { onError("Login failed: empty user profile") }
                    }
                } else {
                    launch(Dispatchers.Main) { onError("Tipe kredensial tidak sesuai") }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled: ${e.message}")
                launch(Dispatchers.Main) { onCancelled() }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                launch(Dispatchers.Main) {
                    onError("Google Sign-In error (Akun belum ada di emulator). Anda dapat menggunakan tombol 'Quick Login Admin' di bawah untuk akses langsung!")
                }
            }
        }
    }

    // Emulator / Quick Testing Login as Administrator
    fun quickLoginAsAdmin(context: Context) {
        val adminUser = AppUser(
            uid = "admin-realverizon-777",
            email = "realverizon@gmail.com",
            displayName = "Admin Serenime (realverizon)",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
            isAdmin = true
        )
        _currentUser.value = adminUser
        _isAdmin.value = true

        // Register profile in Firestore
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            db.collection("users").document(adminUser.uid).set(
                mapOf(
                    "email" to adminUser.email,
                    "displayName" to adminUser.displayName,
                    "photoUrl" to (adminUser.photoUrl ?: ""),
                    "role" to "admin",
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync skipped: ${e.message}")
        }
    }

    // Emulator / Quick Testing Login as Regular Member
    fun quickLoginAsMember(context: Context) {
        val memberUser = AppUser(
            uid = "member-user-888",
            email = "wibu.serenime@gmail.com",
            displayName = "Otaku Member",
            photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
            isAdmin = false
        )
        _currentUser.value = memberUser
        _isAdmin.value = false
    }

    private fun syncUserProfile(context: Context, user: FirebaseUser) {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            val role = if (isEmailAdmin(user.email)) "admin" else "user"
            val userDoc = db.collection("users").document(user.uid)

            val profileData = mapOf(
                "email" to (user.email ?: ""),
                "displayName" to (user.displayName ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "role" to role,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc.set(profileData, com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync user profile to Firestore: ${e.message}")
        }
    }

    fun signOut(context: Context, scope: CoroutineScope, onComplete: () -> Unit) {
        try {
            auth.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
        _isAdmin.value = false
        val credentialManager = CredentialManager.create(context)
        scope.launch(Dispatchers.IO) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                launch(Dispatchers.Main) { onComplete() }
            }
        }
    }
}

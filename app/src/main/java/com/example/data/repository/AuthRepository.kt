package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.domain.model.TraderProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "AuthRepository"

data class AuthState(
    val user: FirebaseUser? = null,
    val isAuthenticated: Boolean = false,
    val profile: TraderProfile? = null
)

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUser: FirebaseUser?
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser>
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun registerWithEmail(email: String, password: String, username: String): Result<FirebaseUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signOut(context: Context? = null)
    suspend fun getUserProfile(userId: String): TraderProfile?
    suspend fun updateUserProfile(profile: TraderProfile): Result<Unit>
}

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Ensure profile document exists in Firestore (non-destructive merge)
                ensureUserProfileExists(user)

                // Fetch and observe user profile from Firestore users/{userId}
                val registration = firestore.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Profile snapshot error: ${error.message}")
                        }
                        val profile = snapshot?.toObject(TraderProfileDocument::class.java)?.toDomain(
                            user.uid,
                            user.displayName ?: user.email.orEmpty()
                        ) ?: TraderProfile(
                            username = user.displayName?.ifBlank { null }
                                ?: user.email?.substringBefore("@")
                                ?: "Trader_${user.uid.take(5)}",
                            accountTier = "Verified Trader",
                            virtualBalance = 10000.0,
                            initialBalance = 10000.0,
                            totalTrades = 0,
                            winRate = 0.0,
                            bestTradePnl = 0.0,
                            totalPnl = 0.0
                        )
                        trySend(AuthState(user = user, isAuthenticated = true, profile = profile))
                    }
                // registration will be disposed when flow cancels or changes
            } else {
                trySend(AuthState(user = null, isAuthenticated = false, profile = null))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun ensureUserProfileExists(user: FirebaseUser) {
        val userDoc = firestore.collection("users").document(user.uid)
        userDoc.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val initialData = mapOf(
                    "userId" to user.uid,
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                    "displayName" to (user.displayName ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "username" to (user.displayName?.ifBlank { null } ?: user.email?.substringBefore("@") ?: "Trader_${user.uid.take(5)}"),
                    "accountTier" to "Verified Trader",
                    "virtualBalance" to 10000.0,
                    "initialBalance" to 10000.0,
                    "totalTrades" to 0,
                    "winRate" to 0.0,
                    "bestTradePnl" to 0.0,
                    "totalPnl" to 0.0,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                userDoc.set(initialData, SetOptions.merge())
            }
        }
    }

    override suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                return Result.failure(IllegalStateException("Web Client ID is not configured in strings or google-services.json"))
            }

            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

            val activity = context as? Activity
                ?: return Result.failure(IllegalStateException("Context must be an Activity for Google Sign-In"))

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw IllegalStateException("Firebase user is null after Google sign-in")
                ensureUserProfileExists(user)
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unsupported credential type returned"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In dismissed: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw IllegalStateException("User is null after sign in")
            ensureUserProfileExists(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw IllegalStateException("User is null after registration")

            // Initialize default user document in Firestore
            val initialProfile = TraderProfileDocument(
                userId = user.uid,
                uid = user.uid,
                email = email.trim(),
                displayName = username.ifBlank { "Trader_${user.uid.take(5)}" },
                username = username.ifBlank { "Trader_${user.uid.take(5)}" },
                accountTier = "Verified Pro Trader",
                virtualBalance = 10000.0,
                initialBalance = 10000.0,
                totalTrades = 0,
                winRate = 0.0,
                bestTradePnl = 0.0,
                totalPnl = 0.0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(user.uid).set(initialProfile, SetOptions.merge()).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(context: Context?) {
        withContext(NonCancellable) {
            try {
                if (context != null) {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed clearing credential state: ${e.message}")
            } finally {
                auth.signOut()
            }
        }
    }

    override suspend fun getUserProfile(userId: String): TraderProfile? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.toObject(TraderProfileDocument::class.java)?.toDomain(
                userId,
                auth.currentUser?.displayName ?: auth.currentUser?.email.orEmpty()
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(profile: TraderProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not authenticated"))
        return try {
            val map = mapOf(
                "username" to profile.username,
                "displayName" to profile.username,
                "accountTier" to profile.accountTier,
                "virtualBalance" to profile.virtualBalance,
                "totalTrades" to profile.totalTrades,
                "winRate" to profile.winRate,
                "bestTradePnl" to profile.bestTradePnl,
                "totalPnl" to profile.totalPnl,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class TraderProfileDocument(
    val userId: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val username: String = "",
    val accountTier: String = "Verified Trader",
    val virtualBalance: Double = 10000.0,
    val initialBalance: Double = 10000.0,
    val totalTrades: Int = 0,
    val winRate: Double = 0.0,
    val bestTradePnl: Double = 0.0,
    val totalPnl: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(uid: String, fallbackName: String): TraderProfile {
        val resolvedName = when {
            username.isNotBlank() -> username
            displayName.isNotBlank() -> displayName
            fallbackName.isNotBlank() -> fallbackName.substringBefore("@")
            else -> "Trader_${uid.take(5)}"
        }
        return TraderProfile(
            username = resolvedName,
            accountTier = accountTier,
            virtualBalance = virtualBalance,
            initialBalance = initialBalance,
            totalTrades = totalTrades,
            winRate = winRate,
            bestTradePnl = bestTradePnl,
            totalPnl = totalPnl
        )
    }
}

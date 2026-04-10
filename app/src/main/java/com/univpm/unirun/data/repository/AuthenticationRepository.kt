package com.univpm.unirun.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.db.UserEntity
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/**
 * Repository that centralizes all authentication operations.
 * Handles Firebase Auth operations and persists user data to Room + DataStore.
 */
class AuthenticationRepository(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Signs in user with email and password using Firebase Auth.
     */
    suspend fun signInWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * Creates a new user account with email and password.
     */
    suspend fun signUpWithEmail(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    /**
     * Signs in with Google using ID token.
     * Exchanges Google ID token for Firebase credential.
     * Returns true if this is a new user (first-time login), false if existing user.
     * Automatically saves basic user profile on first login.
     */
    suspend fun signInWithGoogle(idToken: String): Boolean {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()

        val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
        val currentUser = firebaseAuth.currentUser
            ?: throw Exception("Utente non autenticato dopo Google Sign-In")

        if (isNewUser) {
            val userEntity = UserEntity(
                uid = currentUser.uid,
                name = currentUser.displayName ?: "Utente",
                weightKg = 70f,
                heightCm = 170
            )
            database.userDao().upsert(userEntity)
            return true
        }

        return false
    }

    /**
     * Sends password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    /**
     * Signs out current user from Firebase.
     * Does NOT clear user preferences (weight, height) for next login session.
     */
    suspend fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Saves user profile to both DataStore (UserPreferencesRepository) and Room database.
     * Called after onboarding completion or profile updates.
     */
    suspend fun saveUserProfile(name: String, weightKg: Float, heightCm: Int) {
        val uid = firebaseAuth.currentUser?.uid
            ?: throw Exception("Utente non autenticato")

        preferencesRepository.saveProfile(name, weightKg, heightCm)

        val userEntity = UserEntity(
            uid = uid,
            name = name,
            weightKg = weightKg,
            heightCm = heightCm
        )
        database.userDao().upsert(userEntity)

        val data = mapOf(
            "name" to name,
            "weightKg" to weightKg,
            "heightCm" to heightCm,
            "onboardingDone" to true
        )
        firestore.collection("users").document(uid).set(data, SetOptions.merge()).await()
    }

    /**
     * Checks if onboarding has been completed.
     * Reads from DataStore via UserPreferencesRepository.
     */
    suspend fun isOnboardingDone(): Boolean {
        return preferencesRepository.userPreferencesFlow.first().onboardingDone
    }

    suspend fun isOnboardingDoneForUser(uid: String): Boolean {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.exists() && doc.getBoolean("onboardingDone") == true
        } catch (e: Exception) {
            preferencesRepository.userPreferencesFlow.first().onboardingDone
        }
    }

    /**
     * Checks if user is currently authenticated.
     */
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Gets current user's UID if authenticated, null otherwise.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Gets user profile from Room database.
     */
    suspend fun getUserProfile(uid: String): UserEntity? {
        return database.userDao().getById(uid)
    }
}

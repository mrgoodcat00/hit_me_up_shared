package com.mrgoodcat.hitmeup.data.repostory

import android.app.Activity
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_TOO_MUCH_ATTEMPTS
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_UNKNOWN_ERROR
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_UNKNOWN_ERROR
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthorizationRepositoryImpl @Inject constructor(
    private val ioDispatcher: CoroutineDispatcher,
) : AuthorizationRepository {

    private val firebaseAuth = Firebase.auth

    init {
        Locale.getDefault().language.let {
            firebaseAuth.setLanguageCode(it)
        }
    }

    override fun getAuthorization(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun logOutFirebase() {
        Timber.d("LOGOUT_user_ ${firebaseAuth.currentUser?.uid}")
        firebaseAuth.signOut()
    }

    override fun itsMyId(id: String): Boolean {
        return firebaseAuth.currentUser?.uid == id
    }

    override suspend fun registerWithEmail(email: String, password: String): FirebaseUser {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { registrationResult ->
                            val user = registrationResult.user
                            if (user == null) {
                                continuation.resumeWithException(
                                    CancellationException(
                                        GLOBAL_ERROR_UNKNOWN_ERROR
                                    )
                                )
                                return@addOnSuccessListener
                            } else {
                                continuation.resume(user)
                            }
                        }
                        .addOnFailureListener {
                            Timber.d("createUserWithEmailAndPassword addOnFailureListener")
                            continuation.resumeWithException(it)
                        }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun authorizationWithGoogle(contextWeakRef: WeakReference<Activity>): AuthorizationResult {
        return withContext(ioDispatcher) {

            val context = contextWeakRef.get() ?: throw Exception(GLOBAL_ERROR_UNKNOWN_ERROR)

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.google_server_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption as CredentialOption)
                .build()

            var credential: GetCredentialResponse? = null

            try {
                credential = CredentialManager
                    .create(context)
                    .getCredential(context, request)
            } catch (e: Exception) {
                Timber.d("AuthorizationWithGoogle_exception ${e.message}")
                AuthorizationResult.ResultFalse(e.message ?: GLOBAL_ERROR_UNKNOWN_ERROR)
            }

            suspendCancellableCoroutine { continuation ->
                Timber.d("AuthorizationWithGoogle: ${firebaseAuth.currentUser?.uid}")

                when (credential?.credential) {
                    is PublicKeyCredential -> {
                        // Share responseJson such as a GetCredentialResponse on your server to
                        // validate and authenticate
                        val responseJson =
                            (credential.credential as PublicKeyCredential).authenticationResponseJson
                        Timber.e("Received PublicKeyCredential $responseJson")
                    }

                    // Password credential
                    is PasswordCredential -> {
                        // Send ID and password to your server to validate and authenticate.
                        val username = (credential.credential as PasswordCredential).id
                        val password = (credential.credential as PasswordCredential).password
                        Timber.e("Received PasswordCredential ${credential.credential}")
                    }

                    is CustomCredential -> {
                        if (credential.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                Timber.e(
                                    "Received credential.credential.data ${
                                        credential.credential.data.keySet().toList()
                                    }"
                                )

                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(credential.credential.data)

                                Timber.e("Received CustomCredential ${googleIdTokenCredential.id}")

                                val googleCredentials = GoogleAuthProvider.getCredential(
                                    googleIdTokenCredential.idToken,
                                    null
                                )

                                FirebaseAuth
                                    .getInstance()
                                    .signInWithCredential(googleCredentials)
                                    .addOnSuccessListener {
                                        continuation.resumeWith(
                                            Result.success(
                                                AuthorizationResult.ResultSuccessful(
                                                    it.user
                                                )
                                            )
                                        )
                                    }
                                    .addOnCanceledListener {
                                        Timber.e("signInWithCredential OnCanceled")
                                        continuation.resumeWith(Result.success(AuthorizationResult.ResultCanceled))
                                    }
                                    .addOnFailureListener {
                                        Timber.e("signInWithCredential OnFailure")
                                        continuation.resumeWith(
                                            Result.success(
                                                AuthorizationResult.ResultFailed(
                                                    it.message
                                                        ?: GLOBAL_ERROR_UNKNOWN_ERROR
                                                )
                                            )
                                        )
                                    }


                            } catch (e: GoogleIdTokenParsingException) {
                                Timber.e(e, "Received an invalid google id token response $e")
                                continuation.resumeWithException(e)
                            }
                        } else {
                            Timber.e("Unexpected type of credential")
                            continuation.resumeWith(
                                Result.success(
                                    AuthorizationResult.ResultFailed(
                                        GLOBAL_ERROR_UNKNOWN_ERROR
                                    )
                                )
                            )
                        }
                    }

                    else -> {
                        continuation.resumeWith(
                            Result.success(
                                AuthorizationResult.ResultCanceled
                            )
                        )
                    }
                }
            }
        }
    }

    override suspend fun authorizationWithFacebook(context: WeakReference<ActivityResultRegistryOwner>): AuthorizationResult {
        val registryOwner = context.get() ?: return AuthorizationResult.ResultCanceled

        return withContext(ioDispatcher) {
            Timber.d("AuthorizationWithFacebook_${firebaseAuth.currentUser?.uid}")

            val loginManager = LoginManager.getInstance()

            val callbackManager = CallbackManager.Factory.create()

            suspendCancellableCoroutine { cancellableCoroutine ->
                loginManager.registerCallback(
                    callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onCancel() {
                            Timber.d("AuthorizationWithFacebook_cancel_${firebaseAuth.currentUser?.uid}")
                            cancellableCoroutine.resumeWith(Result.success(AuthorizationResult.ResultCanceled))
                        }

                        override fun onError(error: FacebookException) {
                            Timber.d("AuthorizationWithFacebook_error_${firebaseAuth.currentUser?.uid}")
                            cancellableCoroutine.resumeWith(
                                Result.success(
                                    AuthorizationResult.ResultFailed(
                                        error.message ?: GLOBAL_ERROR_UNKNOWN_ERROR
                                    )
                                )
                            )
                        }

                        override fun onSuccess(result: LoginResult) {
                            val credentials =
                                FacebookAuthProvider.getCredential(result.accessToken.token)

                            FirebaseAuth
                                .getInstance()
                                .signInWithCredential(credentials)
                                .addOnFailureListener {
                                    Timber.d("AuthorizationWithFacebook_firebase_fail_${it.message}")
                                    cancellableCoroutine.resumeWith(
                                        Result.success(
                                            AuthorizationResult.ResultFailed(
                                                it.message ?: GLOBAL_ERROR_UNKNOWN_ERROR
                                            )
                                        )
                                    )
                                }
                                .addOnSuccessListener { task ->
                                    Timber.d("AuthorizationWithFacebook_firebase_success_${firebaseAuth.currentUser?.uid}")
                                    cancellableCoroutine.resumeWith(
                                        Result.success(
                                            AuthorizationResult.ResultSuccessful(
                                                task.user
                                            )
                                        )
                                    )
                                }
                        }
                    })
                try {
                    loginManager.logInWithReadPermissions(
                        registryOwner,
                        callbackManager,
                        listOf("email", "public_profile")
                    )
                } catch (e: Exception) {
                    cancellableCoroutine.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun authorizationWithEmail(
        login: String,
        password: String
    ): AuthorizationResult {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { coroutine ->
                try {
                    firebaseAuth
                        .signInWithEmailAndPassword(login, password)
                        .addOnCanceledListener {
                            coroutine.resumeWith(Result.success(AuthorizationResult.ResultCanceled))
                        }
                        .addOnSuccessListener {
                            coroutine.resumeWith(
                                Result.success(
                                    AuthorizationResult.ResultSuccessful(
                                        it.user
                                    )
                                )
                            )
                        }
                        .addOnFailureListener {
                            Timber.d("authorizationWithEmail fail ${it.javaClass.name}")
                            val error = when (it) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    it.errorCode
                                }

                                is FirebaseAuthInvalidUserException -> {
                                    it.errorCode
                                }

                                is FirebaseTooManyRequestsException -> {
                                    FIREBASE_ERROR_TOO_MUCH_ATTEMPTS
                                }

                                else -> {
                                    FIREBASE_UNKNOWN_ERROR
                                }
                            }

                            coroutine.resumeWith(
                                Result.success(
                                    AuthorizationResult.ResultFailed(
                                        error
                                    )
                                )
                            )
                        }
                } catch (e: Exception) {
                    coroutine.resumeWithException(e)
                }
            }
        }
    }
}
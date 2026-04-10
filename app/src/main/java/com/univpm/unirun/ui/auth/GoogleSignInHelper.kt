package com.univpm.unirun.ui.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.univpm.unirun.R

fun launchGoogleSignIn(
    context: Context,
    onIntentReady: (Intent) -> Unit
) {
    val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

    googleSignInClient.signOut().addOnCompleteListener {
        onIntentReady(googleSignInClient.signInIntent)
    }
}

fun parseGoogleSignInAccount(data: Intent?): GoogleSignInAccount {
    return GoogleSignIn.getSignedInAccountFromIntent(data).getResult(Exception::class.java)
}

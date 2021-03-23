package com.example.centicbids.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.centicbids.R
import com.example.centicbids.databinding.ActivityLoginScreenBinding
import com.example.centicbids.repository.FirebaseRepository
import com.example.centicbids.util.showToast
import com.example.centicbids.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginScreen : AppCompatActivity() {


    // these are used for the google sign in
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var loginScreenBinding: ActivityLoginScreenBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBinding()
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
    }

    private fun initDataBinding() {
        loginScreenBinding = DataBindingUtil.setContentView(this, R.layout.activity_login_screen)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginScreenBinding.viewModel = loginViewModel
        loginScreenBinding.lifecycleOwner = this
        firebaseAuth = FirebaseRepository.getFirebaseAuth()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val signedInAccountFromIntent = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = signedInAccountFromIntent.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showToast(this, getString(R.string.msg_google_sign_in_failed), Toast.LENGTH_SHORT)
            }
        }
    }

    fun onLoginClicked(view: View) {
        signInUsingGoogle()
    }

    private fun signInUsingGoogle() {
        signOutGoogle()
        val signIntent = googleSignInClient.signInIntent
        startActivityForResult(signIntent, RC_SIGN_IN)
    }

    private fun signOutGoogle() {
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            googleSignInClient.signOut()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        loginViewModel.progressBarVisibility.value = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in successful. displlay the message
                    val user = firebaseAuth.currentUser
                    val saveUser = loginViewModel.saveUser(user)
                    if (saveUser) {
                        showToast(
                            this,
                            getString(R.string.msg_google_sign_in_success),
                            Toast.LENGTH_SHORT
                        )
                        finish()
                    } else {
                        showToast(
                            this,
                            getString(R.string.msg_google_sign_in_failed),
                            Toast.LENGTH_SHORT
                        )
                        firebaseAuth.signOut()
                        loginViewModel.progressBarVisibility.value = View.GONE
                    }
                    //updateUI(user)
                } else {
                    // If sign in failed. display the error
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    loginViewModel.progressBarVisibility.value = View.GONE
                    showToast(
                        this,
                        getString(R.string.msg_google_sign_in_failed),
                        Toast.LENGTH_SHORT
                    )
                }
            }
    }

    companion object {
        private const val TAG = "CenticBids.Log"
        private const val RC_SIGN_IN = 9001
    }

}
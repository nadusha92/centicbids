package com.example.centicbids.viewmodel

import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.centicbids.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {
    var progressBarVisibility: MediatorLiveData<Int> = MediatorLiveData()

    init {
        progressBarVisibility.value = View.GONE
    }

    fun saveUser(user: FirebaseUser): Boolean {
        val saveUser = FirebaseRepository.saveUser(user)
        progressBarVisibility.value = View.GONE
        return saveUser
    }
}
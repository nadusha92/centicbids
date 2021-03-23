package com.example.centicbids.viewmodel

import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var progressBarVisibility : MediatorLiveData<Int> = MediatorLiveData()

    init {
        progressBarVisibility.value = View.GONE
    }
}
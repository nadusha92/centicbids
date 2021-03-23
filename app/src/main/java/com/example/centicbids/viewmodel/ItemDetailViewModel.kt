package com.example.centicbids.viewmodel

import android.view.View
import androidx.lifecycle.*
import com.example.centicbids.model.Bid
import com.example.centicbids.model.Item
import com.example.centicbids.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ItemDetailViewModel : ViewModel() {
    var progressBarVisibility: MutableLiveData<Int> = MutableLiveData()
    var item: MediatorLiveData<Item> = MediatorLiveData()
    var throwable: MediatorLiveData<Throwable> = MediatorLiveData()

    init {
        progressBarVisibility.value = View.GONE
    }

    fun downLoadItem(documentId: String) {
        progressBarVisibility.value = View.VISIBLE
        viewModelScope.launch {
            val loadItem = FirebaseRepository.loadItemAndAwaitResponse(documentId)
            if (loadItem.item != null) {
                item.value = loadItem.item
                progressBarVisibility.value = View.GONE
            } else {
                throwable.value = Throwable("failed to download data!")
                progressBarVisibility.value = View.GONE
            }
        }
    }

    fun placeBid(bid: Bid): Boolean {
        return FirebaseRepository.placeBid(bid)
    }

}
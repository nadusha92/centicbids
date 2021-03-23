package com.example.centicbids.listener

import com.google.firebase.firestore.DocumentSnapshot

interface OnItemClickListener {
    fun onItemClicked(item: DocumentSnapshot)
}
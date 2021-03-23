package com.example.centicbids.repository

import android.util.Log
import com.example.centicbids.model.Bid
import com.example.centicbids.model.Item
import com.example.centicbids.model.ItemResponse
import com.example.centicbids.model.User
import com.example.centicbids.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.lang.Exception

object FirebaseRepository {
    private fun getFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun getFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    fun saveUser(user: FirebaseUser): Boolean {
        val collection = getFireStore().collection(Constants.collection_users)
        var saveSuccess = true
        collection.document(user.uid)
            .set(User(user.email, user.displayName, true))
            .addOnSuccessListener {
                saveSuccess = true
            }
            .addOnFailureListener {
                saveSuccess = false
            }
        return saveSuccess
    }

    fun getAllItemsQuery(): Query {
        return getFireStore().collection(Constants.collection_items)
    }

    fun getItemDocumentReference(documentId: String): DocumentReference {
        return getFireStore().collection(Constants.collection_items).document(documentId)
    }

    fun placeBid(bid: Bid): Boolean {
        var saveSuccess = true
        val itemDocument = getFireStore().collection(Constants.collection_items).document(bid.itemDocumentId)
        val userDocument = getFireStore().collection(Constants.collection_users).document(bid.userDocumentId)
        Log.v("vPath", itemDocument.path)

        val data = hashMapOf(
            "bidValue" to bid.bidValue,
            "author" to itemDocument.path,
            "itemId" to itemDocument.path
        )

        getFireStore().collection(Constants.collection_bids)
            .add(data)
            .addOnSuccessListener {
                saveSuccess = true
            }
            .addOnFailureListener {
                saveSuccess = false
            }

        itemDocument.update(
            mapOf(
                "highestBid" to bid.bidValue,
            )
        )

        return saveSuccess
    }

    suspend fun loadItemAndAwaitResponse(documentId: String): ItemResponse {
        var itemResponse = ItemResponse()
        val db = getFireStore()
        try {
            val snapshot = db.collection(Constants.collection_items).document(documentId)
                .get()
                .await()
            if (snapshot != null) {
                snapshot.toObject(Item::class.java)?.let { downloadedItem ->
                    itemResponse.item = downloadedItem
                }
            } else {
                itemResponse.throwable = Throwable("Failed to download Item")
            }
        } catch (e: Exception) {
            itemResponse.throwable = Throwable("Failed to download Item")
        }
        return itemResponse
    }
}
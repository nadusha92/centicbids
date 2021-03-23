package com.example.centicbids.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
class Item {
    var title: String? = null
    var description: String? = null
    var basePrice: Double? = null
    var highestBid: Double? = null
    var expiryDate: Long? = null
    lateinit var images: ArrayList<String>

    constructor()

    constructor(
        title: String?,
        description: String?,
        basePrice: Double?,
        highestBid: Double?,
        expiryDate: Long?,
        images: ArrayList<String>?
    ) {
        this.title = title
        this.description = description
        this.basePrice = basePrice
        this.highestBid = highestBid
        this.expiryDate = expiryDate
        if (images != null) {
            this.images = images
        }
    }
}
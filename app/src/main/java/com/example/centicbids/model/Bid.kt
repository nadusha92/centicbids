package com.example.centicbids.model

data class Bid(
    var userDocumentId: String,
    var bidValue: Double,
    var itemDocumentId: String
)
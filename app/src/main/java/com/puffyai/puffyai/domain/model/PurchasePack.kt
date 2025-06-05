package com.puffyai.puffyai.domain.model

data class PurchasePack(
    val productId: String,
    val name: String,
    val description: String,
    val price: String,
    val credits: Int
)
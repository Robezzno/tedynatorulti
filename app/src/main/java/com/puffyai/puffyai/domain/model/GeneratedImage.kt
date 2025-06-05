package com.puffyai.puffyai.domain.model

data class GeneratedImage(
    val id: Long = 0,
    val prompt: String,
    val imageUrl: String,
    val timestamp: Long
)
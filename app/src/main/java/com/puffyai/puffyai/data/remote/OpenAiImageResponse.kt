package com.puffyai.puffyai.data.remote

import com.google.gson.annotations.SerializedName

data class OpenAiImageResponse(
    @SerializedName("created") val created: Long,
    @SerializedName("data") val data: List<ImageData>
)
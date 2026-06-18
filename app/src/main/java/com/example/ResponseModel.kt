package com.example

import com.google.gson.annotations.SerializedName

data class ResponseModel(
    @SerializedName("status")
    val status: String?,

    @SerializedName("message")
    val message: String?
)
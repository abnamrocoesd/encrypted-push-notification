package com.abnamro.push.common.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PostRequestData (@SerializedName("data") @Expose val data: Data, @SerializedName("to") @Expose val to: String )
data class Data (@SerializedName("encrypted-content") @Expose val content: Content)
data class Content(val version: String, val title: String, val message: String, val type: String, val key: String, val payload: String)
data class EncryptedData(val title: String, val message: String, val type: String, val url: String?)
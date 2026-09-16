package com.example.p3.data.model

import com.google.gson.annotations.SerializedName

data class GithubFileRequest(
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: String,
    @SerializedName("branch") val branch: String = "main",
)

data class GithubFileResponse(
    @SerializedName("content") val content: GithubContent? = null,
)

data class GithubContent(
    @SerializedName("download_url") val downloadUrl: String? = null,
    @SerializedName("path") val path: String? = null,
)

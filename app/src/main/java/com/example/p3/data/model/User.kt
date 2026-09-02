package com.example.p3.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("password") val password: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("phone") val phone: String = "",
    @SerializedName("city") val city: String = "",
)

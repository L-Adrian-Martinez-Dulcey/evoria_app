package com.example.p3.data.api

import com.example.p3.data.model.GithubFileRequest
import com.example.p3.data.model.GithubFileResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface GithubApiService {
    @PUT("repos/Esthefany-Chavez/EvoriaImages/contents/{path}")
    suspend fun uploadFile(
        @Path("path", encoded = true) path: String,
        @Header("Authorization") authorization: String,
        @Header("Accept") accept: String,
        @Body request: GithubFileRequest,
    ): GithubFileResponse
}

package com.example.p3.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import com.example.p3.BuildConfig
import com.example.p3.data.api.GithubApiService
import com.example.p3.data.model.GithubFileRequest
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID

class ImageRepository(
    private val contentResolver: ContentResolver,
    private val githubApi: GithubApiService,
) {
    suspend fun uploadUserImage(uri: Uri, userId: String): String =
        upload(uri, "users", userId)

    suspend fun uploadEventImage(uri: Uri, eventId: String): String =
        upload(uri, "events", eventId)

    private suspend fun upload(uri: Uri, folder: String, ownerId: String): String {
        val token = BuildConfig.GITHUB_TOKEN.trim()
        require(token.isNotEmpty()) {
            "Configura github.token en local.properties antes de subir imágenes."
        }

        val mimeType = contentResolver.getType(uri)?.lowercase(Locale.US)
        val extension = when (mimeType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            else -> throw IllegalArgumentException("Solo se permiten imágenes JPG, JPEG o PNG.")
        }

        val bytes = contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var totalBytes = 0
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                totalBytes += read
                require(totalBytes <= MAX_IMAGE_BYTES) {
                    "La imagen no puede superar los 5 MB."
                }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: throw IllegalArgumentException("No se pudo leer la imagen seleccionada.")

        val fileName = "${ownerId}_${UUID.randomUUID()}.$extension"
        val path = "$folder/$fileName"
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val response = githubApi.uploadFile(
            path = path,
            authorization = "Bearer $token",
            accept = "application/vnd.github+json",
            request = GithubFileRequest(
                message = "Upload $folder image $fileName",
                content = encoded,
            ),
        )

        return response.content?.downloadUrl
            ?: "https://raw.githubusercontent.com/Esthefany-Chavez/EvoriaImages/main/$path"
    }

    private companion object {
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    }
}

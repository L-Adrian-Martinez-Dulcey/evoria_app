package com.example.p3.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.p3.BuildConfig
import com.example.p3.data.api.GithubApiService
import com.example.p3.data.image.evoriaCdnUrl
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
        when (mimeType) {
            "image/jpeg", "image/jpg", "image/png" -> Unit
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

        val (optimizedBytes, extension) = optimizeForDisplay(bytes, mimeType)
        val fileName = "${ownerId}_${UUID.randomUUID()}.$extension"
        val path = "$folder/$fileName"
        val encoded = Base64.encodeToString(optimizedBytes, Base64.NO_WRAP)
        val response = githubApi.uploadFile(
            path = path,
            authorization = "Bearer $token",
            accept = "application/vnd.github+json",
            request = GithubFileRequest(
                message = "Upload $folder image $fileName",
                content = encoded,
            ),
        )

        return evoriaCdnUrl(path)
    }

    /** Reduce resolución y peso de fotos para acelerar la carga inicial. */
    private fun optimizeForDisplay(source: ByteArray, mimeType: String?): Pair<ByteArray, String> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(source, 0, source.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IllegalArgumentException("La imagen seleccionada no es válida.")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
        }
        val decoded = BitmapFactory.decodeByteArray(source, 0, source.size, options)
            ?: throw IllegalArgumentException("No se pudo procesar la imagen seleccionada.")
        val bitmap = decoded.scaleDown(MAX_IMAGE_DIMENSION)
        if (bitmap !== decoded) decoded.recycle()

        val preserveTransparency = mimeType == "image/png" && bitmap.hasAlpha()
        val format = if (preserveTransparency) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val extension = if (preserveTransparency) "png" else "jpg"
        val output = ByteArrayOutputStream()
        bitmap.compress(format, JPEG_QUALITY, output)
        bitmap.recycle()
        return output.toByteArray() to extension
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / sample > MAX_DECODE_DIMENSION || height / sample > MAX_DECODE_DIMENSION) {
            sample *= 2
        }
        return sample
    }

    private fun Bitmap.scaleDown(maxDimension: Int): Bitmap {
        val largestDimension = maxOf(width, height)
        if (largestDimension <= maxDimension) return this
        val ratio = maxDimension.toFloat() / largestDimension
        return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
    }

    private companion object {
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
        const val MAX_DECODE_DIMENSION = 2048
        const val MAX_IMAGE_DIMENSION = 1280
        const val JPEG_QUALITY = 82
    }
}

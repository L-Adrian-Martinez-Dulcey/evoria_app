package com.example.p3

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache

class EvoriaApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        // GitHub puede responder sin cabeceras de caché útiles; conservamos las
        // imágenes descargadas para que las siguientes aperturas sean locales.
        .respectCacheHeaders(false)
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("evoria_image_cache"))
                .maxSizeBytes(100L * 1024 * 1024)
                .build()
        }
        .build()
}

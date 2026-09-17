package com.example.p3.data.image

private const val GITHUB_RAW_PREFIX =
    "https://raw.githubusercontent.com/Esthefany-Chavez/EvoriaImages/main/"
private const val EVORIA_CDN_PREFIX =
    "https://cdn.jsdelivr.net/gh/Esthefany-Chavez/EvoriaImages@main/"

/** Usa el CDN para archivos del repositorio y deja intactas las URI locales. */
fun String?.fastImageUrl(): String? = when {
    this == null -> null
    startsWith(GITHUB_RAW_PREFIX) -> EVORIA_CDN_PREFIX + removePrefix(GITHUB_RAW_PREFIX)
    else -> this
}

fun evoriaCdnUrl(path: String): String = EVORIA_CDN_PREFIX + path

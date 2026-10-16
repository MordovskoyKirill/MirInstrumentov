package com.example.mirinstrumentov.data.remote

import io.github.jan.supabase.storage.storage

object StorageImages {

    // Названия бакетов из Supabase -> Storage.
    // Если все изображения в одном бакете, укажи здесь одинаковое имя.
    const val CATEGORY_BUCKET = "categories"
    const val TOOL_BUCKET = "tools"

    fun publicUrl(path: String?, bucket: String): String? {
        val objectPath = path?.trim()?.trimStart('/')?.takeIf { it.isNotBlank() }
            ?: return null

        if (objectPath.startsWith("https://")) return objectPath

        val pathInBucket = objectPath.removePrefix("$bucket/")
        return SupabaseClient.client.storage
            .from(bucket)
            .publicUrl(pathInBucket)
    }
}

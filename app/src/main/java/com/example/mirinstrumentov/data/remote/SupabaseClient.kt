package com.example.mirinstrumentov.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

object SupabaseClient {
    val client by lazy {
        createSupabaseClient(
            supabaseKey = "sb_publishable_2eiSilfqtaZassS6BIghaw_fTxEYVaV",
            supabaseUrl = "https://oomgqsleozuyuzewjmpq.supabase.co"
        ) {
            install(Auth)
        }
    }
}
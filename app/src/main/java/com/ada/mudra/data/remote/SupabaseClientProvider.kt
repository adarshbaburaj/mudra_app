package com.ada.mudra.data.remote

import com.ada.mudra.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Single Supabase client for the whole app. Returns null when no project is
 * configured in local.properties, so the app keeps working fully offline —
 * sync features simply stay dormant.
 *
 * Only the publishable (anon) key is used here; Row Level Security on the
 * server is what protects family data. The service role key must never
 * appear anywhere in this app.
 */
object SupabaseClientProvider {

    val client: SupabaseClient? by lazy {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_KEY.isBlank()) {
            null
        } else {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_KEY,
            ) {
                install(Auth)
                install(Postgrest)
                install(Realtime)
                install(Storage)
            }
        }
    }

    val isConfigured: Boolean get() = client != null
}

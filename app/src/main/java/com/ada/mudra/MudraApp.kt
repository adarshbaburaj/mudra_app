package com.ada.mudra

import android.app.Application
import com.ada.mudra.data.MudraRepository

class MudraApp : Application() {
    val repository: MudraRepository by lazy { MudraRepository(this) }
}

package com.nilemnod.mangareader.app.util.system

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService

val Context.powerManager: PowerManager
    get() = getSystemService()!!

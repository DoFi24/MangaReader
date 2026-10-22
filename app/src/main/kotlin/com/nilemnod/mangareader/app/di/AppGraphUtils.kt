package com.nilemnod.mangareader.app.di

import android.content.Context
import com.nilemnod.mangareader.core.metro.metroGraph

val Context.appGraph get() = metroGraph<AppGraph>()

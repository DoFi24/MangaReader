package com.nilemnod.mangareader.compat.tachiyomi

import android.app.Application
import eu.kanade.tachiyomi.AppInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import nl.adaptivity.xmlutil.serialization.XML
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.InjektScope
import com.nilemnod.mangareader.core.network.JavaScriptEngine as CoreJavaScriptEngine
import com.nilemnod.mangareader.core.network.NetworkHelper as CoreNetworkHelper

object TachiyomiCompat {

    fun install(
        application: Application,
        versionCode: Int,
        versionName: String,
        json: () -> Json,
        protoBuf: () -> ProtoBuf,
        xml: () -> XML,
        networkHelper: () -> CoreNetworkHelper,
        javaScriptEngine: () -> CoreJavaScriptEngine,
    ) {
        AppInfo.init(versionCode, versionName)
        Injekt = InjektScope(
            MetroInjektRegistrar(
                application = application,
                json = json,
                protoBuf = protoBuf,
                xml = xml,
                networkHelper = networkHelper,
                javaScriptEngine = javaScriptEngine,
            ),
        )
    }
}

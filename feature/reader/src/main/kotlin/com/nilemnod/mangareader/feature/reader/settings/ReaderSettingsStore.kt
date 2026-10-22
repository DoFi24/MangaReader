package com.nilemnod.mangareader.feature.reader.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ReaderSettingsStore {
    val settings: StateFlow<ReaderSettings>

    fun update(transform: (ReaderSettings) -> ReaderSettings)
}

class SharedPreferencesReaderSettingsStore(context: Context) : ReaderSettingsStore {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val state = MutableStateFlow(read())

    override val settings: StateFlow<ReaderSettings> = state.asStateFlow()

    override fun update(transform: (ReaderSettings) -> ReaderSettings) {
        val updated = transform(state.value)
        state.value = updated
        write(updated)
    }

    private fun read(): ReaderSettings {
        val default = ReaderSettings()
        return ReaderSettings(
            mode = prefs.getEnum(KEY_MODE, default.mode),
            fit = prefs.getEnum(KEY_FIT, default.fit),
            background = prefs.getEnum(KEY_BACKGROUND, default.background),
            brightness = prefs.getFloat(KEY_BRIGHTNESS, default.brightness),
            contrast = prefs.getFloat(KEY_CONTRAST, default.contrast),
            grayscale = prefs.getBoolean(KEY_GRAYSCALE, default.grayscale),
            invertColors = prefs.getBoolean(KEY_INVERT_COLORS, default.invertColors),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, default.keepScreenOn),
            showPageNumber = prefs.getBoolean(KEY_SHOW_PAGE_NUMBER, default.showPageNumber),
            invertTapZones = prefs.getBoolean(KEY_INVERT_TAP_ZONES, default.invertTapZones),
            doubleTapZoom = prefs.getBoolean(KEY_DOUBLE_TAP_ZOOM, default.doubleTapZoom),
            panelZoom = prefs.getBoolean(KEY_PANEL_ZOOM, default.panelZoom),
            panelNavigation = prefs.getBoolean(KEY_PANEL_NAVIGATION, default.panelNavigation),
            cropBorders = prefs.getBoolean(KEY_CROP_BORDERS, default.cropBorders),
            splitSpreads = prefs.getBoolean(KEY_SPLIT_SPREADS, default.splitSpreads),
            eInkMode = prefs.getBoolean(KEY_EINK_MODE, default.eInkMode),
            doubleTapScale = prefs.getFloat(KEY_DOUBLE_TAP_SCALE, default.doubleTapScale),
            volumeKeys = prefs.getBoolean(KEY_VOLUME_KEYS, default.volumeKeys),
            invertVolumeKeys = prefs.getBoolean(KEY_INVERT_VOLUME_KEYS, default.invertVolumeKeys),
        )
    }

    private fun write(settings: ReaderSettings) {
        prefs.edit()
            .putString(KEY_MODE, settings.mode.name)
            .putString(KEY_FIT, settings.fit.name)
            .putString(KEY_BACKGROUND, settings.background.name)
            .putFloat(KEY_BRIGHTNESS, settings.brightness)
            .putFloat(KEY_CONTRAST, settings.contrast)
            .putBoolean(KEY_GRAYSCALE, settings.grayscale)
            .putBoolean(KEY_INVERT_COLORS, settings.invertColors)
            .putBoolean(KEY_KEEP_SCREEN_ON, settings.keepScreenOn)
            .putBoolean(KEY_SHOW_PAGE_NUMBER, settings.showPageNumber)
            .putBoolean(KEY_INVERT_TAP_ZONES, settings.invertTapZones)
            .putBoolean(KEY_DOUBLE_TAP_ZOOM, settings.doubleTapZoom)
            .putBoolean(KEY_PANEL_ZOOM, settings.panelZoom)
            .putBoolean(KEY_PANEL_NAVIGATION, settings.panelNavigation)
            .putBoolean(KEY_CROP_BORDERS, settings.cropBorders)
            .putBoolean(KEY_SPLIT_SPREADS, settings.splitSpreads)
            .putBoolean(KEY_EINK_MODE, settings.eInkMode)
            .putFloat(KEY_DOUBLE_TAP_SCALE, settings.doubleTapScale)
            .putBoolean(KEY_VOLUME_KEYS, settings.volumeKeys)
            .putBoolean(KEY_INVERT_VOLUME_KEYS, settings.invertVolumeKeys)
            .apply()
    }

    private inline fun <reified E : Enum<E>> SharedPreferences.getEnum(key: String, default: E): E {
        val name = getString(key, null) ?: return default
        return enumValues<E>().firstOrNull { it.name == name } ?: default
    }

    private companion object {
        const val PREFS_NAME = "feature_reader_settings"
        const val KEY_MODE = "mode"
        const val KEY_FIT = "fit"
        const val KEY_BACKGROUND = "background"
        const val KEY_BRIGHTNESS = "brightness"
        const val KEY_CONTRAST = "contrast"
        const val KEY_GRAYSCALE = "grayscale"
        const val KEY_INVERT_COLORS = "invert_colors"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_SHOW_PAGE_NUMBER = "show_page_number"
        const val KEY_INVERT_TAP_ZONES = "invert_tap_zones"
        const val KEY_DOUBLE_TAP_ZOOM = "double_tap_zoom"
        const val KEY_PANEL_ZOOM = "panel_zoom"
        const val KEY_PANEL_NAVIGATION = "panel_navigation"
        const val KEY_CROP_BORDERS = "crop_borders"
        const val KEY_SPLIT_SPREADS = "split_spreads"
        const val KEY_EINK_MODE = "eink_mode"
        const val KEY_DOUBLE_TAP_SCALE = "double_tap_scale"
        const val KEY_VOLUME_KEYS = "volume_keys"
        const val KEY_INVERT_VOLUME_KEYS = "invert_volume_keys"
    }
}

package com.nilemnod.mangareader.app.domain.ui.model

import com.nilemnod.mangareader.i18n.MR
import dev.icerock.moko.resources.StringResource

enum class TimeFormat(val titleRes: StringResource) {
    FOLLOW_LANGUAGE(MR.strings.follow_language),
    FOLLOW_SYSTEM(MR.strings.follow_system),
    HOURS_24(MR.strings.time_24_hours),
    HOURS_12(MR.strings.time_12_hours),
}

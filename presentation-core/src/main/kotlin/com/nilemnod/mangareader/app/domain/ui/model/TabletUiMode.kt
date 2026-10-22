package com.nilemnod.mangareader.app.domain.ui.model

import com.nilemnod.mangareader.i18n.MR
import dev.icerock.moko.resources.StringResource

enum class TabletUiMode(val titleRes: StringResource) {
    AUTOMATIC(MR.strings.automatic_background),
    ALWAYS(MR.strings.lock_always),
    LANDSCAPE(MR.strings.landscape),
    NEVER(MR.strings.lock_never),
}

package com.nilemnod.mangareader.feature.extensions.preference

import com.nilemnod.mangareader.i18n.MR
import dev.icerock.moko.resources.StringResource

enum class InstallerType(val titleRes: StringResource, val requiresSystemPermission: Boolean) {
    LEGACY(MR.strings.ext_installer_legacy, true),
    PACKAGEINSTALLER(MR.strings.ext_installer_packageinstaller, true),
    SHIZUKU(MR.strings.ext_installer_shizuku, false),
    PRIVATE(MR.strings.ext_installer_private, false),
}

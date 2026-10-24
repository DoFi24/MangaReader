package com.nilemnod.mangareader.feature.extensions.preference

import android.content.Context
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import com.nilemnod.mangareader.core.common.preference.getEnum
import com.nilemnod.mangareader.feature.extensions.installer.hasMiuiPackageInstaller
import com.nilemnod.mangareader.feature.extensions.installer.isShizukuInstalled
import kotlinx.coroutines.CoroutineScope

class ExtensionInstallerPreference(
    private val context: Context,
    preferenceStore: PreferenceStore,
) : Preference<InstallerType> {

    private val basePref = preferenceStore.getEnum(key(), defaultValue())

    override fun key() = "extension_installer"

    val entries get() = InstallerType.entries.run {
        if (context.hasMiuiPackageInstaller) {
            filter { it != InstallerType.PACKAGEINSTALLER }
        } else {
            toList()
        }
    }

    override fun defaultValue() = if (context.hasMiuiPackageInstaller) {
        InstallerType.LEGACY
    } else {
        InstallerType.PACKAGEINSTALLER
    }

    private fun check(value: InstallerType): InstallerType {
        when (value) {
            InstallerType.PACKAGEINSTALLER -> {
                if (context.hasMiuiPackageInstaller) return InstallerType.LEGACY
            }
            InstallerType.SHIZUKU -> {
                if (!context.isShizukuInstalled) return defaultValue()
            }
            else -> {}
        }
        return value
    }

    override fun get(): InstallerType {
        val value = basePref.get()
        val checkedValue = check(value)
        if (value != checkedValue) {
            basePref.set(checkedValue)
        }
        return checkedValue
    }

    override fun set(value: InstallerType) {
        basePref.set(check(value))
    }

    override fun isSet() = basePref.isSet()

    override fun delete() = basePref.delete()

    override fun changes() = basePref.changes()

    override fun stateIn(scope: CoroutineScope) = basePref.stateIn(scope)
}

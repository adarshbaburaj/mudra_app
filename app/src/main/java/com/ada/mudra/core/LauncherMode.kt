package com.ada.mudra.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * Optional home-screen mode. We only enable/disable our own activity-alias and
 * open the system Home chooser; Android never lets an app force itself to be
 * the default launcher, and we don't try.
 */
object LauncherMode {

    private const val ALIAS = "com.ada.mudra.SeniorLauncherAlias"

    fun setEnabled(context: Context, enabled: Boolean) {
        val alias = ComponentName(context.packageName, ALIAS)
        context.packageManager.setComponentEnabledSetting(
            alias,
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
        if (enabled) {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}

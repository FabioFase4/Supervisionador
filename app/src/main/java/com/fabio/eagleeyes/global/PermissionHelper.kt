package com.fabio.eagleeyes.global

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.os.Process
import android.provider.Settings

object PermissionHelper {

    fun temPermissaoUso(context: Context): Boolean {
        val appOs = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOs.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expected = ComponentName(context, service)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(expected.flattenToString()) || 
               enabled.contains(expected.flattenToShortString())
    }
}

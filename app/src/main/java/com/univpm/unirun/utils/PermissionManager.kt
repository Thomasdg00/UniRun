package com.univpm.unirun.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val NOTIFICATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }

    val BACKGROUND_LOCATION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    } else {
        null
    }

    fun hasLocationPermissions(context: Context): Boolean {
        return LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && NOTIFICATION_PERMISSION != null) {
            return ContextCompat.checkSelfPermission(
                context,
                NOTIFICATION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun getMissingPermissions(context: Context): Array<String> {
        val missing = mutableListOf<String>()
        LOCATION_PERMISSIONS.forEach {
            if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED) {
                missing.add(it)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && NOTIFICATION_PERMISSION != null) {
            if (ContextCompat.checkSelfPermission(context, NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                missing.add(NOTIFICATION_PERMISSION)
            }
        }
        return missing.toTypedArray()
    }

    fun shouldShowRationale(activity: Activity): Boolean {
        var shouldShow = false
        LOCATION_PERMISSIONS.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                shouldShow = true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && NOTIFICATION_PERMISSION != null) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, NOTIFICATION_PERMISSION)) {
                shouldShow = true
            }
        }
        return shouldShow
    }
}

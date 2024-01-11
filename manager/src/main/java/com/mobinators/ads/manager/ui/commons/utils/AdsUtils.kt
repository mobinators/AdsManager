package com.mobinators.ads.manager.ui.commons.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.text.TextUtils
import com.applovin.sdk.AppLovinSdk
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException


object AdsUtils {
    fun isPackageInstalled(context: Context, pkgName: String): Boolean {
        return try {
            val packageName: PackageManager = context.packageManager
            packageName.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES)
            true
        } catch (error: PackageManager.NameNotFoundException) {
            logException("Failed to load meta-data, NameNotFound: ${error.localizedMessage}")
            false
        }
    }

    fun isContainPkg(pkgName: String): Boolean {
        if (TextUtils.isEmpty(pkgName)) {
            return false
        }
        return pkgName.contains("play.google.com/")
    }

    fun getPackageName(url: String): String {
        val pkg = url.split("=")
        return pkg[0]
    }

    fun isExistApp(context: Context, packageName: String): Boolean {
        val packageManager: PackageManager = context.packageManager
        return try {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            true
        } catch (error: Exception) {
            false
        }
    }

    fun openPlayStore(context: Context, packageName: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(AdsConstants.PLAY_STORE_URL_1 + packageName)
                )
            )
        } catch (error: android.content.ActivityNotFoundException) {
            logException("Error : ${error.localizedMessage}")
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(AdsConstants.PLAY_STORE_URL + packageName)
                )
            )
        }
    }

    fun findIntegerValueInMap(key: String, map: Map<String, Int>): Int {
        val updateKey = key.toUpperCase(java.util.Locale.ROOT)
        var value = 1
        if (map == null) {
            logD("findValueInMap: key : $key")
        } else {
            for (entry in map.entries) {
                logD("findValueInMap:  ${entry.key}")
                if ((entry.key == key.lowercase())) {
                    value = entry.value
                    logD("findValueInMap:  ${value}")
                    break
                }
            }
        }
        return value
    }


    fun isOnline(context: Context): Boolean {
        return try {
            val manager: ConnectivityManager? =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val netInfo = manager!!.activeNetworkInfo
            netInfo != null && netInfo.isConnectedOrConnecting
        } catch (error: Exception) {
            logException("Internet Connect is disconnected")
            false
        }
    }

    fun maxTestAds(activity: Activity) {
        AppLovinSdk.getInstance(activity).showMediationDebugger()
    }
}
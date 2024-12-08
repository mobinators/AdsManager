package com.mobinators.ads.manager.ui.commons.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.applovin.sdk.AppLovinSdk
import pak.developer.app.managers.extensions.logException


object AdsUtils {
    fun isPackageInstalled(context: Context, pkgName: String): Boolean {
        return try {
            val packageName: PackageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageName.getPackageInfo(
                    pkgName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
                )
            } else {
                packageName.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES)
            }
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


    fun isAppRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = context.packageName
        val runningProcesses = activityManager.runningAppProcesses
        runningProcesses.forEach { processInfo ->
            if (processInfo.processName == packageName) {
                return true
            }
        }
        return false
    }
    fun openPlayStore(context: Context, packageName: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(AdsConstants.PLAY_STORE_URL_1 + packageName)
                )
            )
        } catch (error: ActivityNotFoundException) {
            logException("Error : ${error.localizedMessage}")
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(AdsConstants.PLAY_STORE_URL + packageName)
                )
            )
        }
    }
    fun openAmazonStore(context: Context, packageName: String){
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(AdsConstants.AMAZON_STORE_URL + packageName)
            )
        )
    }
    fun openPlayStore(packageName: String):Intent{
        return try {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(AdsConstants.PLAY_STORE_URL_1 + packageName)
            )
        }catch (error: ActivityNotFoundException){
            logException("Error : ${error.localizedMessage}")
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(AdsConstants.PLAY_STORE_URL + packageName)
            )
        }
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
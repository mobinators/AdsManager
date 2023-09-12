package com.mobinators.ads.managers.applications

import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.managers.BuildConfig
import com.mobinators.ads.manager.extensions.updateManifest
import com.mobinators.ads.manager.ui.commons.listener.FetchRemoteCallback
import pak.developer.app.managers.extensions.logD

class AdsManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        logD("Debug Mode : ${BuildConfig.DEBUG}  : ${0 != applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE}")
        AdsApplication.getValueFromConfig(
            FirebaseRemoteConfig.getInstance(),
            this,
            object : FetchRemoteCallback {
                override fun onFetchValuesSuccess() {
                    logD("onFetchValuesSuccess")
                }

                override fun onFetchValuesFailed() {
                    logD("onFetchValuesFailed")
                }

                override fun onUpdateSuccess(appId: String, maxAppId: String) {
                    logD("onUpdateSuccess : App Id : $appId  : MAX App Id: $maxAppId")
                    updateManifest(appId = appId, maxAppId = maxAppId)
                }
            })
        AdsApplication.setAnalytics(FirebaseAnalytics.getInstance(this))
    }

}
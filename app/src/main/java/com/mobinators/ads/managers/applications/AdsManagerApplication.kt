package com.mobinators.ads.managers.applications

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.multidex.MultiDex
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.managers.BuildConfig
import com.mobinators.ads.manager.extensions.updateManifest
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.listener.FetchRemoteCallback
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

class AdsManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
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
                    MediationAdInterstitial.loadInterstitialAds(
                        this@AdsManagerApplication.applicationContext,
                        false,
                        object : MediationAdInterstitial.LoadCallback {
                            override fun onAdsLoaded() {
                                logD("Interstitial Ads Loaded")
                            }

                            override fun onAdsError(error: String) {
                                logException(error)
                            }

                            override fun onAdsOff() {
                                logD("Interstitial Ads is Off")
                            }

                        })
                    MediationRewardedInterstitialAd.loadRewardedInterstitialAds(
                        applicationContext,
                        false,
                        object : MediationRewardedInterstitialAd.RewardedLoadAds {
                            override fun onAdsLoaded() {
                                logD("Reward Interstitial Ads Loaded")
                            }

                            override fun onAdsOff() {
                                logD("Reward Interstitial Ads is off")
                            }

                            override fun onAdsError(error: String) {
                                logException(error)
                            }

                        })
                }
            })
        AdsApplication.setAnalytics(FirebaseAnalytics.getInstance(this))
    }


}
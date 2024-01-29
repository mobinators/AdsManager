package com.mobinators.ads.managers.applications

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.multidex.MultiDex
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.managers.BuildConfig
import com.mobinators.ads.manager.extensions.updateManifest
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.listener.FetchRemoteCallback
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

class AdsManagerApplication : Application() {
    private var isAppInForeground = false

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        logD("Debug Mode : ${BuildConfig.DEBUG}  : ${0 != applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE}")
        AdsApplication.getValueFromConfig(
            FirebaseRemoteConfig.getInstance(),
            this,
            AdsConstants.GOOGLE_PLAY_STORE,
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

                            override fun onAdsError(errorState: AdsErrorState) {
                                when (errorState) {
                                    AdsErrorState.NETWORK_OFF -> logD("Interstitial Ads : Internet Off")
                                    AdsErrorState.APP_PURCHASED -> logD("Interstitial Ads : You have Purchased your app")
                                    AdsErrorState.ADS_STRATEGY_WRONG -> logD("Interstitial Ads : Ads Strategy wrong")
                                    AdsErrorState.ADS_ID_NULL -> logD("Interstitial Ads : Ads Is Null found")
                                    AdsErrorState.TEST_ADS_ID -> logD("Interstitial Ads : Test Id found in released mode your app")
                                    AdsErrorState.ADS_LOAD_FAILED -> logD("Interstitial Ads : Ads  load failed")
                                    AdsErrorState.ADS_DISMISS -> logD("Interstitial Ads : Ads Dismiss")
                                    AdsErrorState.ADS_DISPLAY_FAILED -> logD("Interstitial Ads : Display Ads failed")
                                    AdsErrorState.ADS_IMPRESS -> logD("Interstitial Ads : Ads Impress Mode")
                                }
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
                    MediationOpenAd.loadAppOpenAds(
                        applicationContext,
                        false,
                        object : MediationOpenAd.AdsLoadedCallback {
                            override fun onAdsOff() {
                                logD("AppOpen Ads is off")
                            }

                            override fun onAdsLoaded() {
                                logD("AppOpen Ads onAdsLoaded")
                            }

                            override fun onAdsError(errorState: AdsErrorState) {
                                when (errorState) {
                                    AdsErrorState.NETWORK_OFF -> logD("Internet Off")
                                    AdsErrorState.APP_PURCHASED -> logD("You have Purchased your app")
                                    AdsErrorState.ADS_STRATEGY_WRONG -> logD("App Open Ads Strategy wrong")
                                    AdsErrorState.ADS_ID_NULL -> logD("App Open Ads Is Null found")
                                    AdsErrorState.TEST_ADS_ID -> logD("App Open Test Id found in released mode your app")
                                    AdsErrorState.ADS_LOAD_FAILED -> logD("App Open Ads  load failed")
                                    AdsErrorState.ADS_DISMISS -> logD("App Open Ads Dismiss")
                                    AdsErrorState.ADS_DISPLAY_FAILED -> logD("App Open Display Ads failed")
                                    AdsErrorState.ADS_IMPRESS -> logD("App Open Ads Impress Mode")
                                }
                            }
                        })

                    MediationNativeAds.loadNativeAds(
                        applicationContext,
                        false,
                        object : MediationNativeAds.NativeLoadAdsCallback {
                            override fun onAdsOff() {
                                logD("Native Ads Loaded off")
                            }

                            override fun onAdsLoaded() {
                                logD("Native Ads Loaded")
                            }

                            override fun onAdsError(errorState: AdsErrorState) {
                                when (errorState) {
                                    AdsErrorState.NETWORK_OFF -> logD("Native Ads Internet Off")
                                    AdsErrorState.APP_PURCHASED -> logD("Native AdsYou have Purchased your app")
                                    AdsErrorState.ADS_STRATEGY_WRONG -> logD("Native Ads Strategy wrong")
                                    AdsErrorState.ADS_ID_NULL -> logD("Native Ads  Is Null found")
                                    AdsErrorState.TEST_ADS_ID -> logD("Native Test Id found in released mode your app")
                                    AdsErrorState.ADS_LOAD_FAILED -> logD("Native Ads  load failed")
                                    AdsErrorState.ADS_DISMISS -> logD("Native Ads Dismiss")
                                    AdsErrorState.ADS_DISPLAY_FAILED -> logD("Native Display Ads failed")
                                    AdsErrorState.ADS_IMPRESS -> logD("Native  Ads Impress Mode")
                                }
                            }
                        })
                }
            })
        AdsApplication.setAnalytics(FirebaseAnalytics.getInstance(this))
    }
}
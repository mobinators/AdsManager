package com.mobinators.ads.managers.applications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.multidex.MultiDex
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.managers.BuildConfig
import com.mobinators.ads.manager.extensions.updateManifest
import com.mobinators.ads.manager.ui.commons.enums.AdsLoadingState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.listener.FetchRemoteCallback
import com.mobinators.ads.manager.ui.commons.models.AdsModel
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AnalyticsManager
import com.mobinators.ads.managers.R
import pak.developer.app.managers.extensions.logD

class AdsManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        logD("Debug Mode : ${BuildConfig.DEBUG}  : ${0 != applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE}")
        AnalyticsManager.getInstance().setAnalytics(FirebaseAnalytics.getInstance(this))
        AnalyticsManager.getInstance().setUserId(resources.getString(R.string.app_name))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId =
                getString(com.mobinators.ads.manager.R.string.default_notification_channel_id)
            val channelName =
                getString(com.mobinators.ads.manager.R.string.default_notification_channel_id)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
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

                override fun onUpdateSuccess(adsModel: AdsModel) {
                    logD("onUpdateSuccess : App Id : ${adsModel.admobAppID}  : MAX App Id: ${adsModel.maxAppId}")
                    updateManifest(adsModel = adsModel)
                    MediationAdInterstitial.loadInterstitialAds(
                        this@AdsManagerApplication.applicationContext,
                        false,
                        object : MediationAdInterstitial.LoadCallback {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                                AnalyticsManager.getInstance().setAnalyticsEvent(
                                    resources.getString(R.string.app_name),
                                    "InterstitialAds",
                                    adsLoadingState.name
                                )
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("Interstitial Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("Interstitial Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("Interstitial Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("Interstitial Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("Interstitial Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("Interstitial Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("Interstitial Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("Interstitial Ads : Ads  load failed")
                                }
                            }

                        })
                    MediationRewardedInterstitialAd.loadRewardedInterstitialAds(
                        applicationContext,
                        false,
                        object : MediationRewardedInterstitialAd.RewardedLoadAds {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                                AnalyticsManager.getInstance().setAnalyticsEvent(
                                    resources.getString(R.string.app_name),
                                    "RewardInterstitialAds",
                                    adsLoadingState.name
                                )
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("RewardInterstitialAds Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("RewardInterstitialAds Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("RewardInterstitialAds Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("RewardInterstitialAds Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("RewardInterstitialAds Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("RewardInterstitialAds Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("RewardInterstitialAds Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("RewardInterstitialAds Ads : Ads  load failed")
                                }
                            }

                        })
                    MediationOpenAd.loadAppOpenAds(
                        applicationContext,
                        false,
                        object : MediationOpenAd.AdsLoadedCallback {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                                AnalyticsManager.getInstance().setAnalyticsEvent(
                                    resources.getString(R.string.app_name),
                                    "AppOpenAds",
                                    adsLoadingState.name
                                )
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("AppOpenAds Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("AppOpenAds Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("AppOpenAds Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("AppOpenAds Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("AppOpenAds Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("AppOpenAds Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("AppOpenAds Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("AppOpenAds Ads : Ads  load failed")
                                }
                            }
                        })

                    MediationNativeAds.loadNativeAds(
                        applicationContext,
                        false,
                        object : MediationNativeAds.NativeLoadAdsCallback {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                                AnalyticsManager.getInstance().setAnalyticsEvent(
                                    resources.getString(R.string.app_name),
                                    "NativeAds",
                                    adsLoadingState.name
                                )
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("NativeAds Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("NativeAds Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("NativeAds Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("NativeAds Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("NativeAds Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("NativeAds Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("NativeAds Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("NativeAds Ads : Ads  load failed")
                                }
                            }
                        })
                }
            })
    }
}
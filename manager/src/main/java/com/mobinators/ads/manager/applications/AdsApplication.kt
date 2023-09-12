package com.mobinators.ads.manager.applications

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.ui.commons.listener.FetchRemoteCallback
import com.mobinators.ads.manager.ui.commons.models.AdsModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException
import pak.developer.app.managers.extensions.preferenceUtils

object AdsApplication : Application() {
    private var admobLimit = "false"
    private var handler = Handler()
    var applyLimitOnAdmob = false
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var onFetchRemoteCallbackListener: FetchRemoteCallback? = null
    private var adsModel: AdsModel? = null
    fun getAdsModel(): AdsModel? {
        return adsModel
    }

    fun isAdmobInLimit(): Boolean {
        return admobLimit == "true" || admobLimit == "on" || admobLimit == "1" || admobLimit == "Yes" || admobLimit == "TRUE"
    }


    private fun initMediation(context: Context) {
        val testDeviceIds: List<String> = mutableListOf("49CB5184DFD9ACB6581265EA7DF47D8A")
        val configuration: RequestConfiguration =
            RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(context) {
            val statusMap: Map<String, AdapterStatus> = it.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status: AdapterStatus? = statusMap[adapterClass]
                logD("Adapter name : $adapterClass , Description : ${status!!.description} , Latency : ${status!!.latency}")
            }
        }
    }

    private fun initMaxMediation(context: Context) {
        AppLovinSdk.getInstance(context).mediationProvider = AppLovinMediationProvider.MAX
        AppLovinSdk.initializeSdk(context) {}
    }

    fun getValueFromConfig(
        firebaseConfig: FirebaseRemoteConfig, context: Context, listener: FetchRemoteCallback
    ) {
        AdsConstants.testMode = 0 != context.applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        logD("Test Mode: ${AdsConstants.testMode}")
        logD("Test pre Build ----------------------> wow it works <---------------------------")
        this.onFetchRemoteCallbackListener = listener
        val setting: FirebaseRemoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3).build()
        firebaseConfig.setConfigSettingsAsync(setting)
        firebaseConfig.setDefaultsAsync(R.xml.remote_config_default_values)
        handler.postDelayed({
            fetchDataFromRemoteConfig(firebaseConfig)
        }, 0)
        if (verifyInstallerId(context).not()) {

        }
        initMediation(context)
        initMaxMediation(context)
    }

    private fun fetchDataFromRemoteConfig(remoteConfig: FirebaseRemoteConfig) {
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                logD("onComplete : Success")
                this.onFetchRemoteCallbackListener!!.onFetchValuesSuccess()
                updateData(remoteConfig)
            } else {
                logD("onComplete : Failed")
                this.onFetchRemoteCallbackListener!!.onFetchValuesFailed()
                updateData(remoteConfig)
            }
        }
    }

    private fun verifyInstallerId(context: Context): Boolean {
        val validInstallers: List<String> =
            ArrayList(mutableListOf("com.android.vending", "com.google.android.feedback"))
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        return installer != null && validInstallers.contains(installer)
    }

    private fun updateData(remoteConfig: FirebaseRemoteConfig) {
        adsModel = AdsModel().apply {
            this.admobAppID = remoteConfig.getString(AdsConstants.ADMOB_APP_ID_KEY)
            this.maxAppId = remoteConfig.getString(AdsConstants.APPLOVIN_APP_ID_KEY)
            this.admobInterstitialID = remoteConfig.getString(AdsConstants.ADMOB_INTERSTITIAL_ID_KEY)
            this.admobNativeID = remoteConfig.getString(AdsConstants.ADMOB_NATIVE_ID_KEY)
            this.admobBannerID = remoteConfig.getString(AdsConstants.ADMOB_BANNER_ID_KEY)
            this.admobOpenAdID = remoteConfig.getString(AdsConstants.ADMOB_OPEN_AD_ID_KEY)
            this.admobRewardedID = remoteConfig.getString(AdsConstants.ADMOB_REWARD_AD_ID_KEY)
            this.admobRewardedInterstitialID = remoteConfig.getString(AdsConstants.ADMOB_REWARD_INTERSTITIAL_AD_ID_KEY)
            this.strategy = remoteConfig.getLong(AdsConstants.ADS_STRATEGY)
            this.maxBannerID = remoteConfig.getString(AdsConstants.MAX_BANNER_ADS_ID_KEY)
            this.maxInterstitialID = remoteConfig.getString(AdsConstants.MAX_INTERSTITIAL_ADS_ID_KEY)
            this.maxNativeID = remoteConfig.getString(AdsConstants.MAX_Native_ADS_ID_KEY)
            this.maxRewardedID = remoteConfig.getString(AdsConstants.MAX_REWARD_ADS_ID_KEY)
            this.isAppOpenAdd = remoteConfig.getBoolean(AdsConstants.ADMOB_OPEN_AD_ENABLE_KEY)
            this.maxAppOpenID = remoteConfig.getString(AdsConstants.MAX_APP_OPEN_ADS_ID_KEY)
            this.admobMediation = remoteConfig.getBoolean(AdsConstants.ADMOB_MEDIATION_KEY)
            this.admobMediationBannerId = remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_BANNER_ID_KEY)
            this.admobMediationInterstitialId = remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_INTERSTITIAL_ID_KEY)
            this.admobMediationNativeId = remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_NATIVE_ID_KEY)
            this.admobMediationRewardedId = remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_REWARDED_ID_KEY)
        }
        checkOpenAddIsEnable(remoteConfig)
        logD("Ads Strategy : ${remoteConfig.getLong(AdsConstants.ADS_STRATEGY)}")
        logD("AdsModel Detail : $adsModel")
        this.onFetchRemoteCallbackListener!!.onUpdateSuccess(
            appId = adsModel!!.admobAppID!!,
            maxAppId = adsModel!!.maxAppId!!
        )
        AdsConstants.isInit = true
    }

    fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    fun setTime(context: Context, key: String) {
        context.preferenceUtils.setIntegerValue(key, System.currentTimeMillis().toInt())
    }

    fun canShow(context: Context, key: String, delaySecond: Long): Boolean {
        return System.currentTimeMillis() >= context.preferenceUtils.getIntegerValue(key) + delaySecond * 1000
    }

    fun setAnalytics(analytics: FirebaseAnalytics) {
        this.firebaseAnalytics = analytics
    }

    fun getFirebaseAnalytics(): FirebaseAnalytics? {
        return this.firebaseAnalytics
    }

    fun analyticsEvent(key: String, value: String) {
        try {
            if (key.isEmpty() || key.isBlank()) {
                logD("Your Firebase Analytics Event key is null or blank")
                return
            }
            this.firebaseAnalytics?.let {
                it.logEvent(AdsConstants.FIREBASE_ANALYTICS_KEY, Bundle().apply {
                    putString(key, value)
                })
            } ?: logD("calling setAnalytics method first ")
        } catch (error: Exception) {
            logException("Firebase Analytics Error : ${error.localizedMessage}")
        }

    }

    private fun checkOpenAddIsEnable(remoteConfig: FirebaseRemoteConfig) {
        AdsConstants.isAppOpenAdEnable =
            remoteConfig.getBoolean(AdsConstants.ADMOB_OPEN_AD_ENABLE_KEY)

    }
}
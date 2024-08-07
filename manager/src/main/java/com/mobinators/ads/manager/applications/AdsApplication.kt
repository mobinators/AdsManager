package com.mobinators.ads.manager.applications

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mbridge.msdk.MBridgeConstans
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.ui.commons.listener.FetchRemoteCallback
import com.mobinators.ads.manager.ui.commons.models.AdsModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.preferenceUtils

object AdsApplication : Application() {
    private var admobLimit = "false"
    private var handler = Handler(Looper.getMainLooper())
    var applyLimitOnAdmob = false
    private var onFetchRemoteCallbackListener: FetchRemoteCallback? = null
    private var adsModel: AdsModel? = null
    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    fun getAdsModel(): AdsModel? {
        return adsModel
    }

    fun isAdmobInLimit(): Boolean {
        return admobLimit == "true" || admobLimit == "on" || admobLimit == "1" || admobLimit == "Yes" || admobLimit == "TRUE"
    }

    private fun initMediation(context: Context) {

        backgroundScope.launch {
            val sdk = MBridgeSDKFactory.getMBridgeSDK()
            sdk.setConsentStatus(context, MBridgeConstans.IS_SWITCH_ON)
            sdk.setDoNotTrackStatus(false)
            val testDeviceIds: List<String> = mutableListOf("49CB5184DFD9ACB6581265EA7DF47D8A")
            val configuration: RequestConfiguration =
                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            MobileAds.setRequestConfiguration(configuration)
            MobileAds.initialize(context) {
                val statusMap: Map<String, AdapterStatus> = it.adapterStatusMap
                for (adapterClass in statusMap.keys) {
                    val status: AdapterStatus? = statusMap[adapterClass]
                    status?.let { state ->
                        logD("Adapter name : $adapterClass , Description : ${state.description} , Latency : ${state.latency}")
                    }
                }
            }
        }
    }



    fun getValueFromConfig(
        firebaseConfig: FirebaseRemoteConfig,
        context: Context,
        storeId: Int,
        listener: FetchRemoteCallback
    ) {
        AdsConstants.selectedStore = storeId
        AdsConstants.testMode =
            0 != context.applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        logD("Test Mode: ${AdsConstants.testMode}")
        logD("Test pre Build ----------------------> wow it works <---------------------------")
        this.onFetchRemoteCallbackListener = listener
        val setting: FirebaseRemoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3).build()
        firebaseConfig.setConfigSettingsAsync(setting)
        firebaseConfig.setDefaultsAsync(R.xml.remote_config_default_values)
        handler.postDelayed({ fetchDataFromRemoteConfig(firebaseConfig) }, 0)
        initMediation(context)
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

    private fun updateData(remoteConfig: FirebaseRemoteConfig) {
        adsModel = AdsModel().apply {
            this.admobAppID = remoteConfig.getString(AdsConstants.ADMOB_APP_ID_KEY)
            this.maxAppId = remoteConfig.getString(AdsConstants.APPLOVIN_APP_ID_KEY)
            this.admobInterstitialID =
                remoteConfig.getString(AdsConstants.ADMOB_INTERSTITIAL_ID_KEY)
            this.admobNativeID = remoteConfig.getString(AdsConstants.ADMOB_NATIVE_ID_KEY)
            this.admobBannerID = remoteConfig.getString(AdsConstants.ADMOB_BANNER_ID_KEY)
            this.collapseBannerID =
                remoteConfig.getString(AdsConstants.ADMOB_COLLAPSE_BANNER_ID_KEY)
            this.admobOpenAdID = remoteConfig.getString(AdsConstants.ADMOB_OPEN_AD_ID_KEY)
            this.admobRewardedID = remoteConfig.getString(AdsConstants.ADMOB_REWARD_AD_ID_KEY)
            this.admobRewardedInterstitialID =
                remoteConfig.getString(AdsConstants.ADMOB_REWARD_INTERSTITIAL_AD_ID_KEY)
            this.strategy = remoteConfig.getLong(AdsConstants.ADS_STRATEGY)
            this.maxBannerID = remoteConfig.getString(AdsConstants.MAX_BANNER_ADS_ID_KEY)
            this.maxInterstitialID =
                remoteConfig.getString(AdsConstants.MAX_INTERSTITIAL_ADS_ID_KEY)
            this.maxNativeID = remoteConfig.getString(AdsConstants.MAX_Native_ADS_ID_KEY)
            this.maxRewardedID = remoteConfig.getString(AdsConstants.MAX_REWARD_ADS_ID_KEY)
            this.isAppOpenAdd = remoteConfig.getBoolean(AdsConstants.ADMOB_OPEN_AD_ENABLE_KEY)
            this.maxAppOpenID = remoteConfig.getString(AdsConstants.MAX_APP_OPEN_ADS_ID_KEY)
            this.admobMediation = remoteConfig.getBoolean(AdsConstants.ADMOB_MEDIATION_KEY)
            this.admobMediationBannerId =
                remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_BANNER_ID_KEY)
            this.admobMediationInterstitialId =
                remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_INTERSTITIAL_ID_KEY)
            this.admobMediationNativeId =
                remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_NATIVE_ID_KEY)
            this.admobMediationRewardedId =
                remoteConfig.getString(AdsConstants.ADMOB_MEDIATION_REWARDED_ID_KEY)
            this.isRateUsDialog = remoteConfig.getLong(AdsConstants.RATE_US_DIALOG_COUNT_KEY)
        }
        checkOpenAddIsEnable(remoteConfig)
        logD("Ads Strategy : ${remoteConfig.getLong(AdsConstants.ADS_STRATEGY)} :")
        logD(" Store Strategy : ${remoteConfig.getLong(AdsConstants.STORE_STRATEGY_KEY)}")
        this.onFetchRemoteCallbackListener!!.onUpdateSuccess(
           adsModel = adsModel!!
        )
        AdsConstants.isAdPreloadEnable = when ((adsModel?.strategy ?: 0).toInt()) {
            AdsConstants.ADS_OFF -> false
            else -> true
        }
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
    private fun checkOpenAddIsEnable(remoteConfig: FirebaseRemoteConfig) {
        AdsConstants.isAppOpenAdEnable =
            remoteConfig.getBoolean(AdsConstants.ADMOB_OPEN_AD_ENABLE_KEY)
    }
}
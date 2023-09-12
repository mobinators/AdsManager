package com.mobinators.ads.manager.ui.commons.utils

object AdsConstants {
    const val TEST_ADMOB_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"
    const val TEST_ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_ADMOB_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_ADMOB_OPEN_APP_ID = "ca-app-pub-3940256099942544/3419835294"
    const val TEST_ADMOB_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_MAX_INTERSTITIAL_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_BANNER_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_REWARD_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_Native_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_APP_OPEN_ADS_ID = "YOUR_AD_UNIT_ID"
    const val PLAY_STORE_URL = "http://play.google.com/store/apps/details?id="
    const val PLAY_STORE_URL_1 = "market://details?id="
    const val ADS_OFF = 0
    const val AD_MOB_MEDIATION = 1
    const val AD_MOB = 2
    const val MAX_MEDIATION = 3
    const val ADMOB_BANNER_ID_KEY = "ADMOB_BANNER_ADS_ID"
    const val ADMOB_INTERSTITIAL_ID_KEY = "ADMOB_INTERSTITIAL_ADS_ID"
    const val ADMOB_NATIVE_ID_KEY = "ADMOB_NATIVE_ADS_ID"
    const val ADMOB_APP_ID_KEY = "ADMOB_APP_ID"
    const val APPLOVIN_APP_ID_KEY = "APPLOVIN_APP_ID"
    const val ADMOB_REWARD_AD_ID_KEY = "ADMOB_REWARD_ADS_ID"
    const val ADMOB_OPEN_AD_ID_KEY = "ADMOB_APP_OPEN_ADS_ID"
    const val ADMOB_REWARD_INTERSTITIAL_AD_ID_KEY = "ADMOB_REWARD_INTERSTITIAL_ADS_ID"
    const val MAX_BANNER_ADS_ID_KEY = "MAX_BANNER_ADS_ID"
    const val MAX_INTERSTITIAL_ADS_ID_KEY = "MAX_INTERSTITIAL_ADS_ID"
    const val MAX_REWARD_ADS_ID_KEY = "MAX_REWARD_ADS_ID"
    const val MAX_Native_ADS_ID_KEY = "MAX_Native_ADS_ID"
    const val MAX_APP_OPEN_ADS_ID_KEY = "MAX_APP_OPEN_ADS_ID"
    const val ADMOB_MEDIATION_BANNER_ID_KEY = "ADMOB_MEDIATION_BANNER_ID"
    const val ADMOB_MEDIATION_NATIVE_ID_KEY = "ADMOB_MEDIATION_NATIVE_ID"
    const val ADMOB_MEDIATION_REWARDED_ID_KEY = "ADMOB_MEDIATION_REWARDED_ID"
    const val ADMOB_MEDIATION_INTERSTITIAL_ID_KEY = "ADMOB_MEDIATION_INTERSTITIAL_ID"
    const val ADMOB_MEDIATION_KEY = "ADMOB_MEDIATION_KEY"
    const val ADS_MODEL_KEY = "Model_Key"
    const val ADS_STRATEGY = "Strategy"
    const val INTERSTITIAL_KEY = "Interstitial_Key"
    const val FIREBASE_ANALYTICS_KEY = "Ads_Manager_Event"
    const val ADMOB_OPEN_AD_ENABLE_KEY = "ADMOB_OPEN_AD_ENABLE"
    const val OPEN_AD_KEY = "Open_Ad"
    var testMode: Boolean = false
    var isAdPreloadEnable = false
    var interstitialClickAdCounter = 0
    var loadAdmobInters = 0
    var isInit = false
    var canShowInterstitial = true
    var admobRequestNativeFailed = 0
    var adMobNativeAdLoad = 0
    var isAppOpenAdEnable = true
}
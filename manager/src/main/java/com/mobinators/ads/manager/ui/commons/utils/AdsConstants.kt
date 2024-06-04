package com.mobinators.ads.manager.ui.commons.utils

object AdsConstants {
    const val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_ADMOB_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"
    const val TEST_ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_ADMOB_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_ADMOB_OPEN_APP_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_ADMOB_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_ADMOB_COLLAPSE_BANNER_ID = "ca-app-pub-3940256099942544/2014213617"
    const val TEST_MAX_INTERSTITIAL_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_BANNER_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_REWARD_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_Native_ADS_ID = "YOUR_AD_UNIT_ID"
    const val TEST_MAX_APP_OPEN_ADS_ID = "YOUR_AD_UNIT_ID"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="
    const val PLAY_STORE_URL_1 = "market://details?id="
    const val AMAZON_STORE_URL="https://www.amazon.com/gp/mas/dl/android?p="
    const val ADS_OFF = 0
    const val AD_MOB_MEDIATION = 1
    const val AD_MOB = 2
    const val MAX_MEDIATION = 3
    const val ADMOB_BANNER_ID_KEY = "ADMOB_BANNER_ADS_ID"
    const val ADMOB_COLLAPSE_BANNER_ID_KEY = "ADMOB_COLLAPSE_BANNER_ADS_ID"
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
    const val RATE_US_DIALOG_COUNT_KEY = "RATE_US_DIALOG_COUNT"
    const val ADS_STRATEGY = "Strategy"
    const val FIREBASE_ANALYTICS_KEY = "Ads_Manager_Event"
    const val ADMOB_OPEN_AD_ENABLE_KEY = "ADMOB_OPEN_AD_ENABLE"
    const val STORE_STRATEGY_KEY = "STORE_STRATEGY"
    const val APP_UPDATE_KEY = "AppUpdate"
    const val APP_UPDATE = "Update"
    const val APP_RATE_US_DIALOG = "RateUsDialog"
    const val APP_UPDATE_VERSION_KEY = "AppUpdateVersion"
    var testMode: Boolean = false
    var isAdPreloadEnable = false
    var isInit = false
    var isAppOpenAdEnable = true
    const val GOOGLE_PLAY_STORE = 1
    const val AMAZON_APP_STORE = 2
    const val HUAWEI_APP_GALLERY = 3
    var selectedStore: Int = 1
}
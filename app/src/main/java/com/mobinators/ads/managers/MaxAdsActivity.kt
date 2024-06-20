package com.mobinators.ads.managers

import android.os.Bundle
import android.view.View
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.enums.AdsLoadingState
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import com.mobinators.ads.manager.ui.commons.utils.AnalyticsManager
import com.mobinators.ads.managers.databinding.ActivityMaxAdsBinding
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.ui.commons.base.BaseActivity

class MaxAdsActivity : BaseActivity<ActivityMaxAdsBinding>(), View.OnClickListener {
    override fun getActivityView() = ActivityMaxAdsBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
//        AdsUtils.maxTestAds(this)
        MediationRewardedAd.loadRewardAds(
            this,
            false,
            object : MediationRewardedAd.RewardLoadCallback {
                override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        adsLoadingState.name
                    )
                    when (adsLoadingState) {
                        AdsLoadingState.APP_PURCHASED -> logD("MainActivity Reward Ads : You have Purchased your app")
                        AdsLoadingState.NETWORK_OFF -> logD("MainActivity Reward Ads : Internet Off")
                        AdsLoadingState.ADS_OFF -> logD("MainActivity Reward Ads : onAdsOff")
                        AdsLoadingState.ADS_STRATEGY_WRONG -> logD("MainActivity Reward Ads : Ads Strategy wrong")
                        AdsLoadingState.ADS_ID_NULL -> logD("MainActivity Reward Ads : Ads ID is Null found")
                        AdsLoadingState.TEST_ADS_ID -> logD("MainActivity Reward Ads : Test Id found in released mode your app")
                        AdsLoadingState.ADS_LOADED -> logD("MainActivity Reward Ads : onAdsLoaded")
                        AdsLoadingState.ADS_LOAD_FAILED -> logD("MainActivity Reward Ads : Ads load failed")
                    }
                }
            })
        binding.maxBanner.setOnClickListener(this)
        binding.maxNative.setOnClickListener(this)
        binding.maxNativeBanner.setOnClickListener(this)
        binding.maxRewarded.setOnClickListener(this)
        binding.maxInterstitial.setOnClickListener(this)
        binding.maxOpen.setOnClickListener(this)
        binding.testAdsEnable.setOnClickListener(this)
    }

    override fun onClick(itemId: View?) {
        when (itemId!!.id) {
            binding.maxBanner.id -> bannerAds()
            binding.maxNative.id -> nativeAds()
            binding.maxNativeBanner.id -> nativeBannerAds()
            binding.maxRewarded.id -> rewardedAds()
            binding.maxInterstitial.id -> interstitialAds()
            binding.maxOpen.id -> openAds()
            binding.testAdsEnable.id -> AdsUtils.maxTestAds(this@MaxAdsActivity)
        }
    }

    private fun bannerAds() {
        BannerAdMediation.showBannerAds(
            this,
            false,
            binding.maxContainer,
            object : BannerAdMediation.BannerAdListener {
                override fun onAdsLoaded() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        "Ads loaded"
                    )
                    logD("MainActivity Banner Ads : Ads Loaded")
                }

                override fun onAdsState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Banner Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Banner Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Banner Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Banner Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Banner Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Banner Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Banner Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Banner Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Banner Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Banner Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Banner Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Banner Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Banner Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Banner Ads : Ads Open")
                    }
                }
            })
    }

    private fun nativeAds() {
        MediationNativeAds.showNativeAds(
            this,
            false,
            binding.maxContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
                override fun onAdsSwipe() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads swipe"
                    )
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Native Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Native Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Native Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Native Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Native Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Native Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Native Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Native Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Native Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Native Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Native Ads : Ads Open")
                    }
                }
            }
        )
    }

    private fun nativeBannerAds() {
        MediationNativeAds.showNativeAds(
            this,
            false,
            binding.maxContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
                override fun onAdsSwipe() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads swipe"
                    )
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Native Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Native Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Native Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Native Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Native Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Native Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Native Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Native Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Native Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Native Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Native Ads : Ads Open")
                    }
                }
            }, true
        )
    }

    private fun rewardedAds() {
        MediationRewardedAd.showRewardAds(
            this,
            false,
            object : MediationRewardedAd.ShowRewardedAdsCallback {
                override fun onRewardEarned(item: Int, type: String) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Amount : $item  : Type : $type"
                    )
                    logD("MainActivity Reward onRewardEarned : Amount : $item  : Type : $type")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Reward Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Reward Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Reward Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Reward Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Reward Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Reward Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Reward Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Reward Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Reward Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Reward Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Reward Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Reward Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Reward Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Reward Ads : Ads Open")
                    }
                }

            })
    }

    private fun interstitialAds() {
        MediationAdInterstitial.showInterstitialAds(
            this,
            false,
            object : MediationAdInterstitial.AdsShowCallback {
                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowInterstitialAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity  Interstitial Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity  Interstitial Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Interstitial Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Interstitial Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Interstitial Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Interstitial Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Interstitial Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Interstitial Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Interstitial Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Interstitial Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Interstitial Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Interstitial Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Interstitial Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Interstitial Ads : Ads Open")
                    }
                }

            })
    }

    private fun openAds() {
        MediationOpenAd.showAppOpenAds(
            this,
            false,
            object : MediationOpenAd.AdsShowAppOpenCallback {
                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowAppOpenAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity  AppOpen Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity  AppOpen Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity AppOpen Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity AppOpen Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity AppOpen Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity AppOpen Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity AppOpen Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity AppOpen Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity AppOpen Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity AppOpen Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity AppOpen Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity AppOpen Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity AppOpen Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity AppOpen Ads : Ads Open")
                    }
                }
            })
    }
}
package com.mobinators.ads.managers

import android.os.Bundle
import android.view.View
import com.facebook.ads.AdView
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.listener.BannerAdListener
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import com.mobinators.ads.managers.databinding.ActivityMaxAdsBinding
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.ui.commons.base.BaseActivity

class MaxAdsActivity : BaseActivity<ActivityMaxAdsBinding>(), View.OnClickListener {
    override fun getActivityView() = ActivityMaxAdsBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        AdsUtils.maxTestAds(this)
        MediationRewardedAd.loadRewardAds(
            this,
            false,
            object : MediationRewardedAd.RewardLoadCallback {
                override fun onAdsLoaded() {
                    logD("MainActivity Reward onAdsLoaded")
                }

                override fun onAdsOff() {
                    logD("MainActivity Reward onAdsOff")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("Ads Impress Mode")
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
            object : BannerAdListener {
                override fun onLoaded(adType: Int) {
                    logD("MaxAdsActivity onLoaded : $adType")
                }

                override fun onAdClicked(adType: Int) {
                    logD("MaxAdsActivity onAdClicked : $adType")
                }

                override fun onError(error: String) {
                    logD("MaxAdsActivity onError Error : $error")
                }

                override fun onFacebookAdCreated(facebookBanner: AdView) {
                    logD("MaxAdsActivity onFacebookAdCreated : $facebookBanner")
                }

                override fun isEnableAds(isAds: Boolean) {
                    logD("MaxAdsActivity isEnableAds : $isAds")
                }

                override fun isOffline(offline: Boolean) {
                    logD("Ads is Offline : $offline")
                }
            })
    }

    private fun nativeAds() {
        MediationNativeAds.showNativeAds(
            this,
            false,
            binding.maxContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
                override fun onAdsOff() {
                    logD("MaxActivity Ads Off")
                }

                override fun onAdsOpen() {
                    logD("MaxActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    logD("MaxActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    logD("MaxActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    logD("MaxActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MaxActivity Native Ads Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MaxActivity Native Ads You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MaxActivity Native Ads Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MaxActivity Native Ads Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MaxActivity Native Ads Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MaxActivity Native Ads ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MaxActivity Native Ads Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MaxActivity Native Ads Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MaxActivity Native Ads Ads Impress Mode")
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
                override fun onAdsOff() {
                    logD("MaxActivity Ads Off")
                }

                override fun onAdsOpen() {
                    logD("MaxActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    logD("MaxActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    logD("MaxActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    logD("MaxActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MaxActivity Native Ads Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MaxActivity Native Ads You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MaxActivity Native Ads Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MaxActivity Native Ads Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MaxActivity Native Ads Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MaxActivity Native Ads ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MaxActivity Native Ads Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MaxActivity Native Ads Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MaxActivity Native Ads Ads Impress Mode")
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
                override fun onAdsOff() {
                    logD("MainActivity Reward onAdsOff")
                }

                override fun onRewardEarned(item: Int, type: String) {
                    logD("MainActivity Reward onRewardEarned : Amount : $item  : Type : $type")
                }

                override fun onAdsClicked() {
                    logD("MainActivity Reward onAdsClicked")
                }

                override fun onAdsDisplay() {
                    logD("MainActivity Reward onAdsDisplay")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("Ads Impress Mode")
                    }
                }

            })
    }

    private fun interstitialAds() {
        MediationAdInterstitial.showInterstitialAds(
            this,
            false,
            object : MediationAdInterstitial.AdsShowCallback {
                override fun onAdsOff() {
                    logD("MaxAds Activity Interstitial Ads is off")
                }

                override fun onAdsError(error: String) {
                    logD("MaxAds Activity Interstitial Ads error : $error")
                }

                override fun onAdsClicked() {
                    logD("MaxAds Activity Interstitial Ads click")
                }

                override fun onAdsDismiss() {
                    logD("MaxAds Activity Interstitial Ads dismiss")
                }

                override fun onAdsImpress() {
                    logD("MaxAds Activity Interstitial Ads Impress")
                }
            })
    }

    private fun openAds() {
        MediationOpenAd.showAppOpenAds(
            this,
            false,
            object : MediationOpenAd.AdsShowAppOpenCallback {
                override fun onAdsOff() {
                    logD("MaxActivity App Open Ads is off")
                }

                override fun onAdsClicked() {
                    logD("MaxActivity App Open Ads Clicked")
                }

                override fun onAdsDisplay() {
                    logD("MaxActivity App Open Ads onAdsDisplay")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("Ads Impress Mode")
                    }
                }
            })
    }
}
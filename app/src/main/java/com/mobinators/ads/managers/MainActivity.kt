package com.mobinators.ads.managers

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.rewarded.RewardItem
import com.mobinators.ads.manager.extensions.appRateUs
import com.mobinators.ads.manager.extensions.appUpdate
import com.mobinators.ads.manager.extensions.exitPanel
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.listener.AppListener
import com.mobinators.ads.manager.ui.commons.listener.AppRateUsCallback
import com.mobinators.ads.manager.ui.commons.listener.AppUpdateState
import com.mobinators.ads.manager.ui.commons.listener.PanelListener
import com.mobinators.ads.manager.ui.commons.listener.RateUsState
import com.mobinators.ads.manager.ui.commons.models.PanelModel
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AppPurchaseUtils
import com.mobinators.ads.manager.ui.commons.utils.DeviceInfoUtils
import com.mobinators.ads.managers.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.navigateActivity
import pak.developer.app.managers.ui.commons.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>(), View.OnClickListener {
    override fun getActivityView() = ActivityMainBinding.inflate(layoutInflater)
    override fun initView(savedInstanceState: Bundle?) {
        MediationRewardedAd.loadRewardAds(
            this,
            false,
            object : MediationRewardedAd.RewardLoadCallback {
                override fun onAdsLoaded() {
                    logD("MainActivity Reward Ads : onAdsLoaded")
                }

                override fun onAdsOff() {
                    logD("MainActivity Reward Ads : onAdsOff")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Reward Ads : Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Reward Ads : You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Reward Ads : Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Reward Ads : Ads ID is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Reward Ads : Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Reward Ads : Ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Reward Ads : Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Reward Ads : Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Reward Ads : Ads Impress Mode")
                    }
                }

            })
        binding.maxAdActivity.setOnClickListener(this)
        binding.loadAds.setOnClickListener(this)
        binding.nativeAds.setOnClickListener(this)
        binding.nativeBannerAds.setOnClickListener(this)
        binding.rewardedAds.setOnClickListener(this)
        binding.rewardedInterAds.setOnClickListener(this)
        binding.openAds.setOnClickListener(this)
        binding.interAds.setOnClickListener(this)
        binding.billingButton.setOnClickListener(this)
        binding.collapseBannerButton.setOnClickListener(this)
        logD("Device Info : ${DeviceInfoUtils.getDeviceInfo()}")
        appRateUs(object : AppRateUsCallback {
            override fun onRateUsState(rateState: RateUsState) {
                when (rateState) {
                    RateUsState.RATE_US_COMPLETED -> logD("Rate US Completed")
                    RateUsState.RATE_US_CANCEL -> logD("Rate US Cancel")
                    RateUsState.RATE_US_FAILED -> logD("Rate US Failed")
                    RateUsState.RATE_US_ERROR -> logD("Rate US Error")
                    RateUsState.AMAZON_STORE -> logD("Rate US : Amazon Store Selected")
                    RateUsState.HUAWEI_STORE -> logD("Rate US : Huawei Store Selected")
                    RateUsState.WRONG_STORE -> logD("Rate US : Wrong Store Selected")
                }
            }
        })
        appUpdate(object : AppListener {
            override fun onDownload() {
                logD("OnDownload")
            }

            override fun onInstalled() {
                logD("onInstalled")
            }

            override fun onCancel() {
                logD("onCancel")
            }

            override fun onFailure(error: Exception) {
                logD("onFailure : $error")
            }

            override fun onNoUpdateAvailable() {
                logD("onNoUpdateAvailable")
            }

            override fun onStore(updateState: AppUpdateState) {
                when (updateState) {
                    AppUpdateState.WRONG_STORE -> logD("Wrong Selected Store ID")
                    AppUpdateState.AMAZON_STORE -> logD("Amazon Store Selected")
                    AppUpdateState.HUAWEI_STORE -> logD("Huawei Store Selected")
                }
            }

        }, 1)
        inAppPurchased()
    }

    override fun onClick(itemId: View?) {
        when (itemId!!.id) {
            binding.maxAdActivity.id -> navigateActivity(this, MaxAdsActivity::class.java)
            binding.loadAds.id -> bannerAds()
            binding.nativeAds.id -> nativeAds()
            binding.nativeBannerAds.id -> nativeBannerAds()
            binding.rewardedAds.id -> rewardedAds()
            binding.rewardedInterAds.id -> rewardedInterstitialAds()
            binding.openAds.id -> openAds()
            binding.interAds.id -> interstitialAds()
            binding.billingButton.id -> {
                CoroutineScope(Dispatchers.Main).launch {
                    AppPurchaseUtils.onSubscription("product_id_example")
                }
            }

            binding.collapseBannerButton.id -> navigateActivity(
                this,
                CollapseBannerActivity::class.java
            )
        }
    }

    private fun bannerAds() {

        BannerAdMediation.showBannerAds(
            this,
            false,
            binding.adContainer,
            object : BannerAdMediation.BannerAdListener {
                override fun onAdsOff() {
                    logD("MainActivity Banner Ads : Ads Off")
                }

                override fun onAdsLoaded() {
                    logD("MainActivity Banner Ads : Ads Loaded")
                }

                override fun onAdsClicked() {
                    logD("MainActivity Banner Ads : Ads Clicked")
                }

                override fun onAdsClosed() {
                    logD("MainActivity Banner Ads : Ads Closed")
                }

                override fun onAdsOpened() {
                    logD("MainActivity Banner Ads : Ads Open")
                }

                override fun onAdsError(adsErrorState: AdsErrorState) {
                    when (adsErrorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Banner Ads : Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Banner Ads : You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Banner Ads : Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Banner Ads : Ads ID is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Banner Ads : Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Banner Ads : Ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Banner Ads : Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Banner Ads : Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Banner Ads : Ads Impress Mode")
                    }
                }

            })
    }

    private fun nativeAds() {
        MediationNativeAds.showNativeAds(
            this,
            false,
            binding.adContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
                override fun onAdsOff() {
                    logD("MainActivity Ads Off")
                }

                override fun onAdsOpen() {
                    logD("MainActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    logD("MainActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    logD("MainActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Native Ads Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Native Ads You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Native Ads Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Native Ads Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Native Ads Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Native Ads Ads Impress Mode")
                    }
                }
            }
        )
    }

    private fun nativeBannerAds() {
        MediationNativeAds.showNativeAds(
            this,
            false,
            binding.adContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
                override fun onAdsOff() {
                    logD("MainActivity Ads Off")
                }

                override fun onAdsOpen() {
                    logD("MainActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    logD("MainActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    logD("MainActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Native Ads Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Native Ads You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Native Ads Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Native Ads Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Native Ads Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Native Ads Ads Impress Mode")
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

    private fun rewardedInterstitialAds() {

        MediationRewardedInterstitialAd.showRewardedInterstitialAd(
            this,
            false,
            object : MediationRewardedInterstitialAd.ShowRewardAdsCallback {
                override fun onAdsOff() {
                    logD("MainActivity Reward Interstitial Ads is off")
                }

                override fun onAdsError(error: String) {
                    logD("MainActivity Reward Interstitial Ads Error : $error")
                }

                override fun onAdsReward(item: RewardItem) {
                    logD("MainActivity Reward Interstitial Ads Reward : $item")
                }

                override fun onAdsClicked() {
                    logD("MainActivity Reward Interstitial Ads Clicked")
                }

                override fun onAdsImpress() {
                    logD("MainActivity Reward Interstitial Ads Impress")
                }

                override fun onAdsDismiss(item: RewardItem) {
                    logD(" MainActivity Reward Interstitial Ads Dismiss : $item")
                }
            })
    }

    private fun interstitialAds() {
        MediationAdInterstitial.showInterstitialAds(
            this,
            false,
            object : MediationAdInterstitial.AdsShowCallback {
                override fun onAdsOff() {
                    logD("MainActivity Interstitial Ads is off")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Interstitial Ads : Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Interstitial Ads : You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Interstitial Ads : Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Interstitial Ads : Ads ID is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Interstitial Ads : Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Interstitial Ads : Ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Interstitial Ads : Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Interstitial Ads : Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Interstitial Ads : Ads Impress Mode")
                    }
                }

                override fun onAdsClicked() {
                    logD("MainActivity Interstitial Ads click")
                }
            })
    }

    private fun openAds() {
        MediationOpenAd.showAppOpenAds(
            this,
            false,
            object : MediationOpenAd.AdsShowAppOpenCallback {
                override fun onAdsOff() {
                    logD("MainActivity App Open Ads is off")
                }

                override fun onAdsClicked() {
                    logD("MainActivity App Open Ads Clicked")
                }

                override fun onAdsDisplay() {
                    logD("MainActivity App Open Ads onAdsDisplay")
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


    private fun inAppPurchased() {
        AppPurchaseUtils.initConnection(
            this,
            "base64_key_example",
            object : AppPurchaseUtils.BillingCallback {
                override fun onRequiredNetwork() {
                    logD("Internet is not available")
                }

                override fun onSubscribe(
                    isSuccess: Boolean,
                    isPremium: Boolean,
                    isLocked: Boolean
                ) {
                    logD("isSuccess : $isSuccess , isPremium: $isPremium, isLocked: $isLocked")
                }

                override fun onError(error: String) {
                    logD("$error")
                }
            })
    }


    override fun onBackPressed() {
        exitPanel(supportFragmentManager, object : PanelListener {
            override fun onExit() {
                logD("Exit Panel")
            }

            override fun onCancel() {
                logD("Cancel Panel ")
            }
        }, model = PanelModel().apply {
            this.title = "App Exit"
            this.titleColor = R.color.black
            this.desc = "Your app is exit?"
            this.descColor = R.color.black
            this.cancelBgColor = R.color.lightGray
            this.cancelButtonText = "Cancel"
            this.cancelButtonTitleColor = R.color.black
            this.exitButtonBgColor = R.color.black
            this.exitButtonText = "exit"
            this.exitButtonTextColor = R.color.white
            this.panelBackgroundColor = R.color.lightGray
            this.isAdsShow = false  // true value is show ads , false  value is not show ads
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        AppPurchaseUtils.clientDestroy()
        MediationNativeAds.onDestroy()
    }
}
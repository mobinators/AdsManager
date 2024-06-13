package com.mobinators.ads.managers


import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.mobinators.ads.manager.ui.commons.utils.AnalyticsManager
import com.mobinators.ads.manager.ui.commons.utils.AppPurchaseUtils
import com.mobinators.ads.manager.ui.commons.utils.DeviceInfoUtils
import com.mobinators.ads.managers.compose.ComposeAdsActivity
import com.mobinators.ads.managers.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.navigateActivity
import pak.developer.app.managers.extensions.sdk33AndUp
import pak.developer.app.managers.extensions.showToast
import pak.developer.app.managers.ui.commons.base.BaseActivity


class MainActivity : BaseActivity<ActivityMainBinding>(), View.OnClickListener {
    override fun getActivityView() = ActivityMainBinding.inflate(layoutInflater)
    override fun initView(savedInstanceState: Bundle?) {

        askNotificationPermission()
        notificationSetting()
        MediationRewardedAd.loadRewardAds(
            this,
            false,
            object : MediationRewardedAd.RewardLoadCallback {
                override fun onAdsLoaded() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Ads loaded"
                    )
                    logD("MainActivity Reward Ads : onAdsLoaded")
                }

                override fun onAdsOff() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Ads is Off"
                    )
                    logD("MainActivity Reward Ads : onAdsOff")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        errorState.name
                    )
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
        binding.composeButton.setOnClickListener(this)
        logD("Device Info : ${DeviceInfoUtils.getDeviceInfo()}")
        appRateUs(object : AppRateUsCallback {
            override fun onRateUsState(rateState: RateUsState) {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "RateUs",
                    rateState.name
                )
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
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onDownload"
                )
                logD("onDownload")
            }

            override fun onInstalled() {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onInstalled"
                )
                logD("onInstalled")
            }

            override fun onCancel() {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onCancel"
                )
                logD("onCancel")
            }

            override fun onFailure(error: Exception) {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onFailure: $error"
                )
                logD("onFailure : $error")
            }

            override fun onNoUpdateAvailable() {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onNoUpdateAvailable"
                )
                logD("onNoUpdateAvailable")
            }

            override fun onStore(updateState: AppUpdateState) {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "Store Type : ${updateState.name}"
                )
                when (updateState) {
                    AppUpdateState.WRONG_STORE -> logD("Wrong Selected Store ID")
                    AppUpdateState.AMAZON_STORE -> logD("Amazon Store Selected")
                    AppUpdateState.HUAWEI_STORE -> logD("Huawei Store Selected")
                }
            }
        }, 1)
        inAppPurchased()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exitPanel(supportFragmentManager, object : PanelListener {
                    override fun onExit() {
                        AnalyticsManager.getInstance().setAnalyticsEvent(
                            resources.getString(R.string.app_name),
                            "ExitPanel",
                            "onExit"
                        )
                        logD("Exit Panel")
                    }

                    override fun onCancel() {
                        AnalyticsManager.getInstance().setAnalyticsEvent(
                            resources.getString(R.string.app_name),
                            "ExitPanel",
                            "onCancel"
                        )
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
                    this.exitButtonText = "Exit"
                    this.exitButtonTextColor = R.color.white
                    this.panelBackgroundColor = R.color.lightGray
                    this.isAdsShow = true  // true value is show ads , false  value is not show ads
                })
            }
        })
        DeviceInfoUtils.getLocalIpAddress(this) {
            logD(" Local Ip Address : $it")
        }

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

                    // In-App Purchased info
                    AppPurchaseUtils.getInAppPurchaseInfo("product_id_example") {
                        logD("Info Detail : $it")
                    }

                    /*  // In-App Purchased
                      AppPurchaseUtils.inAppPurchase("product_id_example")


                       // OR In-App Subscription info
                      AppPurchaseUtils.getInAppSubscriptionInfo("product_id_example"){
                          logD("Info Detail : $it")
                      }

                      // In-App Subscription
                      AppPurchaseUtils.inAppSubscription("product_id_example")*/
                }
            }

            binding.collapseBannerButton.id -> navigateActivity(
                this,
                CollapseBannerActivity::class.java
            )

            binding.composeButton.id -> navigateActivity(this, ComposeAdsActivity::class.java)
        }
    }

    private fun bannerAds() {
        BannerAdMediation.showBannerAds(
            this,
            false,
            binding.adContainer,
            object : BannerAdMediation.BannerAdListener {
                override fun onAdsOff() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        "Ads is Off"
                    )
                    logD("MainActivity Banner Ads : Ads Off")
                }

                override fun onAdsLoaded() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        "Ads loaded"
                    )
                    logD("MainActivity Banner Ads : Ads Loaded")
                }

                override fun onAdsClicked() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        "Ads clicked"
                    )
                    logD("MainActivity Banner Ads : Ads Clicked")
                }

                override fun onAdsClosed() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        "Ads closed"
                    )
                    logD("MainActivity Banner Ads : Ads Closed")
                }

                override fun onAdsOpened() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        "Ads opened"
                    )
                    logD("MainActivity Banner Ads : Ads Open")
                }

                override fun onAdsError(adsErrorState: AdsErrorState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "BannerAds",
                        adsErrorState.name
                    )
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
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads is Off"
                    )
                    logD("MainActivity Ads Off")
                }

                override fun onAdsOpen() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads Open"
                    )
                    logD("MainActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads clicked"
                    )
                    logD("MainActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads closed"
                    )
                    logD("MainActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads swipe"
                    )
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        errorState.name
                    )
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
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "CustomNativeAds",
                        "Ads is Off"
                    )
                    logD("MainActivity Ads Off")
                }

                override fun onAdsOpen() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "CustomNativeAds",
                        "Ads Open"
                    )
                    logD("MainActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "CustomNativeAds",
                        "Ads Clicked"
                    )
                    logD("MainActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "CustomNativeAds",
                        "Ads closed"
                    )
                    logD("MainActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "CustomNativeAds",
                        "Ads swipe"
                    )
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "CustomNativeAds",
                        errorState.name
                    )
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
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Ads is Off"
                    )
                    logD("MainActivity Reward onAdsOff")
                }

                override fun onRewardEarned(item: Int, type: String) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Amount : $item  : Type : $type"
                    )
                    logD("MainActivity Reward onRewardEarned : Amount : $item  : Type : $type")
                }

                override fun onAdsClicked() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Ads clicked"
                    )
                    logD("MainActivity Reward onAdsClicked")
                }

                override fun onAdsDisplay() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Ads display"
                    )
                    logD("MainActivity Reward onAdsDisplay")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        errorState.name
                    )
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
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads is Off"
                    )
                    logD("MainActivity Reward Interstitial Ads is off")
                }

                override fun onAdsError(error: String) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads error : $error"
                    )
                    logD("MainActivity Reward Interstitial Ads Error : $error")
                }

                override fun onAdsReward(item: RewardItem) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads RewardItem : $item"
                    )
                    logD("MainActivity Reward Interstitial Ads Reward : $item")
                }

                override fun onAdsClicked() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads clicked"
                    )
                    logD("MainActivity Reward Interstitial Ads Clicked")
                }

                override fun onAdsImpress() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads Impress"
                    )
                    logD("MainActivity Reward Interstitial Ads Impress")
                }

                override fun onAdsDismiss(item: RewardItem) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads Dismiss"
                    )
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
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowInterstitialAds",
                        "Ads is Off"
                    )
                    logD("MainActivity Interstitial Ads is off")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowInterstitialAds",
                        errorState.name
                    )
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
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowInterstitialAds",
                        "Ads clicked"
                    )
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
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowAppOpenAds",
                        "Ads is Off"
                    )
                    logD("MainActivity App Open Ads is off")
                }

                override fun onAdsClicked() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowAppOpenAds",
                        "Ads clicked"
                    )
                    logD("MainActivity App Open Ads Clicked")
                }

                override fun onAdsDisplay() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowAppOpenAds",
                        "Ads display"
                    )
                    logD("MainActivity App Open Ads onAdsDisplay")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "ShowAppOpenAds",
                        errorState.name
                    )
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
            isAppPurchased = true,
            object : AppPurchaseUtils.BillingCallback {
                override fun onRequiredNetwork() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "InAppPurchased ",
                        "RequiredNetwork"
                    )
                    logD("Internet is not available")
                }

                override fun onSubscribe(
                    isSuccess: Boolean,
                    isPremium: Boolean,
                    isLocked: Boolean
                ) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "InAppPurchased ",
                        "onSubscriber : isSuccess : $isSuccess , isPremium: $isPremium, isLocked: $isLocked"
                    )
                    logD("isSuccess : $isSuccess , isPremium: $isPremium, isLocked: $isLocked")
                }

                override fun onBillingState(billingState: AppPurchaseUtils.BillingState) {
                    when (billingState) {
                        AppPurchaseUtils.BillingState.FEATURE_NOT_SUPPORTED -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.BILLING_UNAVAILABLE -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.USER_CANCELED -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.DEVELOPER_ERROR -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.ITEM_UNAVAILABLE -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.NETWORK_ERROR -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.SERVICE_DISCONNECTED -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.PENDING -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.UNSPECIFIED_STATE -> logD("FEATURE_NOT_SUPPORTED")
                    }
                }

                override fun onBillingError(error: String) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "In App Purchased ",
                        "Error : $error"
                    )
                    logD(error)
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        AppPurchaseUtils.clientDestroy()
        MediationNativeAds.onDestroy()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            showToast(this, "Notifications permission granted")
        } else {
            showToast(this, "FCM can't post notifications without POST_NOTIFICATIONS permission")
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        sdk33AndUp {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun notificationSetting() {
        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.getString(key)
                logD("Key: $key Value: $value")
            }
        }
    }
}
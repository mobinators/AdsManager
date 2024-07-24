package com.mobinators.ads.managers


import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.rewarded.RewardItem
import com.mobinators.ads.manager.extensions.appRateUs
import com.mobinators.ads.manager.extensions.appUpdate
import com.mobinators.ads.manager.extensions.exitPanel
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.enums.AdsLoadingState
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView(savedInstanceState: Bundle?) {
        askNotificationPermission()
        notificationSetting()
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
                logD("AppUpdate : onDownload")
            }

            override fun onInstalled() {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onInstalled"
                )
                logD("AppUpdate : onInstalled")
            }

            override fun onCancel() {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onCancel"
                )
                logD("AppUpdate : onCancel")
            }

            override fun onFailure(error: Exception) {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onFailure: $error"
                )
                logD("AppUpdate : onFailure : $error")
            }

            override fun onNoUpdateAvailable() {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "onNoUpdateAvailable"
                )
                logD("AppUpdate : onNoUpdateAvailable")
            }

            override fun onStore(updateState: AppUpdateState) {
                AnalyticsManager.getInstance().setAnalyticsEvent(
                    resources.getString(R.string.app_name),
                    "AppUpdate",
                    "Store Type : ${updateState.name}"
                )
                when (updateState) {
                    AppUpdateState.WRONG_STORE -> logD("AppUpdate : Wrong Selected Store ID")
                    AppUpdateState.AMAZON_STORE -> logD("AppUpdate : Amazon Store Selected")
                    AppUpdateState.HUAWEI_STORE -> logD("AppUpdate : Huawei Store Selected")
                }
            }
        }, launcher)
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
        logD(" Device Info  : ${DeviceInfoUtils.getDeviceInfo()}")
    }


    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_CANCELED -> logD("RESULT_CANCELED")
                Activity.RESULT_OK -> logD("RESULT_OK")
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
            binding.adContainer,
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
            binding.adContainer,
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

    private fun rewardedInterstitialAds() {
        MediationRewardedInterstitialAd.showRewardedInterstitialAd(
            this,
            false,
            object : MediationRewardedInterstitialAd.ShowRewardAdsCallback {
                override fun onAdsReward(item: RewardItem) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads RewardItem : $item"
                    )
                    logD("MainActivity Reward Interstitial Ads Reward : $item")
                }

                override fun onAdsDismiss(item: RewardItem) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads Dismiss"
                    )
                    logD(" MainActivity Reward Interstitial Ads Dismiss : $item")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads State : ${adsShowState.name}"
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity RewardInterstitial Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity RewardInterstitial Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity RewardInterstitial Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity RewardInterstitial Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity RewardInterstitial Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity RewardInterstitial Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity RewardInterstitial Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity RewardInterstitial Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity RewardInterstitial Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity RewardInterstitial Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity RewardInterstitial Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity RewardInterstitial Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity RewardInterstitial Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity RewardInterstitial Ads : Ads Open")
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
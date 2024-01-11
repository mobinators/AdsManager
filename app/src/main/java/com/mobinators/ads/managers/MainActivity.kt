package com.mobinators.ads.managers

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.facebook.ads.AdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.mobinators.ads.manager.extensions.createThumbNail
import com.mobinators.ads.manager.extensions.exitPanel
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.listener.BannerAdListener
import com.mobinators.ads.manager.ui.commons.listener.ImageProvider
import com.mobinators.ads.manager.ui.commons.listener.OnNativeAdListener
import com.mobinators.ads.manager.ui.commons.listener.OnRewardedAdListener
import com.mobinators.ads.manager.ui.commons.listener.OpenAddCallback
import com.mobinators.ads.manager.ui.commons.listener.PanelListener
import com.mobinators.ads.manager.ui.commons.models.PanelModel
import com.mobinators.ads.manager.ui.commons.nativeBanner.MediationNativeBanner
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAd
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AppPurchaseUtils
import com.mobinators.ads.manager.ui.commons.utils.DeviceInfoUtils
import com.mobinators.ads.manager.ui.commons.views.dialog.ProgressDialogUtils
import com.mobinators.ads.managers.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.navigateActivity
import pak.developer.app.managers.ui.commons.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>(), View.OnClickListener {
    private var mediationNativeAd: MediationNativeAd? = null
    private var progressDialogUtils: ProgressDialogUtils? = null
    override fun getActivityView() = ActivityMainBinding.inflate(layoutInflater)
    override fun initView(savedInstanceState: Bundle?) {
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
        mediationNativeAd = MediationNativeAd(this, false, binding.adContainer, false)
        logD("Device Info : ${DeviceInfoUtils.getDeviceInfo()}")
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
            object : BannerAdListener {
                override fun onLoaded(adType: Int) {
                    logD("MainActivity onLoaded : $adType")
                }

                override fun onAdClicked(adType: Int) {
                    logD("MainActivity onAdClicked : $adType")
                }

                override fun onError(error: String) {
                    logD("MainActivity onError Error : $error")
                }

                override fun onFacebookAdCreated(facebookBanner: AdView) {
                    logD("MainActivity onFacebookAdCreated : $facebookBanner")
                }

                override fun isEnableAds(isAds: Boolean) {
                    logD("MainActivity isEnableAds : $isAds")
                }

                override fun isOffline(offline: Boolean) {
                    logD("Ads is Offline : $offline")
                }
            })
    }

    private fun nativeAds() {
        mediationNativeAd!!.loadAd(object : OnNativeAdListener {
            override fun onError(error: String) {
                logD("MainActivity Error : $error")
            }

            override fun onLoaded(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun onAdClicked(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
    }

    private fun nativeBannerAds() {
        MediationNativeBanner.loadAd(this, false, binding.adContainer, object : OnNativeAdListener {
            override fun onError(error: String) {
                logD("MainActivity Error : $error")
            }

            override fun onLoaded(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun onAdClicked(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        }, object : ImageProvider {
            override fun onProviderImage(imageView: ImageView, imageUrl: String) {
                imageView.createThumbNail(this@MainActivity, imageUrl)
            }
        })
    }

    private fun rewardedAds() {
        MediationRewardedAd.loadRewardedAd(this, false, object : OnRewardedAdListener {
            override fun onError(error: String) {
                logD("MainActivity onError Error : $error")
            }

            override fun onAdLoaded(adType: Int) {
                logD("MainActivity onAdLoaded Ads Type : $adType")
            }

            override fun onClicked(adType: Int) {
                logD("MainActivity onClicked Ads Type : $adType")
            }

            override fun onDismissClick(adType: Int, item: RewardItem) {
                logD("MainActivity onDismissClick Ads Type : $adType")
            }

            override fun onCancel(adType: Int) {
                logD("MainActivity onCancel Ads Type: $adType")
            }

            override fun onRewarded(item: RewardItem) {
                logD("MainActivity onRewarded  : ${item.type} : rewarded : ${item.amount}")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
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
        /*
                MediationRewardedInterstitialAd.showRewardedInterstitialAd(
                    this, false,
                    object : OnRewardedAdListener {
                        override fun onError(error: String) {
                            logD("MainActivity onError Error : $error")
                        }

                        override fun onAdLoaded(adType: Int) {
                            logD("MainActivity onAdLoaded Ads Type : $adType")
                        }

                        override fun onClicked(adType: Int) {
                            logD("MainActivity onClicked Ads Type : $adType")
                        }

                        override fun onDismissClick(adType: Int, item: RewardItem) {
                            logD("MainActivity onDismissClick Ads Type : ${item.amount}")
                        }

                        override fun onCancel(adType: Int) {
                            logD("MainActivity onCancel Ads Type: $adType")
                        }

                        override fun onRewarded(item: RewardItem) {
                            logD("MainActivity onRewarded  : ${item.type} : rewarded : ${item.amount}")
                        }

                        override fun isEnableAds(isAds: Boolean) {
                            logD("MainActivity isEnableAds : $isAds")
                        }

                        override fun isOffline(offline: Boolean) {
                            logD("Ads is Offline : $offline")
                        }
                    })
        */
    }

    private fun openAds() {
        MediationOpenAd.loadAppOpenAd(this, false, object : OpenAddCallback {
            override fun onDismissClick() {
                logD("MainActivity onDismissClick")
            }

            override fun onErrorToShow(error: String) {
                logD("MainActivity onErrorToShow Error : $error")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
    }

    private fun interstitialAds() {
//        progressDialogUtils = ProgressDialogUtils(this)
//        progressDialogUtils!!.showDialog("Loading", "Wait while ad is loading...")

        /*MediationAdInterstitial.showInterstitialAd(this, false, object : InterstitialAdsListener {
            override fun onLoaded(adType: Int) {
                progressDialogUtils!!.isShowDialog()
                logD("MainActivity onLoaded : $adType")
            }

            override fun onClicked(adType: Int) {
                logD("MainActivity onClicked : $adType")
            }

            override fun onBeforeAdShow() {
                logD("MainActivity onBeforeAdShow ")
            }

            override fun onDismisses(adType: Int) {
                logD("MainActivity onDismisses : $adType")
            }

            override fun onError(error: String) {
                logD("MainActivity onError : $error")
            }

            override fun isEnableAds(isAds: Boolean) {
                if (isAds.not()) {
                    progressDialogUtils!!.isShowDialog()
                }
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                progressDialogUtils!!.isShowDialog()
                logD("Ads is Offline : $offline")
            }
        })*/

        MediationAdInterstitial.showInterstitialAds(
            this,
            false,
            object : MediationAdInterstitial.AdsShowCallback {
                override fun onAdsOff() {
                    logD("MainActivity Interstitial Ads is off")
                }

                override fun onAdsError(error: String) {
                    logD("MainActivity Interstitial Ads :error")
                }

                override fun onAdsClicked() {
                    logD("MainActivity Interstitial Ads clieck")
                }

                override fun onAdsDismiss() {
                    logD("MainActivity Interstitial Ads dismiss")
                }

                override fun onAdsImpress() {
                    logD("MainActivity Interstitial Ads Impress")
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
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        AppPurchaseUtils.clientDestroy()
    }
}
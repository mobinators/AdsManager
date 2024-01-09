package com.mobinators.ads.managers

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.facebook.ads.AdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.mobinators.ads.manager.extensions.createThumbNail
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.listener.BannerAdListener
import com.mobinators.ads.manager.ui.commons.listener.ImageProvider
import com.mobinators.ads.manager.ui.commons.listener.InterstitialAdsListener
import com.mobinators.ads.manager.ui.commons.listener.OnNativeAdListener
import com.mobinators.ads.manager.ui.commons.listener.OnRewardedAdListener
import com.mobinators.ads.manager.ui.commons.listener.OpenAddCallback
import com.mobinators.ads.manager.ui.commons.nativeBanner.MediationNativeBanner
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAd
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import com.mobinators.ads.managers.databinding.ActivityMaxAdsBinding
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.ui.commons.base.BaseActivity

class MaxAdsActivity : BaseActivity<ActivityMaxAdsBinding>(), View.OnClickListener {
    private var mediationNativeAd: MediationNativeAd? = null
    override fun getActivityView() = ActivityMaxAdsBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        AdsUtils.maxTestAds(this)
        binding.maxBanner.setOnClickListener(this)
        binding.maxNative.setOnClickListener(this)
        binding.maxNativeBanner.setOnClickListener(this)
        binding.maxRewarded.setOnClickListener(this)
        binding.maxInterstitial.setOnClickListener(this)
        binding.maxOpen.setOnClickListener(this)
        binding.testAdsEnable.setOnClickListener(this)
        mediationNativeAd = MediationNativeAd(this, false, binding.maxContainer)
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
//    BannerAdMediation.maxTestAds(this@MaxAdsActivity)

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
        mediationNativeAd!!.loadAd(object : OnNativeAdListener {
            override fun onError(error: String) {
                logD("MaxAdsActivity Error : $error")
            }

            override fun onLoaded(adType: Int) {
                logD("MaxAdsActivity Ads Type : $adType")
            }

            override fun onAdClicked(adType: Int) {
                logD("MaxAdsActivity Ads Type : $adType")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MaxAdsActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
    }

    private fun nativeBannerAds() {
        MediationNativeBanner.loadAd(
            this,
            false,
            binding.maxContainer,
            object : OnNativeAdListener {
                override fun onError(error: String) {
                    logD("MaxAdsActivity Error : $error")
                }

                override fun onLoaded(adType: Int) {
                    logD("MaxAdsActivity Ads Type : $adType")
                }

                override fun onAdClicked(adType: Int) {
                    logD("MaxAdsActivity Ads Type : $adType")
                }

                override fun isEnableAds(isAds: Boolean) {
                    logD("MaxAdsActivity isEnableAds : $isAds")
                }

                override fun isOffline(offline: Boolean) {
                    logD("Ads is Offline : $offline")
                }
            },
            object : ImageProvider {
                override fun onProviderImage(imageView: ImageView, imageUrl: String) {
                    imageView.createThumbNail(this@MaxAdsActivity, imageUrl)
                }
            })
    }

    private fun rewardedAds() {
        MediationRewardedAd.loadRewardedAd(this, false, object : OnRewardedAdListener {
            override fun onError(error: String) {
                logD("MaxAdsActivity onError Error : $error")
            }

            override fun onAdLoaded(adType: Int) {
                logD("MaxAdsActivity onAdLoaded Ads Type : $adType")
            }

            override fun onClicked(adType: Int) {
                logD("MaxAdsActivity onClicked Ads Type : $adType")
            }

            override fun onDismissClick(adType: Int, item: RewardItem) {
                logD("MaxAdsActivity onDismissClick Ads Type : $adType")
            }

            override fun onCancel(adType: Int) {
                logD("MaxAdsActivity onCancel Ads Type: $adType")
            }

            override fun onRewarded(item: RewardItem) {
                logD("MaxAdsActivity onRewarded  : ${item.type} : rewarded : ${item.amount}")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MaxAdsActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
    }

    private fun interstitialAds() {
        MediationAdInterstitial.showInterstitialAd(this, false, object : InterstitialAdsListener {
            override fun onLoaded(adType: Int) {
                logD("MaxAdsActivity onLoaded : $adType")
            }

            override fun onClicked(adType: Int) {
                logD("MaxAdsActivity onClicked : $adType")
            }

            override fun onBeforeAdShow() {
                logD("MaxAdsActivity onBeforeAdShow ")
            }

            override fun onDismisses(adType: Int) {
                logD("MaxAdsActivity onDismisses : $adType")
            }

            override fun onError(error: String) {
                logD("MaxAdsActivity onError : $error")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MaxAdsActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
    }

    private fun openAds() {
        MediationOpenAd.loadAppOpenAd(this, false, object : OpenAddCallback {
            override fun onDismissClick() {
                logD("MaxAdsActivity onDismissClick")
            }

            override fun onErrorToShow(error: String) {
                logD("MaxAdsActivity onErrorToShow Error : $error")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MaxAdsActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
    }
}
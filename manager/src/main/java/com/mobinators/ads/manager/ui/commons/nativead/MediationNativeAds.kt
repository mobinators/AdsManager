package com.mobinators.ads.manager.ui.commons.nativead

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.databinding.AdmobNativeAdLayoutBinding
import com.mobinators.ads.manager.databinding.CustomNativeBinding
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logException

@SuppressLint("StaticFieldLeak")
object MediationNativeAds {
    private var loadAdsCallback: NativeLoadAdsCallback? = null
    private var showAdsCallback: ShowNativeAdsCallback? = null
    private var maxNativeAdView: MaxNativeAdView? = null
    private var maxNativeAds: MaxNativeAdLoader? = null
    private var builder: AdLoader.Builder? = null
    private var containerView: ViewGroup? = null
    private var isCustomAdsView: Boolean = false
    private var admobNativeAd: NativeAd? = null
    private var maxNativeAdsKey: String? = null
    private var activityRef: Activity? = null
    private var nativeAdsKey: String? = null
    private var contextRef: Context? = null
    private var maxNativeAd: MaxAd? = null
    fun loadNativeAds(context: Context, isPurchased: Boolean, listener: NativeLoadAdsCallback) {
        this.contextRef = context
        this.loadAdsCallback = listener
        if (isPurchased) {
            this.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.NETWORK_OFF)
            return
        }
        initSelectedNativeAds()
    }

    fun showNativeAds(
        activity: Activity,
        isPurchased: Boolean,
        containerView: ViewGroup,
        listener: ShowNativeAdsCallback,
        isCustomView: Boolean = false
    ) {
        this.activityRef = activity
        this.showAdsCallback = listener
        this.isCustomAdsView = isCustomView
        if (isPurchased) {
            this.showAdsCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
        showSelectedNativeAds()
    }

    private fun initSelectedNativeAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.loadAdsCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> initAdmobNativeAds()
                AdsConstants.AD_MOB -> initAdmobNativeAds()
                AdsConstants.MAX_MEDIATION -> initMaxNativeAds()
                else -> this.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }
        } catch (error: Exception) {
            logException(" Init Selected Native Ads Error : ${error.localizedMessage}")
        }
    }

    private fun initAdmobNativeAds() {
        try {
            this.nativeAdsKey = if (AdsConstants.testMode) {
                AdsConstants.TEST_ADMOB_NATIVE_ID
            } else {
                if (AdsApplication.getAdsModel()!!.admobMediation) {
                    AdsApplication.getAdsModel()!!.admobMediationNativeId
                } else {
                    AdsApplication.getAdsModel()!!.admobNativeID
                }
            }
            if (this.nativeAdsKey.isNullOrEmpty() || this.nativeAdsKey.isNullOrBlank()) {
                this.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.nativeAdsKey == AdsConstants.TEST_ADMOB_NATIVE_ID) {
                    this.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            builder = AdLoader.Builder(this.contextRef!!, this.nativeAdsKey!!)
            /* builder.forNativeAd {
                 val activityDestroy = (contextRef as Activity).isDestroyed
                 val activity = (contextRef as Activity)
                 if (activityDestroy || activity.isFinishing || activity.isChangingConfigurations) {
                     it.destroy()
                     return@forNativeAd
                 }
                 this.admobNativeAd?.destroy()
                 this.admobNativeAd = it
                 bindAdmobContentAd()
             }*/
            val videoOption = VideoOptions.Builder().setStartMuted(true).build()
            val nativeAdOption = NativeAdOptions.Builder().setVideoOptions(videoOption).build()
            this.builder!!.withNativeAdOptions(nativeAdOption)
            val adLoader = this.builder!!.withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    this@MediationNativeAds.loadAdsCallback!!.onAdsLoaded()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    this@MediationNativeAds.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                }

                override fun onAdSwipeGestureClicked() {
                    super.onAdSwipeGestureClicked()
                }
            }).build()
            adLoader.loadAd(AdsApplication.getAdRequest())
        } catch (error: Exception) {
            logException(" Init Admob Native Ads Error : ${error.localizedMessage}")
        }
    }

    private fun initMaxNativeAds() {
        try {
            this.maxNativeAdsKey = if (AdsConstants.testMode) {
                AdsConstants.TEST_MAX_Native_ADS_ID
            } else {
                AdsApplication.getAdsModel()!!.maxNativeID
            }
            if (this.maxNativeAdsKey.isNullOrEmpty() || this.maxNativeAdsKey.isNullOrBlank()) {
                this.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.maxNativeAdsKey == AdsConstants.TEST_MAX_Native_ADS_ID) {
                    this.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            bindMaxContentAd()
            this.maxNativeAds = MaxNativeAdLoader(this.maxNativeAdsKey!!, this.contextRef!!)
            this.maxNativeAds!!.setRevenueListener { ad ->
                val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                adjustAdRevenue.setRevenue(ad.revenue, "USD")
                adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
                adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
                adjustAdRevenue.setAdRevenuePlacement(ad.placement)
                Adjust.trackAdRevenue(adjustAdRevenue)
            }
            this.maxNativeAds!!.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(p0: MaxNativeAdView?, p1: MaxAd) {
                    super.onNativeAdLoaded(p0, p1)
                    if (this@MediationNativeAds.maxNativeAd != null) {
                        maxNativeAds!!.destroy(this@MediationNativeAds.maxNativeAd!!)
                    }
                    this@MediationNativeAds.maxNativeAd = p1
                    this@MediationNativeAds.containerView!!.removeAllViews()
                    this@MediationNativeAds.containerView?.addView(p0)
                    this@MediationNativeAds.loadAdsCallback?.onAdsLoaded()
                }

                override fun onNativeAdLoadFailed(p0: String, p1: MaxError) {
                    super.onNativeAdLoadFailed(p0, p1)
                    this@MediationNativeAds.containerView?.gone()
                    this@MediationNativeAds.loadAdsCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                }

                override fun onNativeAdClicked(p0: MaxAd) {
                    super.onNativeAdClicked(p0)
                }

                override fun onNativeAdExpired(p0: MaxAd) {
                    super.onNativeAdExpired(p0)
                }
            })
            this.maxNativeAds!!.loadAd(this.maxNativeAdView)
        } catch (error: Exception) {
            logException("Init Max Native Ads Error : ${error.localizedMessage}")
        }
    }


    private fun showSelectedNativeAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.showAdsCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> bindAdmobContentAd()
                AdsConstants.AD_MOB -> bindAdmobContentAd()
                AdsConstants.MAX_MEDIATION -> bindMaxContentAd()
                else -> this.showAdsCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }
        } catch (error: Exception) {
            logException(" Show Selected Native Ads Error : ${error.localizedMessage}")
        }
    }


    private fun bindAdmobContentAd() {
        this.builder!!.forNativeAd {
            val activityDestroy = this.activityRef!!.isDestroyed
            if (activityDestroy || this.activityRef!!.isFinishing || this.activityRef!!.isChangingConfigurations) {
                it.destroy()
                return@forNativeAd
            }
            this.admobNativeAd?.destroy()
            this.admobNativeAd = it
            bindAdmobContentAd()
        }
        if (this.admobNativeAd != null) {
            this.containerView!!.removeAllViews()
            if (this.isCustomAdsView) {
                val customNative = CustomNativeBinding.inflate(
                    LayoutInflater.from(this.activityRef!!),
                    this.containerView!!,
                    false
                )
                customNativeAdsBinding(customBinding = customNative)
            } else {
                val admobNativeAdLayoutBinding = AdmobNativeAdLayoutBinding.inflate(
                    LayoutInflater.from(this.activityRef!!),
                    this.containerView!!,
                    false
                )
                nativeAdsViewBinding(nativeAdBinding = admobNativeAdLayoutBinding)
            }
        }
    }

    private fun bindMaxContentAd() {

    }

    private fun customNativeAdsBinding(customBinding: CustomNativeBinding) {

    }

    private fun nativeAdsViewBinding(nativeAdBinding: AdmobNativeAdLayoutBinding) {

    }

    fun onDestroy() {
        try {
            if (this.admobNativeAd != null) {
                this.admobNativeAd!!.destroy()
            }
            if (this.maxNativeAds != null) {
                this.maxNativeAds!!.destroy()
            }
        } catch (error: Exception) {
            logException("On Destroy native Ads Error : ${error.localizedMessage}")
        }
    }

    interface NativeLoadAdsCallback {
        fun onAdsOff()
        fun onAdsLoaded()
        fun onAdsError(errorState: AdsErrorState)
    }

    interface ShowNativeAdsCallback {
        fun onAdsOff()
        fun onAdsDisplay()
        fun onAdsError(errorState: AdsErrorState)
    }
}
package com.mobinators.ads.manager.ui.commons.nativead

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.databinding.AdmobNativeAdLayoutBinding
import com.mobinators.ads.manager.databinding.CustomNativeBinding
import com.mobinators.ads.manager.ui.commons.enums.AdsLoadingState
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logException
import pak.developer.app.managers.extensions.visible

@SuppressLint("StaticFieldLeak")
object MediationNativeAds {
    private var loadAdsCallback: NativeLoadAdsCallback? = null
    private var showAdsCallback: ShowNativeAdsCallback? = null
    private var maxNativeAdView: MaxNativeAdView? = null
    private var maxNativeAds: MaxNativeAdLoader? = null
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
            this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.NETWORK_OFF)
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
        this.containerView = containerView
        if (isPurchased) {
            this.showAdsCallback!!.onAdsShowState(adsShowState = AdsShowState.APP_PURCHASED)
            return
        }
        showSelectedNativeAds()
    }

    private fun initSelectedNativeAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_OFF)
                AdsConstants.AD_MOB_MEDIATION -> initAdmobNativeAds()
                AdsConstants.AD_MOB -> initAdmobNativeAds()
                AdsConstants.MAX_MEDIATION -> initMaxNativeAds()
                else -> this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_STRATEGY_WRONG)
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
                this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.nativeAdsKey == AdsConstants.TEST_ADMOB_NATIVE_ID) {
                    this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.TEST_ADS_ID)
                    return
                }
            }
            val builder = AdLoader.Builder(this.contextRef!!, this.nativeAdsKey!!)
            builder.forNativeAd {
                try {
                    val activityDestroy = this.activityRef!!.isDestroyed
                    val activity = this.activityRef!!
                    if (activityDestroy || activity.isFinishing || activity.isChangingConfigurations) {
                        it.destroy()
                        return@forNativeAd
                    }
                } catch (error: Exception) {
                    logException("Activity Ref is null")
                }
                this.admobNativeAd?.destroy()
                this.admobNativeAd = it
            }
            val videoOption = VideoOptions.Builder().setStartMuted(true).build()
            val nativeAdOption = NativeAdOptions.Builder().setVideoOptions(videoOption).build()
            builder.withNativeAdOptions(nativeAdOption)
            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    this@MediationNativeAds.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOADED)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    this@MediationNativeAds.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOAD_FAILED)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    this@MediationNativeAds.showAdsCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_CLICKED)
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    this@MediationNativeAds.showAdsCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_CLOSED)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    this@MediationNativeAds.showAdsCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_IMPRESS)
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    this@MediationNativeAds.showAdsCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_OPEN)
                }

                override fun onAdSwipeGestureClicked() {
                    super.onAdSwipeGestureClicked()
                    this@MediationNativeAds.showAdsCallback?.onAdsSwipe()
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
                this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.maxNativeAdsKey == AdsConstants.TEST_MAX_Native_ADS_ID) {
                    this.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.TEST_ADS_ID)
                    return
                }
            }
            /* try {
                 bindMaxContentAd()
             } catch (error: Exception) {
                 logException("Error : ${error.message}")
             }*/
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
                    this@MediationNativeAds.loadAdsCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOADED)
                }

                override fun onNativeAdLoadFailed(p0: String, p1: MaxError) {
                    super.onNativeAdLoadFailed(p0, p1)
                    this@MediationNativeAds.containerView?.gone()
                    this@MediationNativeAds.loadAdsCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOAD_FAILED)
                }

                override fun onNativeAdClicked(p0: MaxAd) {
                    super.onNativeAdClicked(p0)
                    this@MediationNativeAds.showAdsCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_CLICKED)
                }

                override fun onNativeAdExpired(p0: MaxAd) {
                    super.onNativeAdExpired(p0)
                    this@MediationNativeAds.showAdsCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_CLOSED)
                }
            })
//            this.maxNativeAds!!.loadAd(this.maxNativeAdView)
        } catch (error: Exception) {
            logException("Init Max Native Ads Error : ${error.localizedMessage}")
        }
    }


    private fun showSelectedNativeAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.showAdsCallback!!.onAdsShowState(adsShowState = AdsShowState.ADS_OFF)
                AdsConstants.AD_MOB_MEDIATION -> bindAdmobContentAd()
                AdsConstants.AD_MOB -> bindAdmobContentAd()
                AdsConstants.MAX_MEDIATION -> bindMaxContentAd()
                else -> this.showAdsCallback!!.onAdsShowState(adsShowState = AdsShowState.ADS_STRATEGY_WRONG)
            }
        } catch (error: Exception) {
            logException(" Show Selected Native Ads Error : ${error.localizedMessage}")
        }
    }


    private fun bindAdmobContentAd() {
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
            initSelectedNativeAds()
        } else {
            initSelectedNativeAds()
        }
    }

    private fun bindMaxContentAd() {
        try {
            /* val binder: MaxNativeAdViewBinder =
                 MaxNativeAdViewBinder.Builder(R.layout.max_native_ad_layout)
                     .setTitleTextViewId(R.id.title_text_view)
                     .setBodyTextViewId(R.id.body_text_view)
                     .setAdvertiserTextViewId(R.id.advertiser_textView)
                     .setIconImageViewId(R.id.icon_image_view)
                     .setMediaContentViewGroupId(R.id.media_view_container)
                     .setOptionsContentViewGroupId(R.id.options_view)
                     .setCallToActionButtonId(R.id.cta_button)
                     .build()
             this.maxNativeAdView = MaxNativeAdView(binder, this.activityRef!!)*/
            this.maxNativeAds!!.loadAd()
            initSelectedNativeAds()
        } catch (error: Exception) {
            initSelectedNativeAds()
            logException("showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun customNativeAdsBinding(customBinding: CustomNativeBinding) {
        try {
            this.containerView!!.addView(customBinding.root)
            val nativeAdView = customBinding.root
            nativeAdView.mediaView = customBinding.adMedia
            nativeAdView.headlineView = customBinding.adHeadline
            nativeAdView.bodyView = customBinding.adBody
            nativeAdView.callToActionView = customBinding.adCallToAction
            nativeAdView.iconView = customBinding.adAppIcon
            nativeAdView.priceView = customBinding.adPrice
            nativeAdView.starRatingView = customBinding.adStars
            nativeAdView.storeView = customBinding.adStore
            nativeAdView.advertiserView = customBinding.adAdvertiser
            customBinding.adHeadline.text = this.admobNativeAd!!.headline
            this.admobNativeAd!!.mediaContent?.let { customBinding.adMedia.mediaContent = it }
            if (this.admobNativeAd!!.callToAction == null) {
                customBinding.adCallToAction.gone()
            } else {
                customBinding.adCallToAction.visible()
                customBinding.adCallToAction.text = this.admobNativeAd!!.callToAction
            }

            if (this.admobNativeAd!!.icon == null) {
                customBinding.adAppIcon.gone()
            } else {
                customBinding.adAppIcon.setImageDrawable(this.admobNativeAd!!.icon?.drawable)
                customBinding.adAppIcon.visible()
            }

            if (this.admobNativeAd!!.price == null) {
                customBinding.adPrice.gone()
            } else {
                customBinding.adPrice.visible()
                customBinding.adPrice.text = this.admobNativeAd!!.price
            }
            if (this.admobNativeAd!!.store == null) {
                customBinding.adStore.gone()
            } else {
                customBinding.adStore.visible()
                customBinding.adStore.text = this.admobNativeAd!!.store
            }
            if (this.admobNativeAd!!.starRating == null) {
                customBinding.adStars.gone()
            } else {
                customBinding.adStars.rating = this.admobNativeAd!!.starRating!!.toFloat()
                customBinding.adStars.visible()
            }
            if (this.admobNativeAd!!.advertiser == null) {
                customBinding.adAdvertiser.gone()
            } else {
                customBinding.adAdvertiser.text = this.admobNativeAd!!.advertiser
                customBinding.adAdvertiser.visible()
            }
            nativeAdView.setNativeAd(this.admobNativeAd!!)
            val vc = this.admobNativeAd!!.mediaContent?.videoController
            if (vc != null && vc.hasVideoContent()) {
                vc.videoLifecycleCallbacks =
                    object : VideoController.VideoLifecycleCallbacks() {
                        @SuppressLint("SetTextI18n")
                        override fun onVideoEnd() {
                            logException("Video status: Video playback has ended.")
                            super.onVideoEnd()
                        }
                    }
            } else {
                logException("Video status: Ad does not contain a video asset.")
            }
        } catch (error: Exception) {
            logException("showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun nativeAdsViewBinding(nativeAdBinding: AdmobNativeAdLayoutBinding) {
        this.containerView!!.addView(nativeAdBinding.root)
        val nativeAdView = nativeAdBinding.root
        nativeAdView.mediaView = nativeAdBinding.adMedia
        nativeAdView.headlineView = nativeAdBinding.adHeadline
        nativeAdView.bodyView = nativeAdBinding.adBody
        nativeAdView.callToActionView = nativeAdBinding.adAction
        nativeAdView.iconView = nativeAdBinding.squareIcon
        nativeAdView.priceView = nativeAdBinding.adPrice
        nativeAdView.starRatingView = nativeAdBinding.adStars
        nativeAdView.storeView = nativeAdBinding.adStore
        nativeAdView.advertiserView = nativeAdBinding.advertiser
        nativeAdBinding.adHeadline.text = admobNativeAd!!.headline
        admobNativeAd!!.mediaContent?.let {
            nativeAdBinding.adMedia.mediaContent = it
        }
        if (admobNativeAd!!.body == null) {
            nativeAdBinding.adBody.visibility = View.INVISIBLE
        } else {
            nativeAdBinding.adBody.visibility = View.VISIBLE
            nativeAdBinding.adBody.text = admobNativeAd!!.body
        }

        if (admobNativeAd!!.callToAction == null) {
            nativeAdBinding.adAction.gone()
        } else {
            nativeAdBinding.adAction.visible()
            nativeAdBinding.adAction.text = admobNativeAd!!.callToAction
        }

        if (admobNativeAd!!.icon == null) {
            nativeAdBinding.squareIcon.gone()
        } else {
            nativeAdBinding.squareIcon.setImageDrawable(admobNativeAd!!.icon?.drawable)
            nativeAdBinding.squareIcon.visible()
        }

        if (admobNativeAd!!.price == null) {
            nativeAdBinding.adPrice.gone()
        } else {
            nativeAdBinding.adPrice.visible()
            nativeAdBinding.adPrice.text = admobNativeAd!!.price
        }
        if (admobNativeAd!!.store == null) {
            nativeAdBinding.adStore.gone()
        } else {
            nativeAdBinding.adStore.visible()
            nativeAdBinding.adStore.text = admobNativeAd!!.store
        }
        if (admobNativeAd!!.starRating == null) {
            nativeAdBinding.adStars.gone()
        } else {
            nativeAdBinding.adStars.rating = admobNativeAd!!.starRating!!.toFloat()
            nativeAdBinding.adStars.visible()
        }
        if (admobNativeAd!!.advertiser == null) {
            nativeAdBinding.advertiser.gone()
        } else {
            nativeAdBinding.advertiser.text = admobNativeAd!!.advertiser
            nativeAdBinding.advertiser.visible()
        }
        nativeAdView.setNativeAd(admobNativeAd!!)
        val vc = admobNativeAd!!.mediaContent?.videoController
        if (vc != null && vc.hasVideoContent()) {
            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    @SuppressLint("SetTextI18n")
                    override fun onVideoEnd() {
                        logException("Video status: Video playback has ended.")
                        super.onVideoEnd()
                    }
                }
        } else {
            logException("Video status: Ad does not contain a video asset.")
        }
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
        fun onAdsLoadState(adsLoadingState: AdsLoadingState)
    }

    interface ShowNativeAdsCallback {
        fun onAdsSwipe()
        fun onAdsShowState(adsShowState: AdsShowState)
    }
}
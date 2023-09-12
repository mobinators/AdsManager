package com.mobinators.ads.manager.ui.commons.nativead

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
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
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.databinding.AdmobNativeAdLayoutBinding
import com.mobinators.ads.manager.ui.commons.listener.OnNativeAdListener
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.visible

class MediationNativeAd(
    private var activity: Activity,
    private var isPurchased: Boolean,
    private var itemView: ViewGroup
) {
    private var onNativeAdListener: OnNativeAdListener? = null
    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var maxNativeView: MaxNativeAdView? = null
    private var admobNativeAd: NativeAd? = null
    private var admobKey: String? = null
    private var nativeAd: MaxAd? = null
    private var maxKey: String? = null

    fun onDestroy() {
        if (this.admobNativeAd != null) {
            this.admobNativeAd!!.destroy()
        }
        if (this.nativeAdLoader != null) {
            this.nativeAdLoader!!.destroy(nativeAd)
        }
    }

    fun loadAd(listener: OnNativeAdListener) {
        this.onNativeAdListener = listener
        if (isPurchased) {
            listener.onError("You have pro version!")
            return
        }
        if (AdsConstants.isInit.not()) {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                loadAd(listener)
            }, 1500)
        }
        this.admobKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_ADMOB_NATIVE_ID
        } else {
            if (AdsApplication.getAdsModel()!!.admobMediation) {
                AdsApplication.getAdsModel()!!.admobMediationNativeId
            } else {
                AdsApplication.getAdsModel()!!.admobNativeID
            }
        }
        this.maxKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_MAX_Native_ADS_ID
        } else {
            AdsApplication.getAdsModel()!!.maxNativeID
        }
        selectAd()
    }

    private fun selectAd() {
        when (AdsApplication.getAdsModel()!!.strategy.toInt()) {
            AdsConstants.ADS_OFF -> {
                this.onNativeAdListener!!.isEnableAds(false)
            }

            AdsConstants.AD_MOB_MEDIATION -> {
                this.onNativeAdListener!!.isEnableAds(true)
                if (this.admobNativeAd != null) {
                    bindAdmobContentAd()
                }
                selectAdmobAd()
            }

            AdsConstants.AD_MOB -> {
                this.onNativeAdListener!!.isEnableAds(true)
                if (this.admobNativeAd != null) {
                    bindAdmobContentAd()
                }
                selectAdmobAd()
            }

            AdsConstants.MAX_MEDIATION -> {
                this.onNativeAdListener!!.isEnableAds(true)
                if (this.maxNativeView != null) {
                    bindMaxContentAd()
                }
                selectMaxAd()
            }
        }
    }

    private fun bindAdmobContentAd() {
        try {
            if (this.admobNativeAd == null) {
                onLoadAdError("ADMOB nativeContentAd is null")
                return
            }
            itemView.removeAllViews()
            val admobNativeAdLayoutBinding =
                AdmobNativeAdLayoutBinding.inflate(LayoutInflater.from(activity), itemView, false)
            itemView.addView(admobNativeAdLayoutBinding.root)
            val nativeAdView = admobNativeAdLayoutBinding.root
            nativeAdView.mediaView = admobNativeAdLayoutBinding.adMedia
            nativeAdView.headlineView = admobNativeAdLayoutBinding.adHeadline
            nativeAdView.bodyView = admobNativeAdLayoutBinding.adBody
            nativeAdView.callToActionView = admobNativeAdLayoutBinding.adAction
            nativeAdView.iconView = admobNativeAdLayoutBinding.squareIcon
            nativeAdView.priceView = admobNativeAdLayoutBinding.adPrice
            nativeAdView.starRatingView = admobNativeAdLayoutBinding.adStars
            nativeAdView.storeView = admobNativeAdLayoutBinding.adStore
            nativeAdView.advertiserView = admobNativeAdLayoutBinding.advertiser
            admobNativeAdLayoutBinding.adHeadline.text = admobNativeAd!!.headline
            admobNativeAd!!.mediaContent?.let {
                admobNativeAdLayoutBinding.adMedia.mediaContent = it
            }
            if (admobNativeAd!!.body == null) {
                admobNativeAdLayoutBinding.adBody.visibility = View.INVISIBLE
            } else {
                admobNativeAdLayoutBinding.adBody.visibility = View.VISIBLE
                admobNativeAdLayoutBinding.adBody.text = admobNativeAd!!.body
            }

            if (admobNativeAd!!.callToAction == null) {
                admobNativeAdLayoutBinding.adAction.gone()
            } else {
                admobNativeAdLayoutBinding.adAction.visible()
                admobNativeAdLayoutBinding.adAction.text = admobNativeAd!!.callToAction
            }

            if (admobNativeAd!!.icon == null) {
                admobNativeAdLayoutBinding.squareIcon.gone()
            } else {
                admobNativeAdLayoutBinding.squareIcon.setImageDrawable(admobNativeAd!!.icon?.drawable)
                admobNativeAdLayoutBinding.squareIcon.visible()
            }

            if (admobNativeAd!!.price == null) {
                admobNativeAdLayoutBinding.adPrice.gone()
            } else {
                admobNativeAdLayoutBinding.adPrice.visible()
                admobNativeAdLayoutBinding.adPrice.text = admobNativeAd!!.price
            }
            if (admobNativeAd!!.store == null) {
                admobNativeAdLayoutBinding.adStore.gone()
            } else {
                admobNativeAdLayoutBinding.adStore.visible()
                admobNativeAdLayoutBinding.adStore.text = admobNativeAd!!.store
            }
            if (admobNativeAd!!.starRating == null) {
                admobNativeAdLayoutBinding.adStars.gone()
            } else {
                admobNativeAdLayoutBinding.adStars.rating = admobNativeAd!!.starRating!!.toFloat()
                admobNativeAdLayoutBinding.adStars.visible()
            }
            if (admobNativeAd!!.advertiser == null) {
                admobNativeAdLayoutBinding.advertiser.gone()
            } else {
                admobNativeAdLayoutBinding.advertiser.text = admobNativeAd!!.advertiser
                admobNativeAdLayoutBinding.advertiser.visible()
            }
            nativeAdView.setNativeAd(admobNativeAd!!)
            val vc = admobNativeAd!!.mediaContent?.videoController
            if (vc != null && vc.hasVideoContent()) {
                vc.videoLifecycleCallbacks =
                    object : VideoController.VideoLifecycleCallbacks() {
                        @SuppressLint("SetTextI18n")
                        override fun onVideoEnd() {
                            onLoadAdError("Video status: Video playback has ended.")
                            super.onVideoEnd()
                        }
                    }
            } else {
                onLoadAdError("Video status: Ad does not contain a video asset.")
            }
        } catch (error: Exception) {
            onLoadAdError(error.localizedMessage!!)
        }
    }

    private fun selectAdmobAd() {
        if (this.admobKey!!.isEmpty() || this.admobKey!!.isBlank()) {
            onLoadAdError("Empty is found")
            return
        }
        if (AdsApplication.isAdmobInLimit()) {
            if (AdsApplication.applyLimitOnAdmob) {
                onLoadAdError("Native admob banned due to admob in limit")
                return
            }
        }
        if (AdsUtils.isOnline(this.activity).not()) {
            logD("is Offline ")
            itemView.gone()
            this.onNativeAdListener!!.isOffline(true)
            return
        }
        if (this.admobKey == AdsConstants.TEST_ADMOB_NATIVE_ID) {
            logD("Test Ids")
            if (AdsConstants.testMode.not()) {
                logD("NULL OR TEST IDS FOUND")
                this.onNativeAdListener!!.onError("NULL OR TEST IDS FOUND")
                return
            }
        }
        val builder = AdLoader.Builder(activity, this.admobKey!!)
        builder.forNativeAd { nativeAd ->
            var activityDestroyed: Boolean = false
            activityDestroyed = activity.isDestroyed
            if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                nativeAd.destroy()
                return@forNativeAd
            }
            this.admobNativeAd?.destroy()
            this.admobNativeAd = nativeAd
            bindAdmobContentAd()
        }
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOption = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOption)
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                super.onAdFailedToLoad(errorCode)
                onLoadAdError(errorCode.message)
                AdsConstants.admobRequestNativeFailed++
                if (AdsApplication.isAdmobInLimit()) {
                    AdsApplication.applyLimitOnAdmob = true
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                if (onNativeAdListener != null) {
                    onNativeAdListener!!.onLoaded(AdsConstants.AD_MOB)
                }
            }

            override fun onAdOpened() {
                super.onAdOpened()
                if (onNativeAdListener != null) {
                    onNativeAdListener!!.onAdClicked(AdsConstants.AD_MOB)
                }
            }

            override fun onAdClicked() {
                super.onAdClicked()
                if (onNativeAdListener != null) {
                    onNativeAdListener!!.onAdClicked(AdsConstants.AD_MOB)
                }
            }
        }).build()
        AdsConstants.adMobNativeAdLoad++
        adLoader.loadAd(AdsApplication.getAdRequest())
    }

    private fun bindMaxContentAd() {
        val binder: MaxNativeAdViewBinder =
            MaxNativeAdViewBinder.Builder(R.layout.max_native_ad_layout)
                .setTitleTextViewId(R.id.title_text_view)
                .setBodyTextViewId(R.id.body_text_view)
                .setAdvertiserTextViewId(R.id.advertiser_textView)
                .setIconImageViewId(R.id.icon_image_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.options_view)
                .setCallToActionButtonId(R.id.cta_button)
                .build()
        maxNativeView = MaxNativeAdView(binder, activity)
    }

    private fun selectMaxAd() {
        if (this.maxKey!!.isEmpty() || this.maxKey!!.isBlank()) {
            onLoadAdError("Empty is found")
            return
        }
        if (AdsUtils.isOnline(this.activity).not()) {
            logD("is Offline ")
            itemView.gone()
            this.onNativeAdListener!!.isOffline(true)
            return
        }
        if (this.maxKey == AdsConstants.TEST_MAX_Native_ADS_ID) {
            logD("Test Ids")
            if (AdsConstants.testMode.not()) {
                logD("NULL OR TEST IDS FOUND")
                this.onNativeAdListener!!.onError("NULL OR TEST IDS FOUND")
                return
            }
        }
        bindMaxContentAd()
        nativeAdLoader = MaxNativeAdLoader(this.maxKey!!, activity)
        nativeAdLoader!!.setRevenueListener { ad ->
            val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
            adjustAdRevenue.setRevenue(ad?.revenue, "USD")
            adjustAdRevenue.setAdRevenueNetwork(ad?.networkName)
            adjustAdRevenue.setAdRevenueUnit(ad?.adUnitId)
            adjustAdRevenue.setAdRevenuePlacement(ad?.placement)
            Adjust.trackAdRevenue(adjustAdRevenue)
        }

        nativeAdLoader!!.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(p0: MaxNativeAdView?, ad: MaxAd?) {
                super.onNativeAdLoaded(p0, ad)
                if (nativeAd != null) {
                    nativeAdLoader!!.destroy(nativeAd)
                }
                nativeAd = ad
                itemView.removeAllViews()
                itemView.addView(p0)
                onNativeAdListener!!.onLoaded(AdsConstants.MAX_MEDIATION)
            }

            override fun onNativeAdLoadFailed(p0: String?, p1: MaxError?) {
                super.onNativeAdLoadFailed(p0, p1)
                itemView.gone()
                onNativeAdListener!!.onError(p1!!.message)
            }

            override fun onNativeAdExpired(p0: MaxAd?) {
                super.onNativeAdExpired(p0)
            }

            override fun onNativeAdClicked(p0: MaxAd?) {
                super.onNativeAdClicked(p0)
                onNativeAdListener!!.onAdClicked(AdsConstants.MAX_MEDIATION)
            }
        })
        nativeAdLoader!!.loadAd(maxNativeView)
    }


    private fun onLoadAdError(error: String) {
        if (this.onNativeAdListener != null) {
            this.onNativeAdListener!!.onError(error)
        }
    }
}
package com.mobinators.ads.manager.ui.commons.nativeBanner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
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
import com.google.android.gms.ads.MediaContent
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.databinding.AdmobNativeBannerAdLayoutBinding
import com.mobinators.ads.manager.extensions.createThumbNail
import com.mobinators.ads.manager.ui.commons.listener.ImageProvider
import com.mobinators.ads.manager.ui.commons.listener.OnNativeAdListener
import com.mobinators.ads.manager.ui.commons.models.NativeBannerModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.visible


@SuppressLint("StaticFieldLeak")
object MediationNativeBanner {
    private var nativeBannerBinding: AdmobNativeBannerAdLayoutBinding? = null
    private var onNativeAdListener: OnNativeAdListener? = null
    private var maxNativeAdLoader: MaxNativeAdLoader? = null
    private var maxNativeAdView: MaxNativeAdView? = null
    private var imageProvider: ImageProvider? = null
    private var containerView: ViewGroup? = null
    private var admobNativeAd: NativeAd? = null
    private var isPurchased: Boolean = false
    private var maxNativeAd: MaxAd? = null
    private var activity: Activity? = null
    private var admobKey: String? = null
    private var maxKey: String? = null
    fun loadAd(
        activity: Activity,
        isPurchased: Boolean,
        nativeContainerView: ViewGroup,
        listener: OnNativeAdListener,
        imageProvider: ImageProvider
    ) {
        this.containerView = nativeContainerView
        this.activity = activity
        this.onNativeAdListener = listener
        this.isPurchased = isPurchased
        this.imageProvider = imageProvider
        if (isPurchased) {
            this.onNativeAdListener!!.onError("You have pro version!")
            return
        }
        try {
            if (AdsConstants.isInit.not()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    loadAd(activity, isPurchased, nativeContainerView, listener, imageProvider)
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
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    fun onDestroy() {
        try {
            if (this.admobNativeAd != null) {
                this.admobNativeAd!!.destroy()
            }
            if (this.maxNativeAdLoader != null) {
                this.maxNativeAdLoader!!.destroy(maxNativeAd)
            }
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun selectAd() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> {
                    this.onNativeAdListener!!.isEnableAds(false)
                }

                AdsConstants.AD_MOB_MEDIATION -> {
                    this.onNativeAdListener!!.isEnableAds(true)
                    selectAdmobAd()
                }

                AdsConstants.AD_MOB -> {
                    this.onNativeAdListener!!.isEnableAds(true)
                    selectAdmobAd()
                }

                AdsConstants.MAX_MEDIATION -> {
                    this.onNativeAdListener!!.isEnableAds(true)
                    selectMaxAd()
                }

                else -> {
                    this.onNativeAdListener!!.onError("You have to select priority type ADMOB or MAX")
                }
            }
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun selectAdmobAd() {
        try {
            initView()
            if (AdsApplication.isAdmobInLimit()) {
                if (AdsApplication.applyLimitOnAdmob) {
                    onLoadAdError("Admob is in limit all ads banned in current session")
                    return
                }
            }
            if (this.admobKey!!.isEmpty() || this.admobKey!!.isBlank()) {
                onLoadAdError("Empty is found")
                return
            }
            if (AdsUtils.isOnline(this.activity!!).not()) {
                logD("is Offline ")
                this.containerView!!.gone()
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
            nativeBannerBinding!!.parentNativeBannerConstraintLayout.gone()
            val builder = AdLoader.Builder(this.activity!!, this.admobKey!!)
            builder.forNativeAd { nativeAd ->
                logD("ADMOB NATIVE BANNER AD Installed load")
                this.admobNativeAd = nativeAd
                bindAdmobContentAd(nativeBannerBinding!!.adMobBannerNativeAdView)
            }
            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val adOption = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            builder.withNativeAdOptions(adOption)
            val admobAdListener = builder.withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    logD("ADMOB NATIVE EXPRESS AD Loaded")
                    nativeBannerBinding!!.parentNativeBannerConstraintLayout.visible()
                    if (onNativeAdListener != null) {
                        onNativeAdListener!!.onLoaded(AdsConstants.AD_MOB)
                    }
                }

                override fun onAdFailedToLoad(loadError: LoadAdError) {
                    super.onAdFailedToLoad(loadError)
                    AdsConstants.admobRequestNativeFailed++
                    logD("ADMOB NATIVE EXPRESS AD error: ${loadError.message}")
                    onLoadAdError(loadError.message)
                    if (AdsApplication.isAdmobInLimit()) {
                        AdsApplication.applyLimitOnAdmob = true
                    }
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    logD("ADMOB NATIVE EXPRESS AD Opened")
                    if (onNativeAdListener != null) {
                        onNativeAdListener!!.onAdClicked(AdsConstants.AD_MOB)
                    }
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    logD("ADMOB NATIVE EXPRESS AD Clicked")
                    if (onNativeAdListener != null) {
                        onNativeAdListener!!.onAdClicked(AdsConstants.AD_MOB)
                    }
                }
            }).build()
            admobAdListener.loadAd(AdsApplication.getAdRequest())
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun bindAdmobContentAd(nativeView: NativeAdView?) {
        try {
            if (nativeView == null) {
                onLoadAdError("nativeContent is null")
                return
            }
            nativeBannerBinding!!.adCallToAction.gone()
            val bannerModel = NativeBannerModel()
            if (admobNativeAd!!.icon != null) {
                bannerModel.imageIcon = admobNativeAd!!.icon!!.drawable
            }
            if (admobNativeAd!!.headline != null) {
                bannerModel.title = admobNativeAd!!.headline
                nativeView.headlineView = nativeBannerBinding!!.nativeAdTitle
            }
            if (admobNativeAd!!.advertiser != null) {
                bannerModel.sponsorLabel = admobNativeAd!!.advertiser.toString()
                nativeView.advertiserView = nativeBannerBinding!!.nativeAdSponsoredLabel
            }
            if (admobNativeAd!!.body != null) {
                bannerModel.body = admobNativeAd!!.body.toString()
                nativeView.bodyView = nativeBannerBinding!!.nativeBannerAdBodyText
            }
            if (admobNativeAd!!.callToAction != null) {
                bannerModel.adCollection = admobNativeAd!!.callToAction
                nativeView.callToActionView = nativeBannerBinding!!.adCallToAction
            }
            if (admobNativeAd!!.mediaContent != null) {
                bannerModel.mediaView = admobNativeAd!!.mediaContent
                nativeView.mediaView = nativeBannerBinding!!.fbAdMediaView
            }
            bindNativeAd(bannerModel)
            nativeView.setNativeAd(admobNativeAd!!)
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun bindNativeAd(bannerModel: NativeBannerModel) {
        try {
            if (activity!!.isFinishing) {
                return
            }
            val logoUrl: String? = bannerModel.logoUrl
            val logoDrawable: Drawable? = bannerModel.imageIcon
            if (logoDrawable != null) {
                nativeBannerBinding!!.bannerSquareIcon.setImageDrawable(logoDrawable)
            } else if (logoUrl!!.isEmpty() || logoUrl.isBlank()) {
                logD("Logo Url is null")
            } else if (imageProvider == null) {
                nativeBannerBinding!!.bannerSquareIcon.createThumbNail(activity!!, logoUrl)
            } else if (logoUrl.isNotEmpty() || logoUrl.isNotBlank()) {
                imageProvider!!.onProviderImage(nativeBannerBinding!!.bannerSquareIcon, logoUrl)
            }
            var sponsorLabel: String? = bannerModel.sponsorLabel
            sponsorLabel?.let {
                if (it.isEmpty() || it.isBlank()) {
                    sponsorLabel = "Ad"
                    nativeBannerBinding!!.nativeAdSponsoredLabel.setBackgroundResource(R.drawable.bg_ad_broder)
                    nativeBannerBinding!!.nativeAdSponsoredLabel.setTextColor(
                        ColorStateList.valueOf(
                            activity!!.resources.getColor(R.color.green)
                        )
                    )
                }
            } ?: logD("Sponsor Label is null")

            nativeBannerBinding!!.nativeAdSponsoredLabel.text = sponsorLabel
            val title: String = bannerModel.title!!
            if (title.isEmpty() || title.isBlank()) {
                nativeBannerBinding!!.nativeAdTitle.gone()
            } else {
                nativeBannerBinding!!.nativeAdTitle.visible()
                nativeBannerBinding!!.nativeAdTitle.text = title
            }
            val body: String = bannerModel.body!!
            if (body.isEmpty() || body.isBlank()) {
                nativeBannerBinding!!.nativeBannerAdBodyText.gone()
            } else {
                nativeBannerBinding!!.nativeBannerAdBodyText.visible()
                nativeBannerBinding!!.nativeBannerAdBodyText.text = body
            }
            val callToAction: String = bannerModel.adCollection!!
            if (callToAction.isEmpty() || callToAction.isBlank()) {
                nativeBannerBinding!!.adCallToAction.gone()
            } else {
                nativeBannerBinding!!.adCallToAction.visible()
                nativeBannerBinding!!.adCallToAction.text = callToAction
            }
            val mediaContent: MediaContent? = bannerModel.mediaView
            mediaContent?.let {
                nativeBannerBinding!!.fbAdMediaView.visible()
                nativeBannerBinding!!.fbAdMediaView.mediaContent = mediaContent
            } ?: let {
                logD("Media Content")
                nativeBannerBinding!!.fbAdMediaView.gone()
            }
            nativeBannerBinding!!.parentNativeBannerConstraintLayout.visible()
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun selectMaxAd() {
        try {
            if (this.maxKey!!.isEmpty() || this.maxKey!!.isBlank()) {
                onLoadAdError("Empty is found")
                return
            }
            if (AdsUtils.isOnline(this.activity!!).not()) {
                logD("is Offline ")
                this.containerView!!.gone()
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
            maxNativeAdLoader = MaxNativeAdLoader(this.maxKey!!, activity!!)
            maxNativeAdLoader!!.setRevenueListener { ad ->
                val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                adjustAdRevenue.setRevenue(ad.revenue, "USD")
                adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
                adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
                adjustAdRevenue.setAdRevenuePlacement(ad.placement)
                Adjust.trackAdRevenue(adjustAdRevenue)
            }
            maxNativeAdLoader!!.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(p0: MaxNativeAdView?, p1: MaxAd) {
                    super.onNativeAdLoaded(p0, p1)
                    if (maxNativeAd != null) {
                        maxNativeAdLoader!!.destroy(maxNativeAd)
                    }
                    maxNativeAd = p1
                    containerView!!.removeAllViews()
                    containerView!!.addView(p0)
                    onNativeAdListener!!.onLoaded(AdsConstants.MAX_MEDIATION)
                }

                override fun onNativeAdLoadFailed(p0: String, p1: MaxError) {
                    super.onNativeAdLoadFailed(p0, p1)
                    containerView!!.gone()
                    onNativeAdListener!!.onError(p1.message)
                }

                override fun onNativeAdExpired(p0: MaxAd) {
                    super.onNativeAdExpired(p0)
                }

                override fun onNativeAdClicked(p0: MaxAd) {
                    super.onNativeAdClicked(p0)
                    onNativeAdListener!!.onAdClicked(AdsConstants.MAX_MEDIATION)
                }
            })
            maxNativeAdLoader!!.loadAd(maxNativeAdView)
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun bindMaxContentAd() {
        try {
            val nativeView: View = LayoutInflater.from(activity)
                .inflate(R.layout.admob_native_banner_ad_layout, containerView, false)
            val mediaContent = nativeView.findViewById<MediaView>(R.id.fb_ad_media_view)
            mediaContent.gone()
            val binder: MaxNativeAdViewBinder =
                MaxNativeAdViewBinder.Builder(R.layout.admob_native_banner_ad_layout)
                    .setTitleTextViewId(R.id.native_ad_title)
                    .setBodyTextViewId(R.id.native_banner_ad_body_text)
                    .setAdvertiserTextViewId(R.id.native_ad_sponsored_label)
                    .setIconImageViewId(R.id.banner_square_icon)
                    .setMediaContentViewGroupId(R.id.media_view_container)
                    .setOptionsContentViewGroupId(R.id.ad_choices_container)
                    .setCallToActionButtonId(R.id.ad_call_to_action)
                    .build()
            maxNativeAdView = MaxNativeAdView(binder, activity)
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }

    private fun onLoadAdError(error: String) {
        if (this.onNativeAdListener != null) {
            this.onNativeAdListener!!.onError(error)
        }
    }

    private fun initView() {
        try {
            this.containerView!!.removeAllViews()
            nativeBannerBinding = AdmobNativeBannerAdLayoutBinding.inflate(
                LayoutInflater.from(this.activity), this.containerView, false
            )
            this.containerView!!.addView(nativeBannerBinding!!.root)
        } catch (error: Exception) {
            this.onNativeAdListener!!.onError(error = "showNativeAds Error : ${error.localizedMessage}")
        }
    }
}
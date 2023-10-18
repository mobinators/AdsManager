package com.mobinators.ads.manager.ui.commons.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdk
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.listener.BannerAdListener
import com.mobinators.ads.manager.ui.commons.models.AdsModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException


@SuppressLint("StaticFieldLeak")
object BannerAdMediation {
    private var bannerAdListener: BannerAdListener? = null
    private var bannerContainer: ViewGroup? = null
    private var modelClass: AdsModel? = null
    private var appLovingKey: String? = null
    private var activity: Activity? = null
    private var adMobKey: String? = null

    fun showBannerAds(
        activity: Activity,
        isPurchased: Boolean,
        containerView: ViewGroup,
        listener: BannerAdListener
    ) {
        this.bannerContainer = containerView
        this.bannerAdListener = listener
        this.activity = activity
        if (isPurchased) {
            listener.onError("You have pro version")
            return
        }
        try {
            modelClass = AdsApplication.getAdsModel()
            selectAd()
        } catch (error: Exception) {
            this.bannerAdListener!!.onError(error = "showBannerAds Error : ${error.localizedMessage}")
        }
    }


    private fun selectAd() {
        try {
            when (modelClass?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> {
                    this.bannerAdListener!!.isEnableAds(false)
                }

                AdsConstants.AD_MOB_MEDIATION -> {
                    this.bannerAdListener!!.isEnableAds(true)
                    adMobBannerAds()
                }

                AdsConstants.AD_MOB -> {
                    this.bannerAdListener!!.isEnableAds(true)
                    adMobBannerAds()
                }

                AdsConstants.MAX_MEDIATION -> {
                    this.bannerAdListener!!.isEnableAds(true)
                    maxBannerAds()
                }
            }
        } catch (error: Exception) {
            this.bannerAdListener!!.onError(error = "showBannerAds Error : ${error.localizedMessage}")
        }
    }

    private fun adMobBannerAds() {
        try {
            adMobKey = if (AdsConstants.testMode) {
                AdsConstants.TEST_ADMOB_BANNER_ID
            } else {
                if (modelClass!!.admobMediation) {
                    modelClass!!.admobMediationBannerId

                } else {
                    modelClass!!.admobBannerID
                }
            }
            if (adMobKey!!.isEmpty() || adMobKey!!.isBlank()) {
                this.bannerAdListener!!.onError("AdMob Banner Ads Id is null")
                return
            }
            if (AdsUtils.isOnline(this.activity!!).not()) {
                logD("is Offline ")
                this.bannerContainer!!.gone()
                this.bannerAdListener!!.isOffline(true)
                return
            }
            logD("debug mode : ${AdsConstants.testMode}")
            if (this.adMobKey == AdsConstants.TEST_ADMOB_BANNER_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    logD("NULL OR TEST IDS FOUND")
                    this.bannerAdListener!!.onError("NULL OR TEST IDS FOUND")
                    return
                }
            }
            logD("AdMob Banner Ads Id : $adMobKey")
            val bannerView = AdView(this.activity!!)
            bannerView.setAdSize(AdSize.BANNER)
            bannerView.adUnitId = adMobKey!!
            bannerView.loadAd(AdsApplication.getAdRequest())
            bannerView.adListener = object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    logD("onAdClicked")
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    logD("onAdClosed")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    bannerAdListener!!.onError(p0.message)
                    if (AdsApplication.isAdmobInLimit()) {
                        AdsApplication.applyLimitOnAdmob = true
                    }
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    logD("onAdImpression")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (bannerContainer!!.parent != null) {
                        bannerContainer!!.removeAllViews()
                    }
                    bannerContainer!!.addView(bannerView)
                    if (bannerAdListener != null) {
                        bannerAdListener!!.onLoaded(AdsConstants.AD_MOB)
                    }
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    if (bannerAdListener != null) {
                        bannerAdListener!!.onAdClicked(AdsConstants.AD_MOB)
                    }
                }

                override fun onAdSwipeGestureClicked() {
                    super.onAdSwipeGestureClicked()
                    logD("onAdSwipeGestureClicked")
                }
            }
        } catch (error: Exception) {
            this.bannerAdListener!!.onError(error = "showBannerAds Error : ${error.localizedMessage}")
        }
    }

    private fun maxBannerAds() {
        try {
            this.appLovingKey = if (AdsConstants.testMode) {
                AdsConstants.TEST_MAX_BANNER_ADS_ID
            } else {
                modelClass!!.maxBannerID
            }
            if (this.appLovingKey!!.isEmpty() || this.appLovingKey!!.isBlank()) {
                this.bannerAdListener!!.onError("Max Mediation Banner Ads Id is null")
                return
            }
            if (AdsUtils.isOnline(this.activity!!).not()) {
                this.bannerContainer!!.gone()
                this.bannerAdListener!!.isOffline(true)
                return
            }
            if (this.appLovingKey == AdsConstants.TEST_MAX_BANNER_ADS_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    logD("NULL OR TEST IDS FOUND")
                    this.bannerAdListener!!.onError("NULL OR TEST IDS FOUND")
                    return
                }
            }
            logD("Max Banner Ads Id : ${this.appLovingKey}")
            val maxBannerView = MaxAdView(this.appLovingKey, MaxAdFormat.BANNER, this.activity)
            maxBannerView.setListener(object : MaxAdViewAdListener {
                override fun onAdLoaded(p0: MaxAd?) {
                    logD("onAdLoaded")
                    if (bannerContainer!!.parent != null) {
                        bannerContainer!!.removeAllViews()
                    }
                    bannerContainer!!.addView(maxBannerView)
                    if (bannerAdListener != null) {
                        bannerAdListener!!.onLoaded(AdsConstants.MAX_MEDIATION)
                    }
                }

                override fun onAdDisplayed(p0: MaxAd?) {
                    logD("onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd?) {
                    logD("onAdHidden")
                }

                override fun onAdClicked(p0: MaxAd?) {
                    logD("onAdClicked")
                }

                override fun onAdLoadFailed(p0: String?, p1: MaxError?) {
                    bannerAdListener!!.onError(p1!!.message)
                }

                override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {
                    bannerAdListener!!.onError(p1!!.message)
                }

                override fun onAdExpanded(p0: MaxAd?) {
                    logD("onAdExpanded")
                }

                override fun onAdCollapsed(p0: MaxAd?) {
                    logD("onAdCollapsed")
                }
            })
            maxBannerView.setRevenueListener {
                val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                adjustAdRevenue.setRevenue(it?.revenue, "USD")
                adjustAdRevenue.setAdRevenueNetwork(it?.networkName)
                adjustAdRevenue.setAdRevenueUnit(it?.adUnitId)
                adjustAdRevenue.setAdRevenuePlacement(it?.placement)
                Adjust.trackAdRevenue(adjustAdRevenue)
            }
            maxBannerView.loadAd()
            maxBannerView.startAutoRefresh()
        } catch (error: Exception) {
            this.bannerAdListener!!.onError(error = "showBannerAds Error : ${error.localizedMessage}")
        }
    }
}

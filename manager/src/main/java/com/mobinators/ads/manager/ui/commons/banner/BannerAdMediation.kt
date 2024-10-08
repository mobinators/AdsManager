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
import com.applovin.sdk.AppLovinSdkUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.models.AdsModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.visible


@SuppressLint("StaticFieldLeak")
object BannerAdMediation {
    private var bannerAdListener: BannerAdListener? = null
    private var bannerContainer: ViewGroup? = null
    private var maxBannerView: MaxAdView? = null
    private var modelClass: AdsModel? = null
    private var appLovingKey: String? = null
    private var bannerView: AdView? = null
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
        this.bannerContainer?.gone()
        if (isPurchased) {
            listener.onAdsState(adsShowState = AdsShowState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(activity).not()) {
            listener.onAdsState(adsShowState = AdsShowState.NETWORK_OFF)
            return
        }
        modelClass = AdsApplication.getAdsModel()
        selectAd()
    }


    private fun selectAd() {
        when (modelClass?.strategy?.toInt() ?: 0) {
            AdsConstants.ADS_OFF -> this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_OFF)
            AdsConstants.AD_MOB_MEDIATION -> adMobBannerAds()
            AdsConstants.AD_MOB -> adMobBannerAds()
            AdsConstants.MAX_MEDIATION -> maxBannerAds()
            else -> this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_STRATEGY_WRONG)
        }
    }

    private fun adMobBannerAds() {
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
            this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_ID_NULL)
            return
        }
        if (AdsUtils.isOnline(this.activity!!).not()) {
            this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.NETWORK_OFF)
            return
        }
        if (AdsConstants.testMode.not()) {
            if (this.adMobKey == AdsConstants.TEST_ADMOB_BANNER_ID) {
                this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.TEST_ADS_ID)
                return
            }
        }
        logD("Admob Banner Ads Unit ID: ${this.adMobKey}")
        bannerView = AdView(this.activity!!)
        bannerView?.setAdSize(AdSize.BANNER)
        bannerView?.adUnitId = adMobKey!!
        bannerView?.loadAd(AdsApplication.getAdRequest())
        bannerView?.adListener = object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_CLICKED)
            }

            override fun onAdClosed() {
                super.onAdClosed()
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_CLOSED)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                logD("Admob onAdFailedToLoad : $p0")
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_LOAD_FAILED)
                if (AdsApplication.isAdmobInLimit()) {
                    AdsApplication.applyLimitOnAdmob = true
                }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_IMPRESS)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                this@BannerAdMediation.bannerContainer!!.visible()
                if (bannerContainer!!.parent != null) {
                    bannerContainer!!.removeAllViews()
                }
                bannerContainer!!.addView(bannerView)
                bannerAdListener?.onAdsLoaded()
            }

            override fun onAdOpened() {
                super.onAdOpened()
                bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_OPEN)
            }

            override fun onAdSwipeGestureClicked() {
                super.onAdSwipeGestureClicked()
                logD("onAdSwipeGestureClicked")
            }
        }

    }

    private fun maxBannerAds() {
        this.appLovingKey = modelClass!!.maxBannerID
        if (this.appLovingKey!!.isEmpty() || this.appLovingKey!!.isBlank()) {
            this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_ID_NULL)
            return
        }
        if (AdsUtils.isOnline(this.activity!!).not()) {
            this.bannerContainer!!.gone()
            this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.NETWORK_OFF)
            return
        }
        if (this.appLovingKey == AdsConstants.TEST_MAX_BANNER_ADS_ID) {
            this.bannerAdListener?.onAdsState(adsShowState = AdsShowState.TEST_ADS_ID)
            return
        }
        logD("Max Banner Ads Unit ID: ${this.appLovingKey}")
        maxBannerView = MaxAdView(this.appLovingKey, MaxAdFormat.BANNER, this.activity)
        maxBannerView?.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                this@BannerAdMediation.bannerContainer!!.visible()
                if (bannerContainer!!.parent != null) {
                    bannerContainer!!.removeAllViews()
                }
                bannerContainer!!.addView(maxBannerView)
                if (bannerAdListener != null) {
                    bannerAdListener!!.onAdsLoaded()
                }
            }

            override fun onAdDisplayed(p0: MaxAd) {
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_DISPLAY)
            }

            override fun onAdHidden(p0: MaxAd) {
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_IMPRESS)
            }

            override fun onAdClicked(p0: MaxAd) {
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_CLICKED)
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                logD("BannerAds onAdLoadFailed : Error code : ${p1.code}")
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_LOAD_FAILED)
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                this@BannerAdMediation.bannerAdListener?.onAdsState(adsShowState = AdsShowState.ADS_DISPLAY_FAILED)
            }

            override fun onAdExpanded(p0: MaxAd) {
                logD("onAdExpanded")
            }

            override fun onAdCollapsed(p0: MaxAd) {
                logD("onAdCollapsed")
            }
        })
        maxBannerView?.setRevenueListener {
            val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
            adjustAdRevenue.setRevenue(it.revenue, "USD")
            adjustAdRevenue.setAdRevenueNetwork(it.networkName)
            adjustAdRevenue.setAdRevenueUnit(it.adUnitId)
            adjustAdRevenue.setAdRevenuePlacement(it.placement)
            Adjust.trackAdRevenue(adjustAdRevenue)
        }
        val isTablet = AppLovinSdkUtils.isTablet(this.activity!!)
        val heightPx = AppLovinSdkUtils.dpToPx(this.activity!!, if (isTablet) 90 else 50)

        maxBannerView?.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
        maxBannerView?.loadAd()
        maxBannerView?.startAutoRefresh()
    }

    fun onDestroy() {
        bannerView?.destroy()
        maxBannerView?.destroy()
    }

    interface BannerAdListener {
        fun onAdsLoaded()
        fun onAdsState(adsShowState: AdsShowState)
    }
}

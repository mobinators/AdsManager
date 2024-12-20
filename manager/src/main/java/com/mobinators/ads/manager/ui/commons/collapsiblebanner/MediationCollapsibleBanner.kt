package com.mobinators.ads.manager.ui.commons.collapsiblebanner

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.extensions.sdk30AndUp
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException


@SuppressLint("StaticFieldLeak")
object MediationCollapsibleBanner {
    private var mainActivity: Activity? = null
    private var admobCollapseBannerKey: String? = null
    private var collapseBannerContainer: ViewGroup? = null
    private var bannerListener: BannerAdListener? = null
    private var collapseBannerState = CollapseBannerState.BOTTOM
    fun showCollapsibleBannerAds(
        activity: Activity,
        isPurchased: Boolean,
        containerView: ViewGroup,
        bannerState: CollapseBannerState,
        listener: BannerAdListener
    ) {
        this.mainActivity = activity
        this.collapseBannerContainer = containerView
        collapseBannerState = bannerState
        this.bannerListener = listener
        if (isPurchased) {
            this.bannerListener?.onAdsShowState(adsShowState = AdsShowState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.mainActivity!!).not()) {
            this.bannerListener?.onAdsShowState(adsShowState = AdsShowState.NETWORK_OFF)
            this.collapseBannerContainer!!.gone()
            return
        }
        collapseBannerSelected()
    }

    private fun collapseBannerSelected() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_OFF)
                AdsConstants.AD_MOB_MEDIATION -> {}
                AdsConstants.AD_MOB -> collapseBannerAdsInt()
                AdsConstants.MAX_MEDIATION -> {}
                else -> this.bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_STRATEGY_WRONG)
            }

        } catch (error: Exception) {
            logException("Selected Collapse Banner Ads Error :  ${error.localizedMessage}")
        }
    }

    private fun collapseBannerAdsInt() {
        try {
            this.admobCollapseBannerKey = if (AdsConstants.testMode) {
                AdsConstants.TEST_ADMOB_COLLAPSE_BANNER_ID
            } else {
                if (AdsApplication.getAdsModel()!!.admobMediation) {
                    AdsApplication.getAdsModel()!!.collapseBannerID
                } else {
                    AdsApplication.getAdsModel()!!.collapseBannerID
                }
            }
            if (AdsApplication.getAdsModel()!!.admobMediation) {
                logD("AdMob Mediation is enable")
                return
            }
            if (this.admobCollapseBannerKey.isNullOrEmpty() || this.admobCollapseBannerKey.isNullOrBlank()) {
                this.bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_ID_NULL)
                return
            }
            if (this.admobCollapseBannerKey == AdsConstants.TEST_ADMOB_COLLAPSE_BANNER_ID) {
                if (AdsConstants.testMode.not()) {
                    this.bannerListener?.onAdsShowState(adsShowState = AdsShowState.TEST_ADS_ID)
                    return
                }
            }
            logD("Admob Collapse Banner Ads Unit ID: ${this.admobCollapseBannerKey}")
            val collapseBannerView = AdView(this.mainActivity!!)
            collapseBannerView.adUnitId = this.admobCollapseBannerKey!!
            collapseBannerView.setAdSize(getCollapseBannerSize())
            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                    putString(
                        "collapsible", when (collapseBannerState) {
                            CollapseBannerState.TOP -> "top"
                            CollapseBannerState.BOTTOM -> "bottom"
                        }
                    )
                })
                .build()
            collapseBannerView.loadAd(adRequest)
            collapseBannerView.adListener = object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    this@MediationCollapsibleBanner.bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_CLICKED)
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    this@MediationCollapsibleBanner.bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_CLOSED)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    logD("Admob onAdFailedToLoad : $p0")
                    this@MediationCollapsibleBanner.bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_LOAD_FAILED)
                    if (AdsApplication.isAdmobInLimit()) {
                        AdsApplication.applyLimitOnAdmob = true
                    }
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    this@MediationCollapsibleBanner.bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_IMPRESS)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (collapseBannerContainer!!.parent != null) {
                        collapseBannerContainer!!.removeAllViews()
                    }
                    collapseBannerContainer!!.addView(collapseBannerView)
                    bannerListener?.onAdsLoaded()
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    bannerListener?.onAdsShowState(adsShowState = AdsShowState.ADS_OPEN)
                }

                override fun onAdSwipeGestureClicked() {
                    super.onAdSwipeGestureClicked()
                    logD("onAdSwipeGestureClicked")
                }
            }
//            this.collapseBannerContainer!!.addView(collapseBannerView)
        } catch (error: Exception) {
            logException("Collapse Banner Init Error : ${error.localizedMessage}")
        }
    }

    private fun getCollapseBannerSize(): AdSize {
        val windowMetrics = sdk30AndUp {
            this.mainActivity!!.windowManager.currentWindowMetrics
        }
        val bounds = sdk30AndUp {
            windowMetrics?.bounds
        }
        var adWidthPixels = this.collapseBannerContainer!!.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = bounds!!.width().toFloat()
        }
        val density = this.mainActivity!!.resources.displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            this.mainActivity!!,
            adWidth
        )
    }

    interface BannerAdListener {
        fun onAdsLoaded()
        fun onAdsShowState(adsShowState: AdsShowState)
    }
}
package com.mobinators.ads.manager.ui.commons.collapsiblebanner

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.extensions.sdk30AndUp
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
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
            this.bannerListener?.onAdsError(adsErrorState = AdsErrorState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.mainActivity!!).not()) {
            this.bannerListener?.onAdsError(adsErrorState = AdsErrorState.NETWORK_OFF)
            this.collapseBannerContainer!!.gone()
            return
        }
        collapseBannerSelected()
    }

    private fun collapseBannerSelected() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.bannerListener!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> {}
                AdsConstants.AD_MOB -> collapseBannerAdsInt()
                AdsConstants.MAX_MEDIATION -> {}
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
                this.bannerListener?.onAdsError(adsErrorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (this.admobCollapseBannerKey == AdsConstants.TEST_ADMOB_COLLAPSE_BANNER_ID) {
                if (AdsConstants.testMode.not()) {
                    this.bannerListener?.onAdsError(adsErrorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
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
            this.collapseBannerContainer!!.addView(collapseBannerView)
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
        fun onAdsOff()
        fun onAdsLoaded()
        fun onAdsError(adsErrorState: AdsErrorState)
    }
}
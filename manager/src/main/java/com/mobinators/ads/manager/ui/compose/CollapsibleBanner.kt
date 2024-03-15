package com.mobinators.ads.manager.ui.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.extensions.sdk30AndUp
import com.mobinators.ads.manager.ui.commons.collapsiblebanner.CollapseBannerState
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils


private var admobCollapseBannerKey: String? = null
private var bannerListener: BannerAdsListener? = null
private var collapseBannerState = CollapseBannerState.BOTTOM

@SuppressLint("StaticFieldLeak")
private var composeActivity: Activity? = null

@Composable
fun ShowCollapsibleBanner(
    activity: Activity,
    isPurchased: Boolean,
    bannerState: CollapseBannerState,
    listener: BannerAdsListener
) {
    composeActivity = activity
    collapseBannerState = bannerState
    bannerListener = listener

    if (isPurchased) {
        bannerListener?.onBannerAdsState(adsState = AdsState.APP_PURCHASED)
        return
    }
    if (AdsUtils.isOnline(composeActivity!!).not()) {
        bannerListener?.onBannerAdsState(adsState = AdsState.NETWORK_OFF)
        return
    }
    SelectedCollapsibleBannerAds()
}

@Composable
private fun SelectedCollapsibleBannerAds() {
    when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
        AdsConstants.ADS_OFF -> bannerListener?.onBannerAdsState(adsState = AdsState.ADS_OFF)
        AdsConstants.AD_MOB_MEDIATION -> {}
        AdsConstants.AD_MOB -> CollapsibleBannerAds()
        AdsConstants.MAX_MEDIATION -> {}
    }
}


@Composable
private fun CollapsibleBannerAds() {
    admobCollapseBannerKey = if (AdsConstants.testMode) {
        AdsConstants.TEST_ADMOB_COLLAPSE_BANNER_ID
    } else {
        if (AdsApplication.getAdsModel()!!.admobMediation) {
            AdsApplication.getAdsModel()!!.collapseBannerID
        } else {
            AdsApplication.getAdsModel()!!.collapseBannerID
        }
    }
    if (AdsApplication.getAdsModel()!!.admobMediation) {
        Log.d("TAG", "AdMob Mediation is enable")
        return
    }
    if (admobCollapseBannerKey.isNullOrEmpty() || admobCollapseBannerKey.isNullOrBlank()) {
        bannerListener?.onBannerAdsState(adsState = AdsState.ADS_ID_NULL)
        return
    }
    if (admobCollapseBannerKey == AdsConstants.TEST_ADMOB_COLLAPSE_BANNER_ID) {
        if (AdsConstants.testMode.not()) {
            bannerListener?.onBannerAdsState(adsState = AdsState.TEST_ADS_ID)
            return
        }
    }
    AndroidView(factory = {
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
        AdView(composeActivity!!).apply {
            this.adUnitId = admobCollapseBannerKey!!
            this.setAdSize(getCollapseBannerSize())
            this.loadAd(adRequest)
        }
    })

}

private fun getCollapseBannerSize(): AdSize {
    val windowMetrics = sdk30AndUp {
        composeActivity!!.windowManager.currentWindowMetrics
    }
    val bounds = sdk30AndUp {
        windowMetrics?.bounds
    }
    var adWidthPixels = composeActivity!!.resources.displayMetrics.widthPixels.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = bounds!!.width().toFloat()
    }
    val density = composeActivity!!.resources.displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        composeActivity!!,
        adWidth
    )
}

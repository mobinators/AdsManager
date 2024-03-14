package com.mobinators.ads.manager.ui.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.models.AdsModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils


private var bannerAdsListener: BannerAdsListener? = null
private var modelClass: AdsModel? = null
private var appLovingKey: String? = null
private var adMobKey: String? = null

@Composable
fun ShowBannerAds(modifier: Modifier, isPurchased: Boolean, listener: BannerAdsListener) {
    bannerAdsListener = listener
    isPurchased.then {
        bannerAdsListener?.onBannerAdsState(adsState = AdsState.APP_PURCHASED)
    } ?: run {
        modelClass = AdsApplication.getAdsModel()
        SelectedAds(modifier = modifier)
    }
}

@Composable
private fun SelectedAds(modifier: Modifier) {
    when (modelClass?.strategy?.toInt() ?: 0) {
        AdsConstants.ADS_OFF -> bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_OFF)
        AdsConstants.AD_MOB_MEDIATION -> AdmobBannerAds(modifier = modifier)
        AdsConstants.AD_MOB -> AdmobBannerAds(modifier = modifier)
        AdsConstants.MAX_MEDIATION -> MaxBannerAds(modifier = modifier)
        else -> bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_STRATEGY_WRONG)
    }
}

@Composable
private fun AdmobBannerAds(modifier: Modifier) {
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
        bannerAdsListener!!.onBannerAdsState(adsState = AdsState.ADS_ID_NULL)
        return
    }

    if (AdsUtils.isOnline(LocalContext.current).not()) {
        bannerAdsListener!!.onBannerAdsState(adsState = AdsState.NETWORK_OFF)
        return
    }
    if (AdsConstants.testMode.not()) {
        if (adMobKey == AdsConstants.TEST_ADMOB_BANNER_ID) {
            bannerAdsListener!!.onBannerAdsState(adsState = AdsState.TEST_ADS_ID)
            return
        }
    }
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = adMobKey!!
                loadAd(AdsApplication.getAdRequest())
                adListener = object : AdListener() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_CLICKED)
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_CLOSED)
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_LOAD_FAILED)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_IMPRESS)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_LOADED)
                    }

                    override fun onAdOpened() {
                        super.onAdOpened()
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_OPENED)
                    }

                    override fun onAdSwipeGestureClicked() {
                        super.onAdSwipeGestureClicked()
                    }
                }
            }
        }
    )
}


@Composable
private fun MaxBannerAds(modifier: Modifier) {
    appLovingKey = if (AdsConstants.testMode) {
        AdsConstants.TEST_MAX_BANNER_ADS_ID
    } else {
        modelClass!!.maxBannerID
    }
    if (appLovingKey!!.isEmpty() || appLovingKey!!.isBlank()) {
        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_ID_NULL)
        return
    }
    if (AdsUtils.isOnline(LocalContext.current).not()) {
        bannerAdsListener?.onBannerAdsState(adsState = AdsState.NETWORK_OFF)
        return
    }
    if (AdsConstants.testMode.not()) {
        if (appLovingKey == AdsConstants.TEST_MAX_BANNER_ADS_ID) {
            bannerAdsListener?.onBannerAdsState(adsState = AdsState.TEST_ADS_ID)
            return
        }
    }
    AndroidView(modifier = modifier.fillMaxWidth(),
        factory = {
            MaxAdView(appLovingKey, MaxAdFormat.BANNER, it).apply {
                setListener(object : MaxAdViewAdListener {
                    override fun onAdLoaded(p0: MaxAd) {
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_LOADED)
                    }

                    override fun onAdDisplayed(p0: MaxAd) {
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_DISPLAY)
                    }

                    override fun onAdHidden(p0: MaxAd) {
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_IMPRESS)
                    }

                    override fun onAdClicked(p0: MaxAd) {
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_CLICKED)
                    }

                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_LOAD_FAILED)
                    }

                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                        bannerAdsListener?.onBannerAdsState(adsState = AdsState.ADS_DISPLAY_FAILED)
                    }

                    override fun onAdExpanded(p0: MaxAd) {

                    }

                    override fun onAdCollapsed(p0: MaxAd) {

                    }
                })
            }
        })
}

interface BannerAdsListener {
    fun onBannerAdsState(adsState: AdsState)
}


enum class AdsState {
    ADS_OFF,
    NETWORK_OFF,
    APP_PURCHASED,
    ADS_STRATEGY_WRONG,
    ADS_ID_NULL,
    TEST_ADS_ID,
    ADS_LOAD_FAILED,
    ADS_DISMISS,
    ADS_DISPLAY_FAILED,
    ADS_DISPLAY,
    ADS_IMPRESS,
    ADS_LOADED,
    ADS_CLICKED,
    ADS_CLOSED,
    ADS_OPENED
}

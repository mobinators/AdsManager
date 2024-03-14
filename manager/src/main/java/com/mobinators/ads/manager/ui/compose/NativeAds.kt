package com.mobinators.ads.manager.ui.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
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
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logException
import pak.developer.app.managers.extensions.visible


private var loadNativeListener: NativeAdsLoaderCallback? = null
private var showNativeListener: NativeAdsShowListener? = null
private var nativeAdsKey: String? = null
private var admobNativeAd: NativeAd? = null
private var isCustomAdsView: Boolean = false
private var maxNativeAds: MaxNativeAdLoader? = null

@SuppressLint("StaticFieldLeak")
private var composeActivity: Activity? = null


@Composable
fun LoadNativeAds(activity: Activity, isPurchased: Boolean, listener: NativeAdsLoaderCallback) {
    loadNativeListener = listener
    composeActivity = activity
    isPurchased.then {
        loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.APP_PURCHASED)
    } ?: run {
        if (AdsUtils.isOnline(LocalContext.current).not()) {
            loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.NETWORK_OFF)
            return
        }
        InitSelectedNativeLoaded()
    }
}


@Composable
fun ShowNativeAds(
    isPurchased: Boolean,
    listener: NativeAdsShowListener,
    isCustomView: Boolean = false
) {
    isCustomAdsView = isCustomView
    showNativeListener = listener
    isPurchased.then {
        showNativeListener?.onNativeAdsShowState(showState = ShowNativeAdsState.APP_PURCHASED)
    } ?: run {
        ShowSelectedNativeAds()
    }
}

@Composable
private fun InitSelectedNativeLoaded() {
    when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
        AdsConstants.ADS_OFF -> loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.ADS_OFF)
        AdsConstants.AD_MOB_MEDIATION -> AdmobNativeAdsLoad()
        AdsConstants.AD_MOB -> AdmobNativeAdsLoad()
        AdsConstants.MAX_MEDIATION -> {}
        else -> loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.ADS_STRATEGY_WRONG)
    }
}

@Composable
private fun ShowSelectedNativeAds() {
    when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
        AdsConstants.ADS_OFF -> showNativeListener?.onNativeAdsShowState(showState = ShowNativeAdsState.ADS_OFF)
        AdsConstants.AD_MOB_MEDIATION -> BindNativeAdmobView()
        AdsConstants.AD_MOB -> BindNativeAdmobView()
        AdsConstants.MAX_MEDIATION -> {}
        else -> showNativeListener?.onNativeAdsShowState(showState = ShowNativeAdsState.ADS_STRATEGY_WRONG)
    }
}

@Composable
private fun AdmobNativeAdsLoad() {
    nativeAdsKey = if (AdsConstants.testMode) {
        AdsConstants.TEST_ADMOB_NATIVE_ID
    } else {
        if (AdsApplication.getAdsModel()!!.admobMediation) {
            AdsApplication.getAdsModel()!!.admobMediationNativeId
        } else {
            AdsApplication.getAdsModel()!!.admobNativeID
        }
    }
    if (nativeAdsKey.isNullOrEmpty() || nativeAdsKey.isNullOrBlank()) {
        loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.ADS_ID_NULL)
        return
    }
    if (AdsConstants.testMode.not()) {
        if (nativeAdsKey == AdsConstants.TEST_ADMOB_NATIVE_ID) {
            loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.TEST_ADS_ID)
            return
        }
    }
    val builder = AdLoader.Builder(LocalContext.current, nativeAdsKey!!)
    builder.forNativeAd {
        try {
            val activityDestroy = composeActivity!!.isDestroyed
            val activity = composeActivity!!
            if (activityDestroy || activity.isFinishing || activity.isChangingConfigurations) {
                it.destroy()
                return@forNativeAd
            }
        } catch (error: Exception) {
            Log.d("Tag", "AdmobNativeAdsLoad:  Activity Ref is null")
        }
        admobNativeAd?.destroy()
        admobNativeAd = it
    }
    val videoOption = VideoOptions.Builder().setStartMuted(true).build()
    val nativeAdOption = NativeAdOptions.Builder().setVideoOptions(videoOption).build()
    builder.withNativeAdOptions(nativeAdOption)
    val adLoader = builder.withAdListener(object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.ADS_LOADED)
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            loadNativeListener?.onNativeAdsState(loadState = LoadNativeState.ADS_LOAD_FAILED)
        }

        override fun onAdClicked() {
            super.onAdClicked()
            showNativeListener?.onNativeAdsShowState(showState = ShowNativeAdsState.ADS_CLICKED)
        }

        override fun onAdClosed() {
            super.onAdClosed()
            showNativeListener?.onNativeAdsShowState(showState = ShowNativeAdsState.ADS_CLOSED)
        }

        override fun onAdImpression() {
            super.onAdImpression()
            showNativeListener?.onNativeAdsShowState(showState = ShowNativeAdsState.ADS_IMPRESS)
        }

        override fun onAdOpened() {
            super.onAdOpened()
            showNativeListener?.onNativeAdsShowState(showState = ShowNativeAdsState.ADS_OPENED)
        }

        override fun onAdSwipeGestureClicked() {
            super.onAdSwipeGestureClicked()
        }
    }).build()
    adLoader.loadAd(AdsApplication.getAdRequest())
}


@Composable
private fun BindNativeAdmobView() {
    if (admobNativeAd != null) {
        isCustomAdsView.then {
            NativeCustomAdViewLayout()
        } ?: run {
            NativeAdViewLayout()
        }
        InitSelectedNativeLoaded()
    } else {
        InitSelectedNativeLoaded()
    }
}

@Composable
private fun NativeAdViewLayout() {
    AndroidViewBinding(
        factory = AdmobNativeAdLayoutBinding::inflate,
        modifier = Modifier
            .navigationBarsPadding()
            .wrapContentHeight(unbounded = true)
    ) {
        val nativeAdView = this.root
        nativeAdView.mediaView = this.adMedia
        nativeAdView.headlineView = this.adHeadline
        nativeAdView.bodyView = this.adBody
        nativeAdView.callToActionView = this.adAction
        nativeAdView.iconView = this.squareIcon
        nativeAdView.priceView = this.adPrice
        nativeAdView.starRatingView = this.adStars
        nativeAdView.storeView = this.adStore
        nativeAdView.advertiserView = this.advertiser
        this.adHeadline.text = admobNativeAd!!.headline
        admobNativeAd!!.mediaContent?.let {
            this.adMedia.mediaContent = it
        }
        if (admobNativeAd!!.body == null) {
            this.adBody.gone()
        } else {
            this.adBody.visible()
            this.adBody.text = admobNativeAd!!.body
        }

        if (admobNativeAd!!.callToAction == null) {
            this.adAction.gone()
        } else {
            this.adAction.visible()
            this.adAction.text = admobNativeAd!!.callToAction
        }

        if (admobNativeAd!!.icon == null) {
            this.squareIcon.gone()
        } else {
            this.squareIcon.setImageDrawable(admobNativeAd!!.icon?.drawable)
            this.squareIcon.visible()
        }

        if (admobNativeAd!!.price == null) {
            this.adPrice.gone()
        } else {
            this.adPrice.visible()
            this.adPrice.text = admobNativeAd!!.price
        }
        if (admobNativeAd!!.store == null) {
            this.adStore.gone()
        } else {
            this.adStore.visible()
            this.adStore.text = admobNativeAd!!.store
        }
        if (admobNativeAd!!.starRating == null) {
            this.adStars.gone()
        } else {
            this.adStars.rating =
                admobNativeAd!!.starRating!!.toFloat()
            this.adStars.visible()
        }
        if (admobNativeAd!!.advertiser == null) {
            this.advertiser.gone()
        } else {
            this.advertiser.text = admobNativeAd!!.advertiser
            this.advertiser.visible()
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
}

@Composable
private fun NativeCustomAdViewLayout() {
    AndroidViewBinding(
        factory = CustomNativeBinding::inflate,
        modifier = Modifier
            .navigationBarsPadding()
            .wrapContentHeight(unbounded = true)
    ) {
        val nativeAdView = this.root
        nativeAdView.mediaView = this.adMedia
        nativeAdView.headlineView = this.adHeadline
        nativeAdView.bodyView = this.adBody
        nativeAdView.callToActionView = this.adCallToAction
        nativeAdView.iconView = this.adAppIcon
        nativeAdView.priceView = this.adPrice
        nativeAdView.starRatingView = this.adStars
        nativeAdView.storeView = this.adStore
        nativeAdView.advertiserView = this.adAdvertiser
        this.adHeadline.text = admobNativeAd!!.headline
        admobNativeAd!!.mediaContent?.let { this.adMedia.mediaContent = it }
        if (admobNativeAd!!.callToAction == null) {
            this.adCallToAction.gone()
        } else {
            this.adCallToAction.visible()
            this.adCallToAction.text = admobNativeAd!!.callToAction
        }

        if (admobNativeAd!!.icon == null) {
            this.adAppIcon.gone()
        } else {
            this.adAppIcon.setImageDrawable(admobNativeAd!!.icon?.drawable)
            this.adAppIcon.visible()
        }

        if (admobNativeAd!!.price == null) {
            this.adPrice.gone()
        } else {
            this.adPrice.visible()
            this.adPrice.text = admobNativeAd!!.price
        }
        if (admobNativeAd!!.store == null) {
            this.adStore.gone()
        } else {
            this.adStore.visible()
            this.adStore.text = admobNativeAd!!.store
        }
        if (admobNativeAd!!.starRating == null) {
            this.adStars.gone()
        } else {
            this.adStars.rating = admobNativeAd!!.starRating!!.toFloat()
            this.adStars.visible()
        }
        if (admobNativeAd!!.advertiser == null) {
            this.adAdvertiser.gone()
        } else {
            this.adAdvertiser.text = admobNativeAd!!.advertiser
            this.adAdvertiser.visible()
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
}


@Composable
fun DestroyNativeAds() {
    admobNativeAd?.destroy()
    maxNativeAds?.destroy()
}

interface NativeAdsLoaderCallback {
    fun onNativeAdsState(loadState: LoadNativeState)
}

interface NativeAdsShowListener {
    fun onNativeAdsShowState(showState: ShowNativeAdsState)
}

enum class LoadNativeState {
    APP_PURCHASED,
    NETWORK_OFF,
    ADS_OFF,
    ADS_STRATEGY_WRONG,
    ADS_ID_NULL,
    TEST_ADS_ID,
    ADS_LOADED,
    ADS_LOAD_FAILED
}

enum class ShowNativeAdsState {
    APP_PURCHASED,
    ADS_OFF,
    ADS_STRATEGY_WRONG,
    ADS_CLICKED,
    ADS_CLOSED,
    ADS_IMPRESS,
    ADS_OPENED,
}
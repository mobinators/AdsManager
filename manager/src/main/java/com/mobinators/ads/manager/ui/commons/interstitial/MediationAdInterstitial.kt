package com.mobinators.ads.manager.ui.commons.interstitial

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.listener.InterstitialAdsListener
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import com.mobinators.ads.manager.ui.commons.views.dialog.ProgressDialogUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object MediationAdInterstitial {
    private var interstitialListener: InterstitialAdsListener? = null
//    private var progressDialogUtils: ProgressDialogUtils? = null
    private var activityRef: WeakReference<Activity>? = null
    private var maxInterstitialAd: MaxInterstitialAd? = null
    private var admobInterstitialAD: InterstitialAd? = null
    private var activity: Activity? = null
    private var adMobKey: String? = null
    private var maxKey: String? = null
    private var showAd: Boolean = true
    private var timer = 3000L
    fun showInterstitialAd(
        activity: Activity,
        isPurchased: Boolean,
        listener: InterstitialAdsListener
    ) {
        this.activity = activity
        this.interstitialListener = listener
        if (AdsConstants.isAdPreloadEnable.not()) {
            PreLoadMediationAdInterstitial.showInterstitialAdOnTime(activity, isPurchased, listener)
            return
        }
        if (isPurchased) {
            listener.onError("You have pro version")
            return
        }
        AdsConstants.interstitialClickAdCounter = 0
        AdsConstants.interstitialClickAdCounter++
        this.adMobKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_ADMOB_INTERSTITIAL_ID
        } else {
            if (AdsApplication.getAdsModel()!!.admobMediation) {
                AdsApplication.getAdsModel()!!.admobMediationInterstitialId

            } else {
                AdsApplication.getAdsModel()!!.admobInterstitialID
            }
        }
        this.maxKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_MAX_INTERSTITIAL_ADS_ID
        } else {
            AdsApplication.getAdsModel()!!.maxInterstitialID
        }
/*        progressDialogUtils = ProgressDialogUtils(activity)
        progressDialogUtils!!.showDialog("Loading", "Wait while ad is loading...")*/
        try {
            this.activityRef = WeakReference(activity)
            showAd = true
            logD("Interstitial Ad Ids---Admob: ${this.adMobKey} ----Max: ${this.maxKey}")
            showSelectedAd()
            Handler(Looper.getMainLooper()).postDelayed({
                if (this.showAd && this.interstitialListener != null) {
                   /* try {
                        progressDialogUtils!!.isShowDialog()
                    } catch (error: Exception) {
                        logException("Interstitial Ads Error : ${error.localizedMessage}")
                    }*/
                    this.showAd = false
                    this.timer = 2000L
                }
            }, timer)
        } catch (error: Exception) {
            logException("Interstitial Ads Error : ${error.localizedMessage}")
            if (this.interstitialListener != null) {
                this.interstitialListener!!.onError(error.localizedMessage!!)
            }
            finishAd()
        }
    }


    private fun showAdmobInterstitialAd() {
        if (AdsUtils.isOnline(this.activity!!).not()) {
            logD("is Offline ")
            this.interstitialListener!!.isOffline(true)
            return
        }
        if (this.showAd) {
            if (this.admobInterstitialAD == null) {
                onError("Admob interstitial ad is null")
            } else {
//                progressDialogUtils!!.dialogDismiss()
                this.showAd = false
                if (this.interstitialListener != null) {
                    this.interstitialListener!!.onBeforeAdShow()
                }
                this.admobInterstitialAD!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            super.onAdFailedToShowFullScreenContent(error)
                            onError(error.message)
                            AdsApplication.analyticsEvent(
                                "amb_failedToShow",
                                "Inters admob ad failedShow"
                            )
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            if (interstitialListener != null) {
                                interstitialListener!!.onDismisses(AdsConstants.AD_MOB)
                            }
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            AdsApplication.analyticsEvent(
                                "amb_AdShowdFlScr",
                                "Inters admob ad showFullScreen"
                            )
                            admobInterstitialAD = null
                            AdsApplication.setTime(
                                activityRef!!.get()!!,
                                AdsConstants.INTERSTITIAL_KEY
                            )
                            logD("The admob ad was shown.")
                        }
                    }
                this.admobInterstitialAD!!.show(this.activity!!)
                initSelectedAd()
            }
        }
    }

    private fun showMaxInterstitialAd() {
        if (AdsUtils.isOnline(this.activity!!).not()) {
            logD("is Offline ")
            this.interstitialListener!!.isOffline(true)
            return
        }
        if (this.maxInterstitialAd != null) {
            if (this.maxInterstitialAd!!.isReady) {
                this.maxInterstitialAd!!.showAd()
            }
        }
    }

    private fun initAdMobInterstitialAd() {
        try {
            if (AdsApplication.isAdmobInLimit()) {
                if (AdsApplication.applyLimitOnAdmob) {
                    onError("Interstitial add banned in current duw to admob limit")
                    return
                }
            }
            if (this.adMobKey!!.isEmpty() || this.adMobKey!!.isBlank()) {
                onError("Null IDS Found")
                return
            }
            if (AdsUtils.isOnline(this.activity!!).not()) {
                logD("is Offline ")
                this.interstitialListener!!.isOffline(true)
                return
            }
            if (this.adMobKey == AdsConstants.TEST_ADMOB_INTERSTITIAL_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    onError("NULL OR TEST IDS FOUND")
                    this.interstitialListener!!.onError("NULL OR TEST IDS FOUND")
                    return
                }
            }
            AdsConstants.loadAdmobInters++
            logD("Load Admob : ${AdsConstants.loadAdmobInters}")
            AdsApplication.analyticsEvent("admb_req", "Send interstitial admob request")
            InterstitialAd.load(
                this.activity!!,
                this.adMobKey!!,
                AdsApplication.getAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ads: InterstitialAd) {
                        super.onAdLoaded(ads)
                        admobInterstitialAD = ads
                        if (interstitialListener != null) {
                            interstitialListener!!.onLoaded(AdsConstants.AD_MOB)
                        }
                        AdsApplication.analyticsEvent("admob_load", "Inters admob ad loaded")
                    }

                    override fun onAdFailedToLoad(err: LoadAdError) {
                        super.onAdFailedToLoad(err)
                        logD("Admob Front Ad  Error : ${err.message}")
                        AdsApplication.analyticsEvent(
                            "adm_failedload",
                            "Inters admob ad failedload"
                        )
                        initSelectedAd()
                        admobInterstitialAD = null
                    }
                })
        } catch (error: Exception) {
            logException("Init Interstitial Ads Error : ${error.localizedMessage}")
        }
    }

    private fun initMaxInterstitialAd() {
        if (this.maxKey!!.isEmpty() || this.maxKey!!.isBlank()) {
            onError("NULL  IDS FOUND")
            return
        }
        if (AdsUtils.isOnline(this.activity!!).not()) {
            logD("is Offline ")
            this.interstitialListener!!.isOffline(true)
            return
        }
        if (this.maxKey == AdsConstants.TEST_MAX_INTERSTITIAL_ADS_ID) {
            logD("Test Ids")
            if (AdsConstants.testMode.not()) {
                onError("NULL OR TEST IDS FOUND")
                this.interstitialListener!!.onError("NULL OR TEST IDS FOUND")
                return
            }
        }
        AdsApplication.analyticsEvent("admb_req", "Send interstitial max request")
        maxInterstitialAd = MaxInterstitialAd(this.maxKey!!, activityRef!!.get()!!)
        maxInterstitialAd!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd?) {
                showAd = true
                AdsConstants.canShowInterstitial = true
                logD("MAX MEDIATION FRONT AD Loaded")
                if (interstitialListener != null) {
                    interstitialListener!!.onLoaded(AdsConstants.AD_MOB)
                }
                AdsApplication.analyticsEvent("max_loaded", "Inters max ad loaded")
                showMaxInterstitialAd()
            }

            override fun onAdDisplayed(p0: MaxAd?) {
                AdsApplication.analyticsEvent(
                    "max_AdShow",
                    "Inters max ad showFullScreen"
                )
                maxInterstitialAd = null
                AdsApplication.setTime(
                    activityRef!!.get()!!,
                    AdsConstants.INTERSTITIAL_KEY
                )
                logD("The max ad was shown")
            }

            override fun onAdHidden(p0: MaxAd?) {
                logD("MAX FRONT Dismissed")
                if (interstitialListener != null) {
                    interstitialListener!!.onDismisses(AdsConstants.MAX_MEDIATION)
                }
            }

            override fun onAdClicked(p0: MaxAd?) {

            }

            override fun onAdLoadFailed(p0: String?, adError: MaxError?) {
                logD("MAX FRONT AD Error : ${adError!!.message}")
                AdsApplication.analyticsEvent("max_failed", "Inters max ad failed load")
                onLoadError()
                if (AdsApplication.isAdmobInLimit()) {
                    AdsApplication.applyLimitOnAdmob = true
                }
                maxInterstitialAd = null
            }

            override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {
                AdsApplication.analyticsEvent(
                    "max_failedShow",
                    "Inters max ad failed show"
                )
            }
        })
        maxInterstitialAd!!.setRevenueListener { ad ->
            val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
            adjustAdRevenue.setRevenue(ad?.revenue, "USD")
            adjustAdRevenue.setAdRevenueNetwork(ad?.networkName)
            adjustAdRevenue.setAdRevenueUnit(ad?.adUnitId)
            adjustAdRevenue.setAdRevenuePlacement(ad?.placement)
            Adjust.trackAdRevenue(adjustAdRevenue)
        }
        maxInterstitialAd!!.loadAd()
    }

    private fun showSelectedAd() {
        if (AdsApplication.canShow(this.activity!!, AdsConstants.INTERSTITIAL_KEY, 0).not()) {
            this.interstitialListener!!.onError("Time limit: Time is remaining to show! ")
            finishAd()
            return
        }
        when (AdsApplication.getAdsModel()!!.strategy.toInt()) {
            AdsConstants.ADS_OFF -> {
                this.interstitialListener!!.isEnableAds(false)
            }
            AdsConstants.AD_MOB_MEDIATION -> {
                this.interstitialListener!!.isEnableAds(true)
                showAdmobInterstitialAd()
            }
            AdsConstants.AD_MOB -> {
                this.interstitialListener!!.isEnableAds(true)
                showAdmobInterstitialAd()
            }

            AdsConstants.MAX_MEDIATION -> {
                this.interstitialListener!!.isEnableAds(true)
                showMaxInterstitialAd()
            }

            else -> {
                this.interstitialListener!!.onError("You have to select priority type ADMOB or ADMOB MEDIATION or MAX MEDIATION")
                finishAd()
            }
        }
    }

    private fun initSelectedAd() {
        when (AdsApplication.getAdsModel()!!.strategy.toInt()) {
            AdsConstants.ADS_OFF -> {
                this.interstitialListener!!.isEnableAds(false)
            }

            AdsConstants.AD_MOB_MEDIATION -> {
                this.interstitialListener!!.isEnableAds(true)
                initAdMobInterstitialAd()
            }

            AdsConstants.AD_MOB -> {
                this.interstitialListener!!.isEnableAds(true)
                initAdMobInterstitialAd()
            }

            AdsConstants.MAX_MEDIATION -> {
                this.interstitialListener!!.isEnableAds(true)
                initMaxInterstitialAd()
            }

            else -> {
                finishAd()
            }
        }
    }

    private fun finishAd() {
        this.interstitialListener = null
//        progressDialogUtils!!.isShowDialog()
    }

    private fun onError(error: String) {
        if (interstitialListener != null) {
            interstitialListener!!.onError(error)
        }
        finishAd()
    }

    private fun onLoadError() {
        FirebaseCrashlytics.getInstance().log("Interstitial Ads Error")
//        try {
//            progressDialogUtils!!.isShowDialog()
//        } catch (error: Exception) {
//            logException("onLoadError : ${error.localizedMessage}")
//        }
    }
}
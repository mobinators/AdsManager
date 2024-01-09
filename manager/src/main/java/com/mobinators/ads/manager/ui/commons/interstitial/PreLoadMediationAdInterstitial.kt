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
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object PreLoadMediationAdInterstitial {
    private var interstitialAdListener: InterstitialAdsListener? = null
    private var activityRe: WeakReference<Activity>? = null
    private var maxInterstitialAd: MaxInterstitialAd? = null
    private var admobInterstitialAd: InterstitialAd? = null
    private var adMobKey: String? = null
    private var maxKey: String? = null
    private var showAd: Boolean = true
    private var timer = 3000L
    private var num = -1
    fun showInterstitialAdOnTime(
        activity: Activity,
        isPurchased: Boolean,
        listener: InterstitialAdsListener
    ) {
        if (isPurchased) {
            listener.onError("You have pro version")
            return
        }
        try {
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
            this.activityRe = WeakReference(activity)
            this.interstitialAdListener = listener
            this.showAd = true
            logD("Interstitial Ad Ids ---- ADMOB : ${this.adMobKey} ------ MAX : ${this.maxKey}")
            if (this.admobInterstitialAd != null) {
                showSelectedAd()
                return
            }
            initInterstitialOnTimeLoad(activity, isPurchased)

            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (showAd && interstitialAdListener != null) {
                        if (num != 3) {
                            logD("run")
                        }
                        if (AdsApplication.getFirebaseAnalytics() == null) {
                            logD("Unable to log events! FirebaseAnalytics object is null")
                        }
                        interstitialAdListener!!.onError(" Pre load Delay Time is Finished")
                        showAd = false
                        timer = 2000L
                    }

                }, timer
            )
        } catch (error: Exception) {
            logException("Preload Interstitial Ads Error : ${error.localizedMessage}")
            if (interstitialAdListener != null) {
                interstitialAdListener!!.onError(error.localizedMessage!!)
            }
            finishAd()
        }
    }


    private fun initInterstitialOnTimeLoad(activity: Activity, isPurchased: Boolean) {
        try {
            if (AdsConstants.isInit.not()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    logD("not init ads")
                    initInterstitialOnTimeLoad(activity, isPurchased)
                }, 500)
                return
            }
            logD("pass success")
            if (isPurchased) {
                return
            }
            initSelectedAd()
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }


    private fun initAdmobInterstitialAd() {
        try {
            if (AdsApplication.isAdmobInLimit()) {
                if (AdsApplication.applyLimitOnAdmob) {
                    onError("Interstitial add banned in current due to admob limit")
                    return
                }
            }
            if (this.adMobKey!!.isEmpty() || this.adMobKey!!.isBlank()) {
                onError("NULL  IDS FOUND")
                return
            }
            if (AdsUtils.isOnline(this.activityRe!!.get()!!).not()) {
                logD("is Offline ")
                this.interstitialAdListener!!.isOffline(true)
                return
            }
            if (this.adMobKey == AdsConstants.TEST_ADMOB_BANNER_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    onError("NULL OR TEST IDS FOUND")
                    this.interstitialAdListener!!.onError("NULL OR TEST IDS FOUND")
                    return
                }
            }
            AdsConstants.loadAdmobInters++
            logD("Load Admob : ${AdsConstants.loadAdmobInters}")
            AdsApplication.analyticsEvent("admb_req", "Send interstitial admob request")
            InterstitialAd.load(
                activityRe!!.get()!!,
                adMobKey!!,
                AdsApplication.getAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(loadAd: InterstitialAd) {
                        super.onAdLoaded(loadAd)
                        admobInterstitialAd = loadAd
                        showAd = true
                        AdsConstants.canShowInterstitial = true
                        logD("ADMOB FRONT AD Loaded")
                        if (interstitialAdListener != null) {
                            interstitialAdListener!!.onLoaded(AdsConstants.AD_MOB)
                        }
                        AdsApplication.analyticsEvent("admob_loaded", "Inters admob ad loaded")
                        showAdmobInterstitialAd()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        logD("ADMOB FRONT AD Error : ${adError.message}")
                        AdsApplication.analyticsEvent("adm_failed", "Inters admob ad failed load")
                        if (AdsApplication.isAdmobInLimit()) {
                            AdsApplication.applyLimitOnAdmob = true
                        }
                        admobInterstitialAd = null
                    }
                })
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun showAdmobInterstitialAd() {
        try {
            if (showAd) {
                if (admobInterstitialAd == null) {
                    onError("Admob interstitial ad is null")
                } else {
                    showAd = false
                    if (interstitialAdListener != null) {
                        interstitialAdListener!!.onBeforeAdShow()
                    }
                    admobInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                onError(p0.message)
                                AdsApplication.analyticsEvent(
                                    "amb_failedShow",
                                    "Inters admob ad failed show"
                                )
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                logD("ADMOB FRONT Dismissed")
                                if (interstitialAdListener != null) {
                                    interstitialAdListener!!.onDismisses(AdsConstants.AD_MOB)
                                }
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                AdsApplication.analyticsEvent(
                                    "amb_AdShow",
                                    "Inters admob ad showFullScreen"
                                )
                                admobInterstitialAd = null
                                AdsApplication.setTime(
                                    activityRe!!.get()!!,
                                    AdsConstants.INTERSTITIAL_KEY
                                )
                                logD("The admob ad was shown")
                            }
                        }
                    admobInterstitialAd!!.show(activityRe!!.get()!!)
                }
            }
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun showMaxInterstitialAd() {
        try {
            if (this.maxInterstitialAd != null) {
                if (this.maxInterstitialAd!!.isReady) {
                    this.maxInterstitialAd!!.showAd()
                }
            }
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun initMaxInterstitialAd() {
        try {
            if (this.maxKey!!.isEmpty() || this.maxKey!!.isBlank()) {
                onError("NULL  IDS FOUND")
                return
            }
            if (AdsUtils.isOnline(this.activityRe!!.get()!!).not()) {
                logD("is Offline ")
                this.interstitialAdListener!!.isOffline(true)
                return
            }
            if (this.adMobKey == AdsConstants.TEST_ADMOB_BANNER_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    onError("NULL OR TEST IDS FOUND")
                    this.interstitialAdListener!!.onError("NULL OR TEST IDS FOUND")
                    return
                }
            }
            AdsApplication.analyticsEvent("admb_req", "Send interstitial max request")
            maxInterstitialAd = MaxInterstitialAd(this.maxKey!!, activityRe!!.get()!!)
            maxInterstitialAd!!.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    showAd = true
                    AdsConstants.canShowInterstitial = true
                    logD("MAX MEDIATION FRONT AD Loaded")
                    if (interstitialAdListener != null) {
                        interstitialAdListener!!.onLoaded(AdsConstants.AD_MOB)
                    }
                    AdsApplication.analyticsEvent("max_loaded", "Inters max ad loaded")
                    showMaxInterstitialAd()
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    AdsApplication.analyticsEvent(
                        "max_AdShow",
                        "Inters max ad showFullScreen"
                    )
                    maxInterstitialAd = null
                    AdsApplication.setTime(
                        activityRe!!.get()!!,
                        AdsConstants.INTERSTITIAL_KEY
                    )
                    logD("The max ad was shown")
                }

                override fun onAdHidden(p0: MaxAd) {
                    logD("MAX FRONT Dismissed")
                    if (interstitialAdListener != null) {
                        interstitialAdListener!!.onDismisses(AdsConstants.MAX_MEDIATION)
                    }
                }

                override fun onAdClicked(p0: MaxAd) {

                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    logD("MAX FRONT AD Error : ${p1.message}")
                    AdsApplication.analyticsEvent("max_failed", "Inters max ad failed load")
                    if (AdsApplication.isAdmobInLimit()) {
                        AdsApplication.applyLimitOnAdmob = true
                    }
                    maxInterstitialAd = null
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    AdsApplication.analyticsEvent(
                        "max_failedShow",
                        "Inters max ad failed show"
                    )
                }
            })
            maxInterstitialAd!!.setRevenueListener { ad ->
                val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                adjustAdRevenue.setRevenue(ad.revenue, "USD")
                adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
                adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
                adjustAdRevenue.setAdRevenuePlacement(ad.placement)
                Adjust.trackAdRevenue(adjustAdRevenue)
            }
            maxInterstitialAd!!.loadAd()
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun initSelectedAd() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> {}
                AdsConstants.AD_MOB_MEDIATION -> {}
                AdsConstants.AD_MOB -> {
                    initAdmobInterstitialAd()
                }

                AdsConstants.MAX_MEDIATION -> {
                    initMaxInterstitialAd()
                }

                else -> {
                    interstitialAdListener!!.onError("You Have to select priority type AdMob or  AdMob Mediation or Max Mediation")
                    finishAd()
                }
            }
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun showSelectedAd() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> {}
                AdsConstants.AD_MOB_MEDIATION -> {
                    showAdmobInterstitialAd()
                }

                AdsConstants.AD_MOB -> {
                    showAdmobInterstitialAd()
                }

                AdsConstants.MAX_MEDIATION -> {
                    showMaxInterstitialAd()
                }

                else -> {
                    interstitialAdListener!!.onError("You Have to select priority type AdMob or  AdMob Mediation or Max Mediation")
                    finishAd()
                }
            }
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun finishAd() {
        this.interstitialAdListener = null
    }

    private fun onError(errorMessage: String) {
        try {
            FirebaseCrashlytics.getInstance().log(errorMessage)
            if (this.interstitialAdListener != null) {
                this.interstitialAdListener!!.onError(errorMessage)
            }
            finishAd()
        } catch (error: Exception) {
            this.interstitialAdListener!!.onError(error = "showInterstitialAds Error : ${error.localizedMessage}")
        }
    }
}
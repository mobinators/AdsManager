package com.mobinators.ads.manager.ui.commons.openad

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
import com.applovin.mediation.ads.MaxAppOpenAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.listener.OpenAddCallback
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.preferenceUtils
import java.util.Date

@SuppressLint("StaticFieldLeak")
object MediationOpenAd {
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    private var openAddCallback: OpenAddCallback? = null
    private var maxAppOpenAd: MaxAppOpenAd? = null
    private var currentActivity: Activity? = null
    private var maxAppOpenAdId: String? = null
    private var admobOpenAdKey: String? = null
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd: Boolean = false
    private var loadTime: Long = 0

    fun loadAppOpenAd(activity: Activity, listener: OpenAddCallback) {
        this.currentActivity = activity
        this.openAddCallback = listener
        this.admobOpenAdKey = if (AdsConstants.isInit) {
            AdsConstants.TEST_ADMOB_OPEN_APP_ID
        } else {
            AdsApplication.getAdsModel()!!.admobOpenAdID
        }
        this.maxAppOpenAdId = if (AdsConstants.isInit) {
            AdsConstants.TEST_MAX_APP_OPEN_ADS_ID
        } else {
            AdsApplication.getAdsModel()!!.maxAppOpenID
        }
        if (AdsConstants.isAppOpenAdEnable.not()) {
            openAddCallback!!.onErrorToShow("App open ad disable from remote")
            return
        }
        if (AdsConstants.isInit.not()) {
            Handler(Looper.getMainLooper()).postDelayed({
                loadAppOpenAd(activity, listener)
            }, 2000)
        }
        if (checkIfAdCanBeShow(openAddCallback!!).not()) {
            return
        }
        selectAd()
    }


    private fun selectAd() {
        logD("Strategy Id; ${AdsApplication.getAdsModel()!!.strategy}")
        when (AdsApplication.getAdsModel()!!.strategy.toInt()) {
            AdsConstants.ADS_OFF -> {
                this.openAddCallback!!.isEnableAds(false)
            }

            AdsConstants.AD_MOB_MEDIATION -> {
                this.openAddCallback!!.isEnableAds(true)
            }

            AdsConstants.AD_MOB -> {
                this.openAddCallback!!.isEnableAds(true)
                fetchAd()
            }

            AdsConstants.MAX_MEDIATION -> {
                this.openAddCallback!!.isEnableAds(true)
                maxAppOpenAd()
            }
        }
    }

    private fun checkIfAdCanBeShow(listener: OpenAddCallback): Boolean {
        var counter = currentActivity!!.preferenceUtils.getIntegerValue(AdsConstants.OPEN_AD_KEY)
        return if (counter in 1..2) {
            listener.onErrorToShow("Open ad show after ${(3 - counter)} : time load app")
            counter++
            currentActivity!!.preferenceUtils.setIntegerValue(AdsConstants.OPEN_AD_KEY, counter)
            false
        } else {
            listener.onErrorToShow("checkIfAdCanBeShow : Interval 3 : set counter: $counter ")
            counter = 0
            counter++
            currentActivity!!.preferenceUtils.setIntegerValue(AdsConstants.ADS_MODEL_KEY, counter)
            true
        }
    }

    private fun fetchAd() {
        if (AdsApplication.isAdmobInLimit()) {
            if (AdsApplication.applyLimitOnAdmob) {
                openAddCallback!!.onErrorToShow("admob limit is applied")
                return
            }
        }
        if (isAdAvailable()) {
            return
        }
        if (AdsUtils.isOnline(this.currentActivity!!).not()) {
            logD("is Offline ")
            this.openAddCallback!!.isOffline(true)
            return
        }
        if (this.admobOpenAdKey == AdsConstants.TEST_ADMOB_OPEN_APP_ID) {
            logD("Test Ids")
            if (AdsConstants.testMode.not()) {
                logD("NULL OR TEST IDS FOUND")
                this.openAddCallback!!.onErrorToShow("NULL OR TEST IDS FOUND")
                return
            }
        }
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(openAd: AppOpenAd) {
                super.onAdLoaded(openAd)
                appOpenAd = openAd
                loadTime = Date().time
                AdsApplication.analyticsEvent("openAdLoaded", "onAddLoadedCalled")
                showAdIfAvailable()
            }

            override fun onAdFailedToLoad(loadError: LoadAdError) {
                super.onAdFailedToLoad(loadError)
                openAddCallback!!.onErrorToShow(loadError.message)
                if (AdsApplication.isAdmobInLimit()) {
                    AdsApplication.applyLimitOnAdmob = true
                }
                AdsApplication.analyticsEvent("openAdFailedToLoad", loadError.message)
            }
        }
        AppOpenAd.load(
            this.currentActivity!!,
            this.admobOpenAdKey!!,
            getAdRequest(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback!!
        )
        AdsApplication.analyticsEvent("openRequestAd", "On Request sent for ad")
    }

    private fun isAdAvailable(): Boolean {
        return (appOpenAd != null || maxAppOpenAd != null) && wasLoadTimeLessThanHoursAgo()
    }

    private fun wasLoadTimeLessThanHoursAgo(): Boolean {
        val dateDifference = (Date().time - loadTime)
        val milliSecondPerHour = 3600000
        return (dateDifference < (milliSecondPerHour * 4))
    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().setHttpTimeoutMillis(5000).build()
    }

    private fun showAdIfAvailable() {
        if (isShowingAd.not() && isAdAvailable()) {
            logD("Will show ad.")
            val screenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        appOpenAd = null
                        isShowingAd = false
                        openAddCallback!!.onDismissClick()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        openAddCallback!!.onErrorToShow(adError.message)
                        AdsApplication.analyticsEvent("failedToShowFullScreen", adError.message)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        isShowingAd = true
                        AdsApplication.analyticsEvent("show_success", "onAdShowedFullScreenContent")
                    }
                }
            appOpenAd!!.fullScreenContentCallback = screenContentCallback
            appOpenAd!!.show(currentActivity!!)
        }
    }

    private fun maxAppOpenAd() {
        if (isAdAvailable()) {
            logD("Ad Available")
            return
        }
        if (AdsUtils.isOnline(this.currentActivity!!).not()) {
            logD("is Offline ")
            this.openAddCallback!!.isOffline(true)
            return
        }
        if (this.maxAppOpenAdId == AdsConstants.TEST_MAX_APP_OPEN_ADS_ID) {
            logD("Test Ids")
            if (AdsConstants.testMode.not()) {
                logD("NULL OR TEST IDS FOUND")
                this.openAddCallback!!.onErrorToShow("NULL OR TEST IDS FOUND")
                return
            }
        }
        logD("Max Open Ads ID: ${this.maxAppOpenAdId}")
        maxAppOpenAd = MaxAppOpenAd(this.maxAppOpenAdId!!, this.currentActivity!!)
        maxAppOpenAd!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd?) {
                logD("onAdLoaded")
                showMaxAds()
                loadTime = Date().time
                AdsApplication.analyticsEvent("openAdLoaded", "onAddLoadedCalled")
            }

            override fun onAdDisplayed(p0: MaxAd?) {
                logD("onAdDisplayed")
                isShowingAd = true
            }

            override fun onAdHidden(p0: MaxAd?) {
                logD("onAdHidden")
            }

            override fun onAdClicked(p0: MaxAd?) {
                logD("onAdClicked")
            }

            override fun onAdLoadFailed(p0: String?, loadError: MaxError?) {
                logD("onAdLoadFailed")
                isShowingAd = false
                openAddCallback!!.onErrorToShow(loadError!!.message)
            }

            override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {
                logD("onAdDisplayFailed")
                isShowingAd = false
            }
        })
        maxAppOpenAd!!.setRevenueListener {
            val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
            adjustAdRevenue.setRevenue(it?.revenue, "USD")
            adjustAdRevenue.setAdRevenueNetwork(it?.networkName)
            adjustAdRevenue.setAdRevenueUnit(it?.adUnitId)
            adjustAdRevenue.setAdRevenuePlacement(it?.placement)
            Adjust.trackAdRevenue(adjustAdRevenue)
        }
        maxAppOpenAd!!.loadAd()
    }

    private fun showMaxAds() {
        logD("show Open Ad")
        if (maxAppOpenAd!!.isReady) {
            maxAppOpenAd!!.showAd()
        }
    }
}
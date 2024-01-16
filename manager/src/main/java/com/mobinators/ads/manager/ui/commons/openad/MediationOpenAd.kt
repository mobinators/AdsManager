package com.mobinators.ads.manager.ui.commons.openad

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

@SuppressLint("StaticFieldLeak")
object MediationOpenAd {

    private var activityRef: Activity? = null
    private var admobAppOpenAdsID: String? = null
    private var admobAppOPenAd: AppOpenAd? = null
    private var loadedCallback: AdsLoadedCallback? = null
    private var maxAppOpenAdsId: String? = null
    private var maxAppOpenAds: MaxAppOpenAd? = null
    private var showCallback: AdsShowAppOpenCallback? = null
    private var contextRef: Context? = null
    var isShowingAd: Boolean = false

    fun loadAppOpenAds(activity: Context, isPurchased: Boolean, listener: AdsLoadedCallback) {
        this.contextRef = activity
        this.loadedCallback = listener
        if (isPurchased) {
            this.loadedCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.loadedCallback!!.onAdsError(errorState = AdsErrorState.NETWORK_OFF)
            return
        }
        if (AdsApplication.getAdsModel()!!.isAppOpenAdd.not()) {
            logException("App OPen Ads is not enable")
            return
        }
        initSelectedAppOPenAds()
    }

    fun showAppOpenAds(activity: Activity, isPurchased: Boolean, listener: AdsShowAppOpenCallback) {
        this.activityRef = activity
        this.showCallback = listener
        if (isPurchased) {
            this.showCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
      /*  if (AdsApplication.getAdsModel()!!.isAppOpenAdd.not()) {
            logException("App OPen Ads is not enable")
            return
        }*/
        showSelectedAppOpenAds()

    }

    private fun initSelectedAppOPenAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.loadedCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> logD("No Admob Mediation For App OPen Ads")
                AdsConstants.AD_MOB -> initAppOpenAds()
                AdsConstants.MAX_MEDIATION -> initMaxAppOpenAds()
                else -> this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }
        } catch (error: Exception) {
            logException("Init Selected App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun initAppOpenAds() {
        try {
            this.admobAppOpenAdsID = if (AdsConstants.testMode) {
                AdsConstants.TEST_ADMOB_OPEN_APP_ID
            } else {
                AdsApplication.getAdsModel()!!.admobOpenAdID
            }

            if (this.admobAppOpenAdsID.isNullOrEmpty() || this.admobAppOpenAdsID.isNullOrBlank()) {
                this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.admobAppOpenAdsID == AdsConstants.TEST_ADMOB_OPEN_APP_ID) {
                    this.loadedCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            AppOpenAd.load(
                this.contextRef!!,
                this.admobAppOpenAdsID!!,
                AdsApplication.getAdRequest(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(p0: AppOpenAd) {
                        super.onAdLoaded(p0)
                        this@MediationOpenAd.admobAppOPenAd = p0
                        this@MediationOpenAd.loadedCallback!!.onAdsLoaded()
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        this@MediationOpenAd.admobAppOPenAd = null
                        this@MediationOpenAd.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                    }
                })
        } catch (error: Exception) {
            logException("Init App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun initMaxAppOpenAds() {
        try {
            this.maxAppOpenAdsId = if (AdsConstants.testMode) {
                AdsConstants.TEST_MAX_APP_OPEN_ADS_ID
            } else {
                AdsApplication.getAdsModel()!!.maxAppOpenID
            }
            if (this.maxAppOpenAdsId.isNullOrEmpty() || this.maxAppOpenAdsId.isNullOrBlank()) {
                this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.maxAppOpenAdsId == AdsConstants.TEST_MAX_APP_OPEN_ADS_ID) {
                    this.loadedCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            this.maxAppOpenAds = MaxAppOpenAd(this.maxAppOpenAdsId!!, this.contextRef!!)
            this.maxAppOpenAds!!.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    this@MediationOpenAd.loadedCallback!!.onAdsLoaded()
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    this@MediationOpenAd.isShowingAd = true
                    this@MediationOpenAd.showCallback?.onAdsDisplay()
                }

                override fun onAdHidden(p0: MaxAd) {
                    this@MediationOpenAd.isShowingAd = false
                    this@MediationOpenAd.showCallback?.onAdsError(errorState = AdsErrorState.ADS_DISMISS)
                }

                override fun onAdClicked(p0: MaxAd) {
                    this@MediationOpenAd.showCallback!!.onAdsClicked()
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    this@MediationOpenAd.isShowingAd = false
                    this@MediationOpenAd.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    this@MediationOpenAd.isShowingAd = false
                    this@MediationOpenAd.showCallback?.onAdsError(errorState = AdsErrorState.ADS_DISPLAY_FAILED)
                }
            })
            this.maxAppOpenAds!!.setRevenueListener {
                val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                adjustAdRevenue.setRevenue(it.revenue, "USD")
                adjustAdRevenue.setAdRevenueNetwork(it.networkName)
                adjustAdRevenue.setAdRevenueUnit(it.adUnitId)
                adjustAdRevenue.setAdRevenuePlacement(it.placement)
                Adjust.trackAdRevenue(adjustAdRevenue)
            }
            this.maxAppOpenAds!!.loadAd()
        } catch (error: Exception) {
            logException("Init Max App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showSelectedAppOpenAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.showCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> logD("No Admob Mediation For App OPen Ads")
                AdsConstants.AD_MOB -> showAdmobAppOpenAds()
                AdsConstants.MAX_MEDIATION -> showMaxAppOpenAds()
                else -> this.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }

        } catch (error: Exception) {
            logException("Show App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showAdmobAppOpenAds() {
        try {
            if (this.admobAppOPenAd != null) {
                if (MediationRewardedAd.isAdsShow.not() || MediationAdInterstitial.isAdsShow.not() || MediationRewardedInterstitialAd.isAdsShow.not() || isShowingAd.not()) {
                    this.admobAppOPenAd!!.show(this.activityRef!!)
                    this.admobAppOPenAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                super.onAdClicked()
                                this@MediationOpenAd.showCallback!!.onAdsClicked()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                this@MediationOpenAd.isShowingAd = true
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                this@MediationOpenAd.isShowingAd = false
                                this@MediationOpenAd.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_DISMISS)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                this@MediationOpenAd.isShowingAd = false
                                this@MediationOpenAd.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_DISPLAY_FAILED)
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                this@MediationOpenAd.isShowingAd = false
                                this@MediationOpenAd.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_IMPRESS)
                            }
                        }
                }
                initSelectedAppOPenAds()
            } else {
                initSelectedAppOPenAds()
            }
        } catch (error: Exception) {
            logException(" Show App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showMaxAppOpenAds() {
        try {
            if (this.maxAppOpenAds!!.isReady) {
                if (MediationRewardedAd.isAdsShow.not() || MediationAdInterstitial.isAdsShow.not() || MediationRewardedInterstitialAd.isAdsShow.not() || isShowingAd.not()) {
                    this.maxAppOpenAds!!.showAd()
                }
                initSelectedAppOPenAds()
            } else {
                initSelectedAppOPenAds()
            }
        } catch (error: Exception) {
            logException("Show Max App Open Ads Error : ${error.localizedMessage}")
        }
    }

    interface AdsLoadedCallback {
        fun onAdsOff()
        fun onAdsLoaded()
        fun onAdsError(errorState: AdsErrorState)
    }

    interface AdsShowAppOpenCallback {
        fun onAdsOff()
        fun onAdsClicked()
        fun onAdsDisplay()
        fun onAdsError(errorState: AdsErrorState)
    }
    /*  private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
      private var openAddCallback: OpenAddCallback? = null
      private var maxAppOpenAd: MaxAppOpenAd? = null
      private var currentActivity: Activity? = null
      private var maxAppOpenAdId: String? = null
      private var admobOpenAdKey: String? = null
      private var appOpenAd: AppOpenAd? = null
      private var isShowingAd: Boolean = false
      private var loadTime: Long = 0

      fun loadAppOpenAd(activity: Activity, isPurchased: Boolean, listener: OpenAddCallback) {
          this.currentActivity = activity
          this.openAddCallback = listener
          if (isPurchased) {
              openAddCallback!!.onErrorToShow("You have pro version")
              return
          }
          try {
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
                      loadAppOpenAd(activity, isPurchased, listener)
                  }, 2000)
              }
              if (checkIfAdCanBeShow(openAddCallback!!).not()) {
                  return
              }
              selectAd()
          } catch (error: Exception) {
              this.openAddCallback!!.onErrorToShow("showOpenAds Error : ${error.localizedMessage}")
          }
      }


      private fun selectAd() {
          try {
              when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
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
          } catch (error: Exception) {
              this.openAddCallback!!.onErrorToShow("showOpenAds Error : ${error.localizedMessage}")
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
          try {
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
          } catch (error: Exception) {
              this.openAddCallback!!.onErrorToShow("showOpenAds Error : ${error.localizedMessage}")
          }
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
          try {
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
                              AdsApplication.analyticsEvent(
                                  "show_success",
                                  "onAdShowedFullScreenContent"
                              )
                          }
                      }
                  appOpenAd!!.fullScreenContentCallback = screenContentCallback
                  appOpenAd!!.show(currentActivity!!)
              }
          } catch (error: Exception) {
              this.openAddCallback!!.onErrorToShow("showOpenAds Error : ${error.localizedMessage}")
          }
      }

      private fun maxAppOpenAd() {
          try {
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
                  override fun onAdLoaded(p0: MaxAd) {
                      logD("onAdLoaded")
                      showMaxAds()
                      loadTime = Date().time
                      AdsApplication.analyticsEvent("openAdLoaded", "onAddLoadedCalled")
                  }

                  override fun onAdDisplayed(p0: MaxAd) {
                      logD("onAdDisplayed")
                      isShowingAd = true
                  }

                  override fun onAdHidden(p0: MaxAd) {
                      logD("onAdHidden")
                  }

                  override fun onAdClicked(p0: MaxAd) {
                      logD("onAdClicked")
                  }

                  override fun onAdLoadFailed(p0: String, p1: MaxError) {
                      logD("onAdLoadFailed")
                      isShowingAd = false
                      openAddCallback!!.onErrorToShow(p1.message)
                  }

                  override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                      logD("onAdDisplayFailed")
                      isShowingAd = false
                  }
              })
              maxAppOpenAd!!.setRevenueListener {
                  val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                  adjustAdRevenue.setRevenue(it.revenue, "USD")
                  adjustAdRevenue.setAdRevenueNetwork(it.networkName)
                  adjustAdRevenue.setAdRevenueUnit(it.adUnitId)
                  adjustAdRevenue.setAdRevenuePlacement(it.placement)
                  Adjust.trackAdRevenue(adjustAdRevenue)
              }
              maxAppOpenAd!!.loadAd()
          } catch (error: Exception) {
              this.openAddCallback!!.onErrorToShow("showOpenAds Error : ${error.localizedMessage}")
          }
      }

      private fun showMaxAds() {
          if (maxAppOpenAd!!.isReady) {
              maxAppOpenAd!!.showAd()
          }
      }*/
}
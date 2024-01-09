package com.mobinators.ads.manager.ui.commons.rewardedInter

import android.annotation.SuppressLint
import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.listener.OnRewardedAdListener
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

@SuppressLint("StaticFieldLeak")
object MediationRewardedInterstitialAd {
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var onRewardedAdListener: OnRewardedAdListener? = null
    private var admobRewardedInterKey: String? = null
    private var currentActivity: Activity? = null
    private var isAdShow: Boolean = false

    fun loadRewardedInterstitialAds(activity: Activity, isPurchased: Boolean) {
        this.currentActivity = activity
        if (isPurchased) {
            logD("You have purchased app")
            return
        }
        try {
            this.admobRewardedInterKey = if (AdsConstants.testMode) {
                AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID
            } else {
                AdsApplication.getAdsModel()!!.admobRewardedInterstitialID
            }
            if (AdsApplication.isAdmobInLimit()) {
                if (AdsApplication.applyLimitOnAdmob) {
                    logD("admob limit is applied")
                    return
                }
            }
            if (AdsUtils.isOnline(activity).not()) {
                logD("required internet")
                return
            }
            selectRewardedInterstitialAds(true)
        } catch (error: Exception) {

        }

    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        isPurchase: Boolean,
        listener: OnRewardedAdListener
    ) {
        this.currentActivity = activity
        this.onRewardedAdListener = listener
        if (isPurchase) {
            this.onRewardedAdListener!!.onError("You have purchase")
            return
        }
        try {
            if (AdsUtils.isOnline(activity).not()) {
                this.onRewardedAdListener!!.isOffline(true)
                return
            }
            selectRewardedInterstitialAds(false)
        } catch (error: Exception) {
            this.onRewardedAdListener!!.onError("showRewardInterstitialAds Error: ${error.localizedMessage}")
        }
    }

    private fun selectRewardedInterstitialAds(isAdsLoaded: Boolean) {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> {
                    this.onRewardedAdListener?.isEnableAds(false)
                }

                AdsConstants.AD_MOB_MEDIATION -> {
                    this.onRewardedAdListener?.isEnableAds(true)
                }

                AdsConstants.AD_MOB -> {
                    this.onRewardedAdListener?.isEnableAds(true)
                    if (isAdsLoaded) {
                        loadedRewardInit()
                    } else {
                        admobRewardInterstitialAdShow()
                    }
                }

                AdsConstants.MAX_MEDIATION -> {
                    this.onRewardedAdListener?.isEnableAds(true)
                }
            }
        } catch (error: Exception) {
            this.onRewardedAdListener?.onError("showRewardInterstitialAds Error: ${error.localizedMessage}")
        }
    }

    private fun loadedRewardInit() {
        try {
            if (this.admobRewardedInterKey!!.isEmpty() || this.admobRewardedInterKey!!.isBlank()) {
                logD("AdMob Interstitial Reward Ads Id is null")
                return
            }
            if (AdsUtils.isOnline(this.currentActivity!!).not()) {
                logD("is Offline ")
                return
            }
            if (this.admobRewardedInterKey == AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    logD("NULL OR TEST IDS FOUND")
                    return
                }
            }
            RewardedInterstitialAd.load(this.currentActivity!!, this.admobRewardedInterKey!!,
                AdsApplication.getAdRequest(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        logD("Ad was loaded.")
                        rewardedInterstitialAd = ad
                        val options = ServerSideVerificationOptions.Builder()
                            .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                            .build()
                        rewardedInterstitialAd!!.setServerSideVerificationOptions(options)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        logException("Ad Error : ${adError.message}")
                        rewardedInterstitialAd = null
                    }
                })
        } catch (error: Exception) {
            logException("Ad Error : ${error.message}")
        }
    }

    private fun admobRewardInterstitialAdShow() {
        try {
            if (rewardedInterstitialAd != null) {
                rewardedInterstitialAd!!.show(currentActivity!!) {
                    onRewardedAdListener!!.onRewarded(it)
                }
                rewardedInterstitialAd!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            super.onAdClicked()
                            onRewardedAdListener!!.onClicked(AdsConstants.AD_MOB)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            if (rewardedInterstitialAd!!.rewardItem.amount != null) {
                                onRewardedAdListener!!.onDismissClick(AdsConstants.AD_MOB, rewardedInterstitialAd!!.rewardItem)
                            } else {
                                onRewardedAdListener!!.onCancel(AdsConstants.AD_MOB)
                            }
                            rewardedInterstitialAd = null
                            selectRewardedInterstitialAds(true)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            rewardedInterstitialAd = null
                            selectRewardedInterstitialAds(true)
                            onRewardedAdListener!!.onError(p0.message)
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            onRewardedAdListener!!.onAdLoaded(AdsConstants.AD_MOB)
                        }
                    }
            } else {
                selectRewardedInterstitialAds(true)
            }
        } catch (error: Exception) {
            this.onRewardedAdListener!!.onError("showRewardInterstitialAds Error: ${error.localizedMessage}")
        }
    }


    /* fun loadRewardedInterstitialAd(
         activity: Activity,
         isPurchase: Boolean,
         listener: OnRewardedAdListener
     ) {
         this.currentActivity = activity
         this.onRewardedAdListener = listener
         if (isPurchase) {
             this.onRewardedAdListener!!.onError("You have purchase")
             return
         }
         try {
             this.admobRewardedInterKey = if (AdsConstants.isInit) {
                 AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID
             } else {
                 AdsApplication.getAdsModel()!!.admobRewardedInterstitialID
             }

             if (AdsApplication.isAdmobInLimit()) {
                 if (AdsApplication.applyLimitOnAdmob) {
                     onRewardedAdListener!!.onError("admob limit is applied")
                     return
                 }
             }
             if (AdsConstants.isInit.not()) {
                 Handler(Looper.getMainLooper()).postDelayed({
                     MediationRewardedAd.loadRewardedAd(activity, isPurchase, listener)
                 }, 2000)
             }
             selectAd()
         } catch (error: Exception) {
             this.onRewardedAdListener!!.onError("showRewardInterstitialAds Error: ${error.localizedMessage}")
         }
     }

     private fun selectAd() {
         try {
             when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                 AdsConstants.ADS_OFF -> {
                     this.onRewardedAdListener!!.isEnableAds(false)
                 }

                 AdsConstants.AD_MOB_MEDIATION -> {
                     this.onRewardedAdListener!!.isEnableAds(false)
                 }

                 AdsConstants.AD_MOB -> {
                     this.onRewardedAdListener!!.isEnableAds(false)
                     admobRewardInterstitialAd()
                 }

                 AdsConstants.MAX_MEDIATION -> {
                     this.onRewardedAdListener!!.isEnableAds(false)
                 }
             }
         } catch (error: Exception) {
             this.onRewardedAdListener!!.onError("showRewardInterstitialAds Error: ${error.localizedMessage}")
         }
     }

     private fun admobRewardInterstitialAd() {
         try {
             if (this.admobRewardedInterKey!!.isEmpty() || this.admobRewardedInterKey!!.isBlank()) {
                 this.onRewardedAdListener!!.onError("AdMob Reward Ads Id is null")
                 return
             }
             if (AdsUtils.isOnline(this.currentActivity!!).not()) {
                 logD("is Offline ")
                 this.onRewardedAdListener!!.isOffline(true)
                 return
             }
             if (this.admobRewardedInterKey == AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID) {
                 logD("Test Ids")
                 if (AdsConstants.testMode.not()) {
                     logD("NULL OR TEST IDS FOUND")
                     this.onRewardedAdListener!!.onError("NULL OR TEST IDS FOUND")
                     return
                 }
             }
             logD("Admob Rewarded Ad Id : $admobRewardedInterKey}")
             RewardedInterstitialAd.load(
                 currentActivity!!,
                 admobRewardedInterKey!!,
                 AdsApplication.getAdRequest(),
                 object : RewardedInterstitialAdLoadCallback() {
                     override fun onAdLoaded(ads: RewardedInterstitialAd) {
                         super.onAdLoaded(ads)
                         rewardedInterstitialAd = ads
                         val options = ServerSideVerificationOptions.Builder()
                             .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                             .build()
                         rewardedInterstitialAd!!.setServerSideVerificationOptions(options)
                         onRewardedAdListener!!.onAdLoaded(AdsConstants.AD_MOB)
                         admobRewardInterstitialAdShow()
                     }

                     override fun onAdFailedToLoad(adError: LoadAdError) {
                         super.onAdFailedToLoad(adError)
                         rewardedInterstitialAd = null
                         onRewardedAdListener!!.onError(adError.message)
                         if (AdsApplication.isAdmobInLimit()) {
                             AdsApplication.applyLimitOnAdmob = true
                         }
                     }
                 })
         } catch (error: Exception) {
             this.onRewardedAdListener!!.onError("showRewardInterstitialAds Error: ${error.localizedMessage}")
         }
     }

     private fun admobRewardInterstitialAdShow() {
         try {
             if (rewardedInterstitialAd != null) {
                 rewardedInterstitialAd!!.show(currentActivity!!) {
                     onRewardedAdListener!!.onRewarded(it)
                 }
                 rewardedInterstitialAd!!.fullScreenContentCallback =
                     object : FullScreenContentCallback() {
                         override fun onAdClicked() {
                             super.onAdClicked()
                             onRewardedAdListener!!.onClicked(AdsConstants.AD_MOB)
                         }

                         override fun onAdDismissedFullScreenContent() {
                             super.onAdDismissedFullScreenContent()
                             rewardedInterstitialAd = null
                             onRewardedAdListener!!.onDismissClick(AdsConstants.AD_MOB)
                         }

                         override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                             super.onAdFailedToShowFullScreenContent(p0)
                             rewardedInterstitialAd = null
                             onRewardedAdListener!!.onError(p0.message)
                         }

                         override fun onAdImpression() {
                             super.onAdImpression()
                         }

                         override fun onAdShowedFullScreenContent() {
                             super.onAdShowedFullScreenContent()
                             onRewardedAdListener!!.onAdLoaded(AdsConstants.AD_MOB)
                         }
                     }
             }
         } catch (error: Exception) {
             this.onRewardedAdListener!!.onError("showRewardInterstitialAds Error: ${error.localizedMessage}")
         }
     }*/
}
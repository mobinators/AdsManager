package com.mobinators.ads.managers.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.rewarded.RewardItem
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.compose.AdsState
import com.mobinators.ads.manager.ui.compose.BannerAdsListener
import com.mobinators.ads.manager.ui.compose.LoadNativeAds
import com.mobinators.ads.manager.ui.compose.LoadNativeState
import com.mobinators.ads.manager.ui.compose.NativeAdsLoaderCallback
import com.mobinators.ads.manager.ui.compose.NativeAdsShowListener
import com.mobinators.ads.manager.ui.compose.ShowBannerAds
import com.mobinators.ads.manager.ui.compose.ShowNativeAds
import com.mobinators.ads.manager.ui.compose.ShowNativeAdsState
import pak.developer.app.managers.extensions.logD

class ComposeAdsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoadNativeAds(
                activity = this@ComposeAdsActivity,
                isPurchased = false,
                listener = object : NativeAdsLoaderCallback {
                    override fun onNativeAdsState(loadState: LoadNativeState) {
                        logD("Native Ads Loaded : State : ${loadState.name}")
                    }
                })
            Scaffold(
                topBar = {
                    TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.DarkGray, titleContentColor = Color.White
                    ), title = {
                        Text(
                            "Ads Manager", maxLines = 1
                        )
                    })
                }, modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Box(modifier = Modifier.padding(it)) {

                    App()
                }
            }
        }
    }

    @Composable
    fun App() {
        val bannerHider = remember { mutableStateOf(false) }
        val nativeHider = remember { mutableStateOf(false) }
        val nativeCustom = remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                bannerHider.value.then {
                    ShowBannerAds(modifier = Modifier.height(50.dp),
                        false,
                        object : BannerAdsListener {
                            override fun onBannerAdsState(adsState: AdsState) {
                                when (adsState) {
                                    AdsState.ADS_OFF -> logD("ADS_OFF")
                                    AdsState.NETWORK_OFF -> logD("NETWORK_OFF")
                                    AdsState.APP_PURCHASED -> logD("APP_PURCHASED")
                                    AdsState.ADS_STRATEGY_WRONG -> logD("ADS_STRATEGY_WRONG")
                                    AdsState.ADS_ID_NULL -> logD("ADS_ID_NULL")
                                    AdsState.TEST_ADS_ID -> logD("TEST_ADS_ID")
                                    AdsState.ADS_LOAD_FAILED -> logD("ADS_LOAD_FAILED")
                                    AdsState.ADS_DISMISS -> logD("ADS_DISMISS")
                                    AdsState.ADS_DISPLAY_FAILED -> logD("ADS_DISPLAY_FAILED")
                                    AdsState.ADS_DISPLAY -> logD("ADS_DISPLAY")
                                    AdsState.ADS_IMPRESS -> logD("ADS_IMPRESS")
                                    AdsState.ADS_LOADED -> logD("ADS_LOADED")
                                    AdsState.ADS_CLICKED -> logD("ADS_CLICKED")
                                    AdsState.ADS_CLOSED -> logD("ADS_CLOSED")
                                    AdsState.ADS_OPENED -> logD("ADS_OPENED")
                                }
                            }
                        })
                }
                nativeHider.value.then {
                    ShowNativeAds(isPurchased = false, listener = object : NativeAdsShowListener {
                        override fun onNativeAdsShowState(showState: ShowNativeAdsState) {
                            logD("Native Ads : ${showState.name}")
                        }
                    }, nativeCustom.value)
                }
            }
            Spacer(
                modifier = Modifier
                    .height(5.dp)
                    .fillMaxWidth()
            )
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                Button(
                    onClick = {
                        bannerHider.value = bannerHider.value.not()
                        nativeHider.value = false
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Banner Ads")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        nativeCustom.value = false
                        bannerHider.value = false
                        nativeHider.value = nativeHider.value.not()
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Native Ads")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        bannerHider.value = false
                        nativeCustom.value = true
                        nativeHider.value = nativeHider.value.not()
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Custom Native Ads")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        MediationAdInterstitial.showInterstitialAds(
                            this@ComposeAdsActivity,
                            false,
                            object : MediationAdInterstitial.AdsShowCallback {
                                override fun onAdsOff() {
                                    logD("Interstitial Ads Off")
                                }

                                override fun onAdsClicked() {
                                    logD("Interstitial Ads Clicked")
                                }

                                override fun onAdsError(errorState: AdsErrorState) {
                                    when (errorState) {
                                        AdsErrorState.NETWORK_OFF -> logD("Interstitial Ads: NETWORK_OFF")
                                        AdsErrorState.APP_PURCHASED -> logD("Interstitial Ads: APP_PURCHASED")
                                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("Interstitial Ads: ADS_STRATEGY_WRONG")
                                        AdsErrorState.ADS_ID_NULL -> logD("Interstitial Ads: ADS_ID_NULL")
                                        AdsErrorState.TEST_ADS_ID -> logD("Interstitial Ads: TEST_ADS_ID")
                                        AdsErrorState.ADS_LOAD_FAILED -> logD("Interstitial Ads: ADS_LOAD_FAILED")
                                        AdsErrorState.ADS_DISMISS -> logD("Interstitial Ads: ADS_DISMISS")
                                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("Interstitial Ads: ADS_DISPLAY_FAILED")
                                        AdsErrorState.ADS_IMPRESS -> logD("Interstitial Ads: ADS_IMPRESS")
                                    }
                                }

                            })
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Interstitial Ads")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        MediationRewardedAd.showRewardAds(
                            this@ComposeAdsActivity,
                            false,
                            object : MediationRewardedAd.ShowRewardedAdsCallback {
                                override fun onAdsOff() {
                                    logD("Reward Ads : onAdsOff")
                                }

                                override fun onRewardEarned(item: Int, type: String) {
                                    logD("Reward Ads : onRewardEarned : $item : $type")
                                }

                                override fun onAdsClicked() {
                                    logD("Reward Ads : onAdsClicked")
                                }

                                override fun onAdsDisplay() {
                                    logD("Reward Ads : onAdsDisplay")
                                }

                                override fun onAdsError(errorState: AdsErrorState) {
                                    logD("Reward Ads : ${errorState.name}")
                                }

                            })
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Reward Ads")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        MediationRewardedInterstitialAd.showRewardedInterstitialAd(
                            this@ComposeAdsActivity,
                            false,
                            object : MediationRewardedInterstitialAd.ShowRewardAdsCallback {
                                override fun onAdsOff() {
                                    logD("Rewarded Interstitial Ads : onAdsOff")
                                }

                                override fun onAdsError(error: String) {
                                    logD("Rewarded Interstitial Ads : onAdsError :$error")
                                }

                                override fun onAdsReward(item: RewardItem) {
                                    logD("Rewarded Interstitial Ads : onAdsReward : Reward Item : $item")
                                }

                                override fun onAdsClicked() {
                                    logD("Rewarded Interstitial Ads : onAdsClicked")
                                }

                                override fun onAdsImpress() {
                                    logD("Rewarded Interstitial Ads : onAdsImpress")
                                }

                                override fun onAdsDismiss(item: RewardItem) {
                                    logD("Rewarded Interstitial Ads : onAdsDismiss : Reward Item : $item")
                                }
                            })
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Rewarded Interstitial Ads")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        MediationOpenAd.showAppOpenAds(
                            this@ComposeAdsActivity,
                            false,
                            object : MediationOpenAd.AdsShowAppOpenCallback {
                                override fun onAdsOff() {
                                    logD("App Open Ads: onAdsOff")
                                }

                                override fun onAdsClicked() {
                                    logD("App Open Ads: onAdsClicked")
                                }

                                override fun onAdsDisplay() {
                                    logD("App Open Ads: onAdsDisplay")
                                }

                                override fun onAdsError(errorState: AdsErrorState) {
                                    when (errorState) {
                                        AdsErrorState.NETWORK_OFF -> logD("App Open Ads: NETWORK_OFF")
                                        AdsErrorState.APP_PURCHASED -> logD("App Open Ads: APP_PURCHASED")
                                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("App Open Ads: ADS_STRATEGY_WRONG")
                                        AdsErrorState.ADS_ID_NULL -> logD("App Open Ads: ADS_ID_NULL")
                                        AdsErrorState.TEST_ADS_ID -> logD("App Open Ads: TEST_ADS_ID")
                                        AdsErrorState.ADS_LOAD_FAILED -> logD("App Open Ads: ADS_LOAD_FAILED")
                                        AdsErrorState.ADS_DISMISS -> logD("App Open Ads: ADS_DISMISS")
                                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("App Open Ads: ADS_DISPLAY_FAILED")
                                        AdsErrorState.ADS_IMPRESS -> logD("App Open Ads: ADS_IMPRESS")
                                    }
                                }
                            })
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "App Open Ads")
                }
            }
        }

    }
}
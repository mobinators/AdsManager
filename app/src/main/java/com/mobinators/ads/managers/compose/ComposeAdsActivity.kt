package com.mobinators.ads.managers.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.rewarded.RewardItem
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AnalyticsManager
import com.mobinators.ads.manager.ui.compose.AdsState
import com.mobinators.ads.manager.ui.compose.BannerAdsListener
import com.mobinators.ads.manager.ui.compose.BottomSheet
import com.mobinators.ads.manager.ui.compose.RateUsDialog
import com.mobinators.ads.manager.ui.compose.ShowBannerAds
import com.mobinators.ads.manager.ui.compose.ShowNativeAds
import com.mobinators.ads.managers.R
import kotlinx.coroutines.launch
import pak.developer.app.managers.extensions.logD

class ComposeAdsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun App() {
        val scope = rememberCoroutineScope()
        val isBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        isBottomSheetVisible.value.then {
            BottomSheet(
                this@ComposeAdsActivity,
                isBottomSheetVisible = isBottomSheetVisible.value,
                sheetState = sheetState,
                panelTitle = "App Exit",
                panelTitleColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelDes = "Your app is exit?",
                panelDesColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelCancelBtnBgColor = Color.LightGray,
                panelCancelTitleColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelExitBtnBgColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelExitTitleColor = Color.White,
                isAdsShow = true,
                onDismiss = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { isBottomSheetVisible.value = false }
                },
                onExit = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { isBottomSheetVisible.value = false }
                    finishAffinity()
                }
            )
        }
        BackHandler {
            isBottomSheetVisible.value = true
        }
        val bannerHider = remember { mutableStateOf(false) }
        val nativeHider = remember { mutableStateOf(false) }
        val nativeCustom = remember { mutableStateOf(false) }

        val rateDialog = remember {
            mutableStateOf(false)
        }
        RateUsDialog(context = this@ComposeAdsActivity, showDialog = rateDialog)
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
                    ShowNativeAds(
                        activity = this@ComposeAdsActivity,
                        isPurchased = false,
                        listener = object : MediationNativeAds.ShowNativeAdsCallback {
                            override fun onAdsSwipe() {
                                AnalyticsManager.getInstance().setAnalyticsEvent(
                                    resources.getString(R.string.app_name),
                                    "NativeAds",
                                    "Ads swipe"
                                )
                                logD("MainActivity onAdsSwipe")
                            }

                            override fun onAdsShowState(adsShowState: AdsShowState) {
                                AnalyticsManager.getInstance().setAnalyticsEvent(
                                    resources.getString(R.string.app_name),
                                    "NativeAds",
                                    adsShowState.name
                                )
                                when (adsShowState) {
                                    AdsShowState.APP_PURCHASED -> logD("MainActivity Native Ads : You have Purchased your app")
                                    AdsShowState.NETWORK_OFF -> logD("MainActivity Native Ads : Internet Off")
                                    AdsShowState.ADS_OFF -> logD("MainActivity Native Ads : Ads Off")
                                    AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads : Ads Strategy wrong")
                                    AdsShowState.ADS_ID_NULL -> logD("MainActivity Native Ads : Ads ID is Null found")
                                    AdsShowState.TEST_ADS_ID -> logD("MainActivity Native Ads : Test Id found in released mode your app")
                                    AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads : Ads load failed")
                                    AdsShowState.ADS_DISMISS -> logD("MainActivity Native Ads : Ads Dismiss")
                                    AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads : Display Ads failed")
                                    AdsShowState.ADS_DISPLAY -> logD("MainActivity Native Ads : Ads Display")
                                    AdsShowState.ADS_IMPRESS -> logD("MainActivity Native Ads : Ads Impress Mode")
                                    AdsShowState.ADS_CLICKED -> logD("MainActivity Native Ads : Ads Clicked")
                                    AdsShowState.ADS_CLOSED -> logD("MainActivity Native Ads : Ads Closed")
                                    AdsShowState.ADS_OPEN -> logD("MainActivity Native Ads : Ads Open")
                                }
                            }

                        },
                        nativeCustom.value
                    )
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
                                override fun onAdsShowState(adsShowState: AdsShowState) {
                                    AnalyticsManager.getInstance().setAnalyticsEvent(
                                        resources.getString(R.string.app_name),
                                        "ShowInterstitialAds",
                                        adsShowState.name
                                    )
                                    when (adsShowState) {
                                        AdsShowState.APP_PURCHASED -> logD("MainActivity  Interstitial Ads : You have Purchased your app")
                                        AdsShowState.NETWORK_OFF -> logD("MainActivity  Interstitial Ads : Internet Off")
                                        AdsShowState.ADS_OFF -> logD("MainActivity Interstitial Ads : Ads Off")
                                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Interstitial Ads : Ads Strategy wrong")
                                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Interstitial Ads : Ads ID is Null found")
                                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Interstitial Ads : Test Id found in released mode your app")
                                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Interstitial Ads : Ads load failed")
                                        AdsShowState.ADS_DISMISS -> logD("MainActivity Interstitial Ads : Ads Dismiss")
                                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Interstitial Ads : Display Ads failed")
                                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Interstitial Ads : Ads Display")
                                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Interstitial Ads : Ads Impress Mode")
                                        AdsShowState.ADS_CLICKED -> logD("MainActivity Interstitial Ads : Ads Clicked")
                                        AdsShowState.ADS_CLOSED -> logD("MainActivity Interstitial Ads : Ads Closed")
                                        AdsShowState.ADS_OPEN -> logD("MainActivity Interstitial Ads : Ads Open")
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
                                override fun onRewardEarned(item: Int, type: String) {
                                    AnalyticsManager.getInstance().setAnalyticsEvent(
                                        resources.getString(R.string.app_name),
                                        "RewardAds",
                                        "Amount : $item  : Type : $type"
                                    )
                                    logD("MainActivity Reward onRewardEarned : Amount : $item  : Type : $type")
                                }

                                override fun onAdsShowState(adsShowState: AdsShowState) {
                                    AnalyticsManager.getInstance().setAnalyticsEvent(
                                        resources.getString(R.string.app_name),
                                        "RewardAds",
                                        adsShowState.name
                                    )
                                    when (adsShowState) {
                                        AdsShowState.APP_PURCHASED -> logD("MainActivity Reward Ads : You have Purchased your app")
                                        AdsShowState.NETWORK_OFF -> logD("MainActivity Reward Ads : Internet Off")
                                        AdsShowState.ADS_OFF -> logD("MainActivity Reward Ads : Ads Off")
                                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Reward Ads : Ads Strategy wrong")
                                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Reward Ads : Ads ID is Null found")
                                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Reward Ads : Test Id found in released mode your app")
                                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Reward Ads : Ads load failed")
                                        AdsShowState.ADS_DISMISS -> logD("MainActivity Reward Ads : Ads Dismiss")
                                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Reward Ads : Display Ads failed")
                                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Reward Ads : Ads Display")
                                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Reward Ads : Ads Impress Mode")
                                        AdsShowState.ADS_CLICKED -> logD("MainActivity Reward Ads : Ads Clicked")
                                        AdsShowState.ADS_CLOSED -> logD("MainActivity Reward Ads : Ads Closed")
                                        AdsShowState.ADS_OPEN -> logD("MainActivity Reward Ads : Ads Open")
                                    }
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
                                override fun onAdsReward(item: RewardItem) {
                                    AnalyticsManager.getInstance().setAnalyticsEvent(
                                        resources.getString(R.string.app_name),
                                        "RewardInterstitialAds",
                                        "Ads RewardItem : $item"
                                    )
                                    logD("MainActivity Reward Interstitial Ads Reward : $item")
                                }
                                override fun onAdsDismiss(item: RewardItem) {
                                    AnalyticsManager.getInstance().setAnalyticsEvent(
                                        resources.getString(R.string.app_name),
                                        "RewardInterstitialAds",
                                        "Ads Dismiss"
                                    )
                                    logD(" MainActivity Reward Interstitial Ads Dismiss : $item")
                                }

                                override fun onAdsShowState(adsShowState: AdsShowState) {
                                    AnalyticsManager.getInstance().setAnalyticsEvent(
                                        resources.getString(R.string.app_name),
                                        "RewardInterstitialAds",
                                        "Ads State : ${adsShowState.name}"
                                    )
                                    when (adsShowState) {
                                        AdsShowState.APP_PURCHASED -> logD("MainActivity RewardInterstitial Ads : You have Purchased your app")
                                        AdsShowState.NETWORK_OFF -> logD("MainActivity RewardInterstitial Ads : Internet Off")
                                        AdsShowState.ADS_OFF -> logD("MainActivity RewardInterstitial Ads : Ads Off")
                                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity RewardInterstitial Ads : Ads Strategy wrong")
                                        AdsShowState.ADS_ID_NULL -> logD("MainActivity RewardInterstitial Ads : Ads ID is Null found")
                                        AdsShowState.TEST_ADS_ID -> logD("MainActivity RewardInterstitial Ads : Test Id found in released mode your app")
                                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity RewardInterstitial Ads : Ads load failed")
                                        AdsShowState.ADS_DISMISS -> logD("MainActivity RewardInterstitial Ads : Ads Dismiss")
                                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity RewardInterstitial Ads : Display Ads failed")
                                        AdsShowState.ADS_DISPLAY -> logD("MainActivity RewardInterstitial Ads : Ads Display")
                                        AdsShowState.ADS_IMPRESS -> logD("MainActivity RewardInterstitial Ads : Ads Impress Mode")
                                        AdsShowState.ADS_CLICKED -> logD("MainActivity RewardInterstitial Ads : Ads Clicked")
                                        AdsShowState.ADS_CLOSED -> logD("MainActivity RewardInterstitial Ads : Ads Closed")
                                        AdsShowState.ADS_OPEN -> logD("MainActivity RewardInterstitial Ads : Ads Open")
                                    }
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
                                override fun onAdsShowState(adsShowState: AdsShowState) {
                                    AnalyticsManager.getInstance().setAnalyticsEvent(
                                        resources.getString(R.string.app_name),
                                        "ShowAppOpenAds",
                                        adsShowState.name
                                    )
                                    when (adsShowState) {
                                        AdsShowState.APP_PURCHASED -> logD("MainActivity  AppOpen Ads : You have Purchased your app")
                                        AdsShowState.NETWORK_OFF -> logD("MainActivity  AppOpen Ads : Internet Off")
                                        AdsShowState.ADS_OFF -> logD("MainActivity AppOpen Ads : Ads Off")
                                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity AppOpen Ads : Ads Strategy wrong")
                                        AdsShowState.ADS_ID_NULL -> logD("MainActivity AppOpen Ads : Ads ID is Null found")
                                        AdsShowState.TEST_ADS_ID -> logD("MainActivity AppOpen Ads : Test Id found in released mode your app")
                                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity AppOpen Ads : Ads load failed")
                                        AdsShowState.ADS_DISMISS -> logD("MainActivity AppOpen Ads : Ads Dismiss")
                                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity AppOpen Ads : Display Ads failed")
                                        AdsShowState.ADS_DISPLAY -> logD("MainActivity AppOpen Ads : Ads Display")
                                        AdsShowState.ADS_IMPRESS -> logD("MainActivity AppOpen Ads : Ads Impress Mode")
                                        AdsShowState.ADS_CLICKED -> logD("MainActivity AppOpen Ads : Ads Clicked")
                                        AdsShowState.ADS_CLOSED -> logD("MainActivity AppOpen Ads : Ads Closed")
                                        AdsShowState.ADS_OPEN -> logD("MainActivity AppOpen Ads : Ads Open")
                                    }
                                }
                            })
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "App Open Ads")
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        rateDialog.value = true
                    }, modifier = Modifier
                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Rate US Dialog")
                }
            }
        }
    }
}
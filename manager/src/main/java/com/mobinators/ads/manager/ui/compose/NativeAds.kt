package com.mobinators.ads.manager.ui.compose

import android.app.Activity
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds

@Composable
fun ShowNativeAds(
    activity: Activity,
    isPurchased: Boolean,
    listener: MediationNativeAds.ShowNativeAdsCallback,
    isCustomView: Boolean = false
) {
    AndroidView(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth(),
        factory = { context ->
            LinearLayout(context).apply {
                MediationNativeAds.showNativeAds(
                    activity,
                    isPurchased,
                    this,
                    object : MediationNativeAds.ShowNativeAdsCallback {
                        override fun onAdsSwipe() {
                            listener.onAdsSwipe()
                        }

                        override fun onAdsShowState(adsShowState: AdsShowState) {
                            listener.onAdsShowState(adsShowState)
                        }
                    },
                    isCustomView
                )
            }
        })
}

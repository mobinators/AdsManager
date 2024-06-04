package com.mobinators.ads.manager.ui.compose

import android.app.Activity
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
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
                        override fun onAdsOff() {
                            listener.onAdsOff()
                        }

                        override fun onAdsOpen() {
                            listener.onAdsOpen()
                        }

                        override fun onAdsClicked() {
                            listener.onAdsClicked()
                        }

                        override fun onAdsClosed() {
                            listener.onAdsClosed()
                        }

                        override fun onAdsSwipe() {
                            listener.onAdsSwipe()
                        }

                        override fun onAdsError(errorState: AdsErrorState) {
                            listener.onAdsError(errorState = errorState)
                        }
                    },
                    isCustomView
                )
            }
        })
}

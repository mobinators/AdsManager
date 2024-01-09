package com.mobinators.ads.managers

import android.os.Bundle
import android.view.ViewTreeObserver
import com.facebook.ads.AdView
import com.mobinators.ads.manager.ui.commons.collapsiblebanner.CollapseBannerState
import com.mobinators.ads.manager.ui.commons.collapsiblebanner.MediationCollapsibleBanner
import com.mobinators.ads.manager.ui.commons.listener.BannerAdListener
import com.mobinators.ads.managers.databinding.ActivityCollapseBannerBinding
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.ui.commons.base.BaseActivity

class CollapseBannerActivity : BaseActivity<ActivityCollapseBannerBinding>() {
    override fun getActivityView() = ActivityCollapseBannerBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.bannerContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.bannerContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                collapseBannerAds()
            }
        })
    }

    private fun collapseBannerAds() {
        MediationCollapsibleBanner.showCollapsibleBannerAds(
            this,
            false,
            binding.bannerContainer,
            CollapseBannerState.BOTTOM,
            object : BannerAdListener {
                override fun onLoaded(adType: Int) {
                    logD("MainActivity onLoaded : $adType")
                }

                override fun onAdClicked(adType: Int) {
                    logD("MainActivity onAdClicked : $adType")
                }

                override fun onError(error: String) {
                    logD("MainActivity onError Error : $error")
                }

                override fun onFacebookAdCreated(facebookBanner: AdView) {
                    logD("MainActivity onFacebookAdCreated : $facebookBanner")
                }

                override fun isEnableAds(isAds: Boolean) {
                    logD("MainActivity isEnableAds : $isAds")
                }

                override fun isOffline(offline: Boolean) {
                    logD("Ads is Offline : $offline")
                }
            })
    }


}
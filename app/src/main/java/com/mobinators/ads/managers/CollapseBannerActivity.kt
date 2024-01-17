package com.mobinators.ads.managers

import android.os.Bundle
import android.view.ViewTreeObserver
import com.facebook.ads.AdView
import com.mobinators.ads.manager.ui.commons.collapsiblebanner.CollapseBannerState
import com.mobinators.ads.manager.ui.commons.collapsiblebanner.MediationCollapsibleBanner
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState.*
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
            object : MediationCollapsibleBanner.BannerAdListener {
                override fun onAdsOff() {
                    logD("CollapseBannerActivity Ads Of")
                }

                override fun onAdsLoaded() {
                    logD("CollapseBannerActivity Ads Loaded")
                }

                override fun onAdsError(adsErrorState: AdsErrorState) {
                    when (adsErrorState) {
                        NETWORK_OFF -> logD("CollapseBannerActivity Ads Network Off")
                        APP_PURCHASED -> logD("CollapseBannerActivity Ads You have purchased")
                        ADS_STRATEGY_WRONG -> logD("CollapseBannerActivity Ads Strategy Wrong")
                        ADS_ID_NULL -> logD("CollapseBannerActivity Ads Null Id")
                        TEST_ADS_ID -> logD("CollapseBannerActivity Ads Test ID")
                        ADS_LOAD_FAILED -> logD("CollapseBannerActivity Ads Load Failed")
                        ADS_DISMISS -> logD("CollapseBannerActivity Ads Dismiss")
                        ADS_DISPLAY_FAILED -> logD("CollapseBannerActivity Ads Display Failed")
                        ADS_IMPRESS -> logD("CollapseBannerActivity Ads Impress")
                    }
                }

            })
    }


}
package com.mobinators.ads.manager.ui.fragments


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.databinding.FragmentExitBottomSheetBinding
import com.mobinators.ads.manager.extensions.setBackgroundColors
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.base.BaseBottomSheet
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState.*
import com.mobinators.ads.manager.ui.commons.listener.PanelListener
import com.mobinators.ads.manager.ui.commons.models.PanelModel
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logD


class ExitBottomSheetFragment : BaseBottomSheet<FragmentExitBottomSheetBinding>(),
    View.OnClickListener {
    private var listener: PanelListener? = null
    private var panelModel: PanelModel? = null
    override fun getBottomView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentExitBottomSheetBinding.inflate(inflater, container, false)

    @SuppressLint("ResourceAsColor", "ResourceType")
    override fun initView() {
        binding.cancelButton.setOnClickListener(this)
        binding.exitButton.setOnClickListener(this)
        panelModel?.let {
            binding.cancelButton.setBackgroundColors(
                requireContext(),
                it.cancelBgColor ?: R.color.menuColor
            )
            binding.cancelButton.text = it.cancelButtonText ?: resources.getString(R.string.cancel)
            binding.cancelButton.setTextColor(
                resources.getColor(
                    it.cancelButtonTitleColor ?: R.color.menuColor
                )
            )
            binding.exitButton.setBackgroundColors(
                requireContext(),
                it.exitButtonBgColor ?: R.color.menuColor
            )
            binding.exitButton.text = it.exitButtonText ?: resources.getString(R.string.exit_)
            binding.exitButton.setTextColor(
                resources.getColor(
                    it.exitButtonTextColor ?: R.color.menuColor
                )
            )
            binding.exitText.text = it.desc ?: resources.getString(R.string.sure_you_want_to_exit)
            binding.exitAppText.text = it.title ?: resources.getString(R.string.exit_app)
            binding.exitAppText.setTextColor(resources.getColor(it.titleColor ?: R.color.menuColor))
            binding.exitText.setTextColor(resources.getColor(it.descColor ?: R.color.menuColor))
            it.panelBackgroundColor?.let { colorId ->
                binding.rootLayout.setCardBackgroundColor(resources.getColor(colorId))
            } ?: binding.rootLayout.setCardBackgroundColor(Color.WHITE)
        }
        try {
            panelModel?.isAdsShow?.then {
                logD("Ads is not enable:${panelModel?.isAdsShow}")
                BannerAdMediation.showBannerAds(
                    requireActivity(),
                    false,
                    binding.exitBannerFrame,
                    object : BannerAdMediation.BannerAdListener {
                        override fun onAdsOff() {
                            binding.exitBannerLayout.gone()
                            logD("Exit Panel onAdsOff")
                        }

                        override fun onAdsLoaded() {
                            logD("Exit Panel onAdsLoaded")
                        }

                        override fun onAdsClicked() {
                            logD("Exit Panel onAdsClicked")
                        }

                        override fun onAdsClosed() {
                            logD("Exit Panel onAdsClosed")
                        }

                        override fun onAdsOpened() {
                            logD("Exit Panel onAdsOpened")
                        }

                        override fun onAdsError(adsErrorState: AdsErrorState) {
                            when (adsErrorState) {
                                NETWORK_OFF -> {
                                    binding.exitBannerLayout.gone()
                                    logD("Exit Panel Network Off")
                                }

                                APP_PURCHASED -> {
                                    binding.exitBannerLayout.gone()
                                    logD("Exit Panel Purchased")
                                }

                                ADS_STRATEGY_WRONG -> {
                                    binding.exitBannerLayout.gone()
                                    logD("Exit Panel Strategy Wrong")
                                }

                                ADS_ID_NULL -> {
                                    binding.exitBannerLayout.gone()
                                    logD("Exit Panel Ads id Null")
                                }

                                TEST_ADS_ID -> {
                                    binding.exitBannerLayout.gone()
                                    logD("Exit Panel Test Ads ID")
                                }

                                ADS_LOAD_FAILED -> {
                                    binding.exitBannerLayout.gone()
                                    logD("Exit Panel Load Failed")
                                }

                                ADS_DISMISS -> {
                                    logD("Exit Panel Dismiss")
                                }

                                ADS_DISPLAY_FAILED -> {
                                    binding.exitBannerLayout.gone()
                                    logD("Exit Panel Display Failed")
                                }

                                ADS_IMPRESS -> {
                                    binding.adsLoadingText.gone()
                                    logD("Exit Panel Impress")
                                }
                            }
                        }
                    })
            } ?: run {
                logD("Ads is not enable : ${panelModel?.isAdsShow}")
                binding.exitBannerLayout.gone()
            }
        } catch (error: Exception) {
            logD("Exit Panel Ads Error : ${error.localizedMessage}")
        }
    }

    override fun onClick(itemId: View?) {
        when (itemId!!.id) {
            binding.cancelButton.id -> {
                listener!!.onCancel()
                dismiss()
            }

            binding.exitButton.id -> {
                listener!!.onExit()
                dismiss()
                requireActivity().finishAffinity()
            }
        }
    }

    fun setListener(listener: PanelListener) {
        this.listener = listener
    }

    fun setPanelModel(model: PanelModel?) {
        this.panelModel = model
    }
}
package com.mobinators.ads.manager.ui.fragments


import  android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.databinding.FragmentExitBottomSheetBinding
import com.mobinators.ads.manager.extensions.setBackgroundColors
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.base.BaseBottomSheet
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.ADS_DISMISS
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.ADS_DISPLAY_FAILED
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.ADS_ID_NULL
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.ADS_IMPRESS
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.ADS_LOAD_FAILED
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.ADS_STRATEGY_WRONG
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.APP_PURCHASED
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.NETWORK_OFF
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState.TEST_ADS_ID
import com.mobinators.ads.manager.ui.commons.listener.PanelListener
import com.mobinators.ads.manager.ui.commons.models.PanelModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.gone
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.preferenceUtils
import pak.developer.app.managers.extensions.showToast
import pak.developer.app.managers.extensions.visible

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
        binding.cancelButton.setOnClickListener {
            listener?.onCancel()
            dismiss()
        }
        binding.exitButton.setOnClickListener {
            listener?.onExit()
            dismiss()
            requireActivity().finishAffinity()
        }
        panelModel?.let {
            binding.cancelButton.setBackgroundColors(
                requireContext(),
                it.cancelBgColor ?: R.color.menuColor
            )
            binding.cancelButton.text = it.cancelButtonText ?: resources.getString(R.string.cancel)
            binding.cancelButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    it.cancelButtonTitleColor ?: R.color.menuColor
                )
            )

            binding.exitButton.setBackgroundColors(
                requireContext(),
                it.exitButtonBgColor ?: R.color.menuColor
            )
            binding.exitButton.text = it.exitButtonText ?: resources.getString(R.string.exit_)
            binding.exitButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    it.exitButtonTextColor ?: R.color.menuColor
                )
            )

            binding.exitText.text = it.desc ?: resources.getString(R.string.sure_you_want_to_exit)
            binding.exitAppText.text = it.title ?: resources.getString(R.string.exit_app)
            binding.exitAppText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    it.titleColor ?: R.color.menuColor
                )
            )
            binding.exitText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    it.descColor ?: R.color.menuColor
                )
            )

            it.panelBackgroundColor?.let { colorId ->
                binding.rootLayout.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        colorId
                    )
                )
            } ?: binding.rootLayout.setCardBackgroundColor(Color.WHITE)

            binding.rateBtn.setBackgroundColors(
                requireContext(),
                it.rateButtonBgColor ?: R.color.rateColor
            )
            binding.rateDescText.setTextColor(
                ContextCompat.getColor(requireContext(), it.rateDescTextColor ?: R.color.black)
            )
            binding.rateTitle.setTextColor(
                ContextCompat.getColor(requireContext(), it.rateTittleTextColor ?: R.color.black)
            )

            binding.rateBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    it.rateButtonColor ?: R.color.white
                )
            )

            binding.rateCountText.setTextColor(
                ContextCompat.getColor(requireContext(), it.rateTextColor ?: R.color.rateColor)
            )
        }
        try {
            var dialogCounter: Int =
                preferenceUtils.getIntegerValue(AdsConstants.RATE_US_DIALOG_COUNT_KEY)
            if (dialogCounter >= (AdsApplication.getAdsModel()?.isRateUsDialog ?: 5)) {
                preferenceUtils.setIntegerValue(AdsConstants.RATE_US_DIALOG_COUNT_KEY, 0)
                binding.exitText.gone()
                binding.exitBannerLayout.gone()
                binding.rateUsLayout.visible()
            } else {
                dialogCounter++
                preferenceUtils.setIntegerValue(
                    AdsConstants.RATE_US_DIALOG_COUNT_KEY,
                    dialogCounter
                )
                binding.rateUsLayout.gone()
                binding.exitText.visible()
                binding.exitBannerLayout.visible()
                panelModel?.isAdsShow?.then {
                    logD("Ads is not enable:${panelModel?.isAdsShow}")
                    BannerAdMediation.showBannerAds(
                        requireActivity(),
                        false,
                        binding.exitBannerFrame,
                        object : BannerAdMediation.BannerAdListener {
                            override fun onAdsLoaded() {
                                logD("Exit Panel onAdsLoaded")
                            }

                            override fun onAdsState(adsShowState: AdsShowState) {
                                when (adsShowState) {
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

                                    AdsShowState.ADS_OFF -> logD("Exit Panel onAdsOff")
                                    AdsShowState.ADS_DISPLAY -> {
                                        binding.adsLoadingText.gone()
                                        logD("Exit Panel ads Display ")

                                    }

                                    AdsShowState.ADS_CLICKED -> logD("Exit Panel onAdsClicked")
                                    AdsShowState.ADS_CLOSED -> logD("Exit Panel Ads Closed")
                                    AdsShowState.ADS_OPEN -> logD("Exit Panel Ads Open")
                                }
                            }

                        })
                } ?: run {
                    logD("Ads is not enable : ${panelModel?.isAdsShow}")
                    binding.exitBannerLayout.gone()
                }
            }
        } catch (error: Exception) {
            logD("Exit Panel Ads Error : ${error.localizedMessage}")
        }
        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            logD("Rate US Bar : $ratingBar : $rating  : $fromUser")
            checkStore()
            dismiss()
        }
    }


    private fun checkStore() {
        when (AdsConstants.selectedStore) {
            AdsConstants.GOOGLE_PLAY_STORE -> AdsUtils.openPlayStore(
                requireContext(),
                requireContext().packageName
            )

            AdsConstants.AMAZON_APP_STORE -> AdsUtils.openAmazonStore(
                requireContext(),
                requireContext().packageName
            )

            AdsConstants.HUAWEI_APP_GALLERY -> {}
            else -> showToast(requireActivity(), "wrong selected store")
        }
    }

    override fun onClick(itemId: View?) {
        when (itemId!!.id) {
            binding.cancelButton.id -> {
                listener?.onCancel()
                dismiss()
            }

            binding.exitButton.id -> {
                listener?.onExit()
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
package com.mobinators.ads.manager.ui.fragments


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.ads.AdView
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.databinding.FragmentExitBottomSheetBinding
import com.mobinators.ads.manager.extensions.setBackgroundColors
import com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
import com.mobinators.ads.manager.ui.commons.base.BaseBottomSheet
import com.mobinators.ads.manager.ui.commons.listener.BannerAdListener
import com.mobinators.ads.manager.ui.commons.listener.PanelListener
import com.mobinators.ads.manager.ui.commons.models.PanelModel
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
            binding.cancelButton.setBackgroundColors(requireContext(), it.cancelBgColor ?: R.color.menuColor)
            binding.cancelButton.text = it.cancelButtonText ?: resources.getString(R.string.cancel)
            binding.cancelButton.setTextColor(resources.getColor(it.cancelButtonTitleColor?: R.color.menuColor))
            binding.exitButton.setBackgroundColors(requireContext(), it.exitButtonBgColor ?: R.color.menuColor)
            binding.exitButton.text = it.exitButtonText ?: resources.getString(R.string.exit_)
            binding.exitButton.setTextColor(resources.getColor(it.exitButtonTextColor ?: R.color.menuColor))
            binding.exitText.text = it.desc ?: resources.getString(R.string.sure_you_want_to_exit)
            binding.exitAppText.text = it.title ?: resources.getString(R.string.exit_app)
            binding.exitAppText.setTextColor(resources.getColor(it.titleColor ?: R.color.menuColor))
            binding.exitText.setTextColor(resources.getColor(it.descColor ?: R.color.menuColor))
            it.panelBackgroundColor?.let {colorId->
                binding.rootLayout.setCardBackgroundColor(resources.getColor(colorId))
            }?: binding.rootLayout.setCardBackgroundColor(Color.WHITE)
        }

        BannerAdMediation.showBannerAds(
            requireActivity(),
            false,
            binding.exitBannerFrame,
            object : BannerAdListener {
                override fun onLoaded(adType: Int) {
                    logD("Exit Panel onLoaded : $adType")
                }

                override fun onAdClicked(adType: Int) {
                    logD("Exit Panel onAdClicked : $adType")
                }

                override fun onError(error: String) {
                    logD("Exit Panel onError Error : $error")
                }

                override fun onFacebookAdCreated(facebookBanner: AdView) {
                    logD("Exit Panel onFacebookAdCreated : $facebookBanner")
                }

                override fun isEnableAds(isAds: Boolean) {
                    logD("Exit Panel isEnableAds : $isAds")
                }

                override fun isOffline(offline: Boolean) {
                    logD("Exit Panel Ads is Offline : $offline")
                }
            })
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
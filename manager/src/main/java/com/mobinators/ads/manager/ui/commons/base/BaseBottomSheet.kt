package com.mobinators.ads.manager.ui.commons.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobinators.ads.manager.R

abstract class BaseBottomSheet<viewBinding : ViewBinding> : BottomSheetDialogFragment() {
    protected lateinit var binding: viewBinding
    override fun getTheme() = R.style.CustomBottomSheetDialogTheme
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getBottomView(inflater, container, savedInstanceState)
        return binding.root
    }

    abstract fun getBottomView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): viewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    abstract fun initView()
}
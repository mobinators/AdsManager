package com.mobinators.ads.manager.ui.commons.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.databinding.RateUsDialogBinding
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD

class RateUsDialog private constructor() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: RateUsDialog? = null
        fun getInstance(): RateUsDialog {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = RateUsDialog()
                    }
                }
            }
            return instance!!
        }
    }

    private var alertDialog: AlertDialog? = null
    private var desTextColor: Int? = null
    private var rateButtonBgColor: Int? = null
    private var rateButtonTextColor: Int? = null

    fun showDialog(activity: Activity) {
        val dialogBinding = RateUsDialogBinding.inflate(LayoutInflater.from(activity))
        alertDialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()
        alertDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog!!.show()
        dialogBinding.rateBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, this.rateButtonBgColor?:R.color.rateColor))
        dialogBinding.rateBtn.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(activity,this.rateButtonTextColor?:R.color.white)))
        dialogBinding.decText.setTextColor(this.desTextColor?:R.color.rateColor)
        dialogBinding.rateBtn.setOnClickListener {
            dismissDialog()
            AdsUtils.openPlayStore(activity, activity.packageName)
        }
        dialogBinding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            logD("Rate US Bar : $ratingBar : $rating  : $fromUser")
            dismissDialog()
            AdsUtils.openPlayStore(activity, activity.packageName)
        }
    }

    fun dismissDialog() {
        alertDialog?.dismiss()
    }

    fun setRateButtonBg(rateColor: Int) {
        this.rateButtonBgColor = rateColor
    }

    fun setRateBtnTextColor(rateColor: Int) {
        this.rateButtonTextColor = rateColor
    }

    fun setTextColor(rateColor: Int) {
        this.desTextColor = rateColor
    }
}
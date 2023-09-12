package com.mobinators.ads.manager.ui.commons.views.dialog

import android.app.Activity
import android.app.ProgressDialog
import pak.developer.app.managers.extensions.logException

class ProgressDialogUtils constructor(private var activity: Activity) {
    private var progressDialog: ProgressDialog? = null

    init {
        progressDialog = ProgressDialog(activity)
    }

    fun showDialog(title: String, desc: String) {
        try {
            progressDialog!!.setTitle(title)
            progressDialog!!.setMessage(desc)
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        } catch (error: Exception) {
            logException("Dialog Box Error : ${error.localizedMessage}")
        }
    }


    fun isShowDialog() {
        try {
            if (progressDialog!!.isShowing) {
                dialogDismiss()
            }
        } catch (error: Exception) {
            logException("Dialog Box Error : ${error.localizedMessage}")
        }
    }

    fun dialogDismiss() {
        try {
            progressDialog!!.dismiss()
        } catch (error: Exception) {
            logException("Dialog Box Error : ${error.localizedMessage}")
        }
    }
}
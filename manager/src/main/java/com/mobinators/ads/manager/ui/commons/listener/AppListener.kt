package com.mobinators.ads.manager.ui.commons.listener

interface AppListener {
    fun onDownload()
    fun onInstalled()
    fun onCancel()
    fun onFailure(error: Exception)
    fun onNoUpdateAvailable()
    fun onStore(updateState: AppUpdateState)
}

enum class AppUpdateState {
    WRONG_STORE, AMAZON_STORE, HUAWEI_STORE
}

interface AppRateUsCallback {
    fun onRateUsState(rateState: RateUsState)
}

enum class RateUsState {
    RATE_US_COMPLETED, RATE_US_CANCEL, RATE_US_FAILED, RATE_US_ERROR,WRONG_STORE, AMAZON_STORE, HUAWEI_STORE
}
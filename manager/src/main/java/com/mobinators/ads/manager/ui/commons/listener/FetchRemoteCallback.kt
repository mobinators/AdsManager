package com.mobinators.ads.manager.ui.commons.listener

interface FetchRemoteCallback {
    fun onFetchValuesSuccess()
    fun onFetchValuesFailed()
    fun onUpdateSuccess(appId: String, maxAppId: String)
}
package com.mobinators.ads.manager.ui.commons.listener

import com.mobinators.ads.manager.ui.commons.models.AdsModel

interface FetchRemoteCallback {
    fun onFetchValuesSuccess()
    fun onFetchValuesFailed()
    fun onUpdateSuccess(adsModel: AdsModel)
}
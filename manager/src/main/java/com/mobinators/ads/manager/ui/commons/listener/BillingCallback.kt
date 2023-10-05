package com.mobinators.ads.manager.ui.commons.listener

import com.mobinators.ads.manager.ui.commons.models.InAppPurchasedModel
import com.mobinators.ads.manager.ui.commons.utils.ConnectionState

interface BillingCallback {
    fun onSubscribe(msg: String)
    fun onAlreadySubscribe(msg:String)
    fun onFeatureNotSupported()
    fun onBillingError(error: String)
    fun onSubscriptionPending(msg: String)
    fun onUnspecifiedState(msg:String)
    fun onProductDetail(productDetail: InAppPurchasedModel)
    fun isOffline(offline: Boolean)
    fun onServiceDisConnected()
    fun onBillingFinished(state: ConnectionState)
}
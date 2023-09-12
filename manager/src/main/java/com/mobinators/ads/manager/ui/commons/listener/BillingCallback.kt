package com.mobinators.ads.manager.ui.commons.listener

import com.mobinators.ads.manager.ui.commons.models.InAppPurchasedModel

interface BillingCallback {
    fun onSubscribe()
    fun onAlreadySubscribe()
    fun onFeatureNotSupported()
    fun onBillingError(error: String)
    fun onSubscriptionPending()
    fun onUnspecifiedState()
    fun onProductDetail(productDetail: InAppPurchasedModel)
    fun isOffline(offline: Boolean)
}
package com.mobinators.ads.manager.ui.commons.listener

import com.mobinators.ads.manager.ui.commons.states.SubscriptionState

interface PurchaseCallBack {
    fun onPurchaseState(state: SubscriptionState)
}
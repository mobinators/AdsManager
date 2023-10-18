package com.mobinators.ads.manager.ui.commons.states

import com.mobinators.ads.manager.ui.commons.utils.AppPurchaseUtils

sealed class SubscriptionState {
    object AlReadySubscribe : SubscriptionState()
    object PendingSubscribe : SubscriptionState()
    object UnspecifiedState : SubscriptionState()
    data class Subscribed(val isSuccess: Boolean) : SubscriptionState()
    data class SubscriptionFailure(val error: String) : SubscriptionState()
    data class SubscriptionFinished(val isPremium: Boolean) : SubscriptionState()
    data class ProductDetail(val model: AppPurchaseUtils.PurchaseModel) : SubscriptionState()
}
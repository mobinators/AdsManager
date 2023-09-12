package com.mobinators.ads.manager.ui.commons.utils

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.mobinators.ads.manager.ui.commons.listener.BillingCallback
import com.mobinators.ads.manager.ui.commons.models.InAppPurchasedModel
import pak.developer.app.managers.extensions.logD
import java.io.IOException

class InAppPurchaseUtils constructor(
    private var activity: Activity,
    private var productId: String,
    private var base64Key: String,
    private var listener: BillingCallback
) {
    private var billingClient: BillingClient? = null
    private var isSuccess = false

    suspend fun startConnection() {
        billingClient = BillingClient.newBuilder(activity).setListener(purchasesUpdatedListener)
            .enablePendingPurchases().build()
        getBillingPrice()
    }

    suspend fun inPurchase() {
        if (AdsUtils.isOnline(activity).not()) {
            logD("is Offline ")
            listener.isOffline(true)
            return
        }
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                listener.onBillingError("Error : onBillingServiceDisconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS).build()
                )
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                billingClient!!.queryProductDetailsAsync(params.build()) { _, productDetailList ->
                    run {
                        for (productDetails in productDetailList) {
                            val offerToken = productDetails.subscriptionOfferDetails?.get(0)
                                ?.offerToken
                            val productDetailsParamsList = listOf(
                                offerToken?.let {
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails).setOfferToken(it)
                                        .build()
                                }
                            )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()
                            val result =
                                billingClient!!.launchBillingFlow(activity, billingFlowParams)
                            logD("onBillingSetupFinished: billing result = $result")
                        }
                    }
                }
            }
        })
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                listener.onAlreadySubscribe()
                isSuccess = true
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                listener.onFeatureNotSupported()
            } else {
                listener.onBillingError("Error : ${billingResult.debugMessage} ")
            }
        }

    private fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val consumeResponseListener = ConsumeResponseListener { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                logD("handlePurchase: handle purchase if")
            }
        }
        billingClient!!.consumeAsync(consumeParams, consumeResponseListener)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                listener.onBillingError("Error :Invalid Purchase ")
                return
            }
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(
                    acknowledgePurchaseParams,
                    acknowledgePurchaseResponseListener
                )
                listener.onSubscribe()
                isSuccess = true
            } else {
                listener.onAlreadySubscribe()
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            listener.onSubscriptionPending()
        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            listener.onUnspecifiedState()
        }
    }

    private var acknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                listener.onSubscribe()
                isSuccess = true
            }
        }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val security = InAppSecurity()
            security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            listener.onBillingError("Error : verifyValidSignature : ${e.localizedMessage} ")
            false
        }
    }

    private suspend fun getBillingPrice() {
        if (AdsUtils.isOnline(activity).not()) {
            logD("is Offline ")
            listener.isOffline(true)
            return
        }
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                listener.onBillingError("Error : onBillingServiceDisconnected ")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
                val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
                billingClient!!.queryProductDetailsAsync(params.build()) { _, productDetailList ->
                    for (productDetails in productDetailList) {
                        val response =
                            productDetails.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(
                                0
                            )?.formattedPrice
                        listener.onProductDetail(productDetail = InAppPurchasedModel().apply {
                            this.productName = productDetails.name
                            this.price = response
                            this.desc = productDetails.description

                        })
                    }
                }
            }
        })
    }

    fun disConnect() {
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }
}
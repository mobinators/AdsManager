package com.mobinators.ads.manager.ui.commons.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
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
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import com.mobinators.ads.manager.ui.commons.listener.BillingCallback
import com.mobinators.ads.manager.ui.commons.models.InAppPurchasedModel
import java.io.IOException
import java.util.concurrent.Executors

class InAppPurchaseUtils constructor(
    private var activity: Activity,
    private var productId: String,
    private var base64Key: String,
    private var billingCallback: BillingCallback
) {
    private var billingClient: BillingClient? = null
    private var isSuccess = false
    private var purchaseModel: InAppPurchasedModel? = null

    fun startConnection() {
        billingClient = BillingClient.newBuilder(activity).setListener(purchasesUpdatedListener)
            .enablePendingPurchases().build()
    }

    fun startSubSubscription() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder().setProductList(
                        ImmutableList.of(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductType(BillingClient.ProductType.SUBS)
                                .setProductId(productId).build()
                        )
                    ).build()
                billingClient!!.queryProductDetailsAsync(
                    queryProductDetailsParams
                ) { _, list ->
                    for (productDetails in list) {
                        val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
                        val productDetailsParamList: ImmutableList<BillingFlowParams.ProductDetailsParams> =
                            ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails).setOfferToken(offerToken!!)
                                    .build()
                            )
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamList).build()
                        billingClient!!.launchBillingFlow(activity, billingFlowParams)
                    }
                }
            }
        })
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handlePurchase(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val listener = ConsumeResponseListener { billingResult: BillingResult, _: String? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("TAG", "handlePurchase")
            }
        }
        billingClient!!.consumeAsync(consumeParams, listener)
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    billingCallback.onBillingError("Error : invalid Purchase")
                    return
                }
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken).build()
                    billingClient!!.acknowledgePurchase(
                        acknowledgePurchaseParams, acknowledgePurchaseResponseListener
                    )
                    billingCallback.onSubscribe("Subscribed")
                    isSuccess = true
                } else {
                    billingCallback.onAlreadySubscribe("Already Subscribed")
                }
                ConnectionState.premium = true
                ConnectionState.locked = false
                billingCallback.onBillingFinished(ConnectionState)
            }

            Purchase.PurchaseState.PENDING -> {
                billingCallback.onSubscriptionPending("Subscription Pending")
            }

            Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                billingCallback.onUnspecifiedState("UNSPECIFIED_STATE")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private var acknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                billingCallback.onSubscribe("Subscribed")
                isSuccess = true
                ConnectionState.premium = true
                ConnectionState.locked = false
                billingCallback.onBillingFinished(ConnectionState)
            }
        }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            InAppSecurity.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }


    fun getSubscriptionInfo() {
        purchaseModel = InAppPurchasedModel()
        billingClient!!.startConnection(object : BillingClientStateListener {
            @SuppressLint("CheckResult", "SetTextI18n")
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val executorService = Executors.newSingleThreadExecutor()
                    executorService.execute {
                        val queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder().setProductList(
                                    ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(productId)
                                            .setProductType(BillingClient.ProductType.SUBS).build()
                                    )

                                ).build()
                        billingClient!!.queryProductDetailsAsync(
                            queryProductDetailsParams
                        ) { _, productDetailsList ->
                            for (productDetails in productDetailsList) {
                                val offerToken =
                                    productDetails.subscriptionOfferDetails?.get(0)?.offerToken

                                ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(offerToken!!).build()
                                )
                                purchaseModel!!.productName = productDetails.name
                                purchaseModel!!.desc = productDetails.description
                                val formattedPrice =
                                    productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0].formattedPrice
                                val billingPeriod =
                                    productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0].billingPeriod
                                val recurrenceMode =
                                    productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0].recurrenceMode
                                val bp: String = billingPeriod
                                val n: String = billingPeriod.substring(1, 2)
                                val duration: String = billingPeriod.substring(2, 3)
                                if (recurrenceMode == 2) {
                                    purchaseModel!!.duration = when (duration) {
                                        "M" -> {
                                            " For $n Month "
                                        }

                                        "Y" -> {
                                            " For $n Year "
                                        }

                                        "W" -> {
                                            " For $n Week "
                                        }

                                        "D" -> {
                                            " For $n Days "
                                        }

                                        else -> {
                                            ""
                                        }
                                    }
                                } else {
                                    purchaseModel!!.duration = when (bp) {
                                        "P1M" -> {
                                            "/Monthly"
                                        }

                                        "P6M" -> {
                                            "/Every 6 Month"
                                        }

                                        "P1Y" -> {
                                            "/Yearly"
                                        }

                                        "P1W" -> {
                                            "/Weekly"
                                        }

                                        "P3W" -> {
                                            "/Every /3 Week"
                                        }

                                        else -> {
                                            ""
                                        }
                                    }
                                }
                                purchaseModel!!.phases =
                                    "$formattedPrice ${purchaseModel!!.duration}"
                                for (i in 0..productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList.size) {
                                    if (i > 0) {
                                        val period =
                                            productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[i].billingPeriod
                                        val price =
                                            productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[i].formattedPrice
                                        purchaseModel!!.duration = when (period) {
                                            "P1M" -> {
                                                "/Monthly"
                                            }

                                            "P6M" -> {
                                                "/Every 6 Month"
                                            }

                                            "P1Y" -> {
                                                "/Yearly"
                                            }

                                            "P1W" -> {
                                                "/Weekly"
                                            }

                                            "P3W" -> {
                                                "/Every /3 Week"
                                            }

                                            else -> {
                                                ""
                                            }
                                        }
                                        purchaseModel!!.phases += """
                                            
                                            $price${purchaseModel!!.duration}
                                            """.trimIndent()
                                    }
                                }
                            }
                        }
                    }
                    billingCallback.onProductDetail(productDetail = purchaseModel!!)
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    fun disConnected() {
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }
}
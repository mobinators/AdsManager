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
import com.mobinators.ads.manager.ui.commons.listener.PurchaseCallBack
import com.mobinators.ads.manager.ui.commons.states.SubscriptionState
import java.io.IOException
import java.util.concurrent.Executors


@SuppressLint("StaticFieldLeak")
object AppPurchaseUtils {
    data class PurchaseModel(
        var productName: String? = null,
        var price: String? = null,
        var desc: String? = null,
        var duration: String? = null,
        var phases: String? = null
    )

    private var isConnection: Boolean = false
    private var base64Key: String? = null
    private var purchaseListener: PurchaseCallBack? = null
    private var activity: Activity? = null
    private var billingClient: BillingClient? = null
    private var purchaseModel: PurchaseModel? = null

    fun startConnection(activity: Activity, base64Key: String, listener: PurchaseCallBack) {
        isConnection = true
        this.activity = activity
        this.base64Key = base64Key
        this.purchaseListener = listener
        try {
            billingClient = BillingClient.newBuilder(activity).setListener(purchasesUpdatedListener)
                .enablePendingPurchases().build()
        } catch (error: Exception) {
            this.purchaseListener!!.onPurchaseState(
                state = SubscriptionState.SubscriptionFailure(
                    error = error.localizedMessage ?: "Unexpected Error"
                )
            )
        }
    }

    fun startSubscription(productId: String) {
        try {
            if (this.isConnection.not()) {

                this.purchaseListener!!.onPurchaseState(
                    state = SubscriptionState.SubscriptionFailure(
                        error = "startSubscription Calling first startConnection Method"
                    )
                )
                return
            }

            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    purchaseListener!!.onPurchaseState(
                        state = SubscriptionState.SubscriptionFailure(
                            error = "onBillingServiceDisconnected calling "
                        )
                    )
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder().setProductList(
                            ImmutableList.of(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .setProductId(productId)
                                    .build()
                            )
                        ).build()
                    billingClient!!.queryProductDetailsAsync(
                        queryProductDetailsParams
                    ) { _, list ->
                        for (productDetails in list) {
                            val offerToken = productDetails.subscriptionOfferDetails
                                ?.get(0)?.offerToken
                            val productDetailsParamList: ImmutableList<BillingFlowParams.ProductDetailsParams> =
                                ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(offerToken!!)
                                        .build()
                                )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamList)
                                .build()
                            billingClient!!.launchBillingFlow(activity!!, billingFlowParams)
                        }
                    }
                }
            })
        } catch (error: Exception) {
            this.purchaseListener!!.onPurchaseState(
                state = SubscriptionState.SubscriptionFailure(
                    error = error.localizedMessage ?: "Unexpected Error"
                )
            )
        }
    }

    fun getSubscriptionInfo(productId: String) {
        try {
            if (this.isConnection.not()) {

                this.purchaseListener!!.onPurchaseState(
                    state = SubscriptionState.SubscriptionFailure(
                        error = "getSubscriptionInfo Calling first startConnection Method"
                    )
                )
                return
            }
            purchaseModel = PurchaseModel()
            billingClient!!.startConnection(object : BillingClientStateListener {
                @SuppressLint("CheckResult", "SetTextI18n")
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val executorService = Executors.newSingleThreadExecutor()
                        executorService.execute {
                            val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                                .setProductList(
                                    ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(productId)
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build()
                                    )
                                )
                                .build()
                            billingClient!!.queryProductDetailsAsync(
                                queryProductDetailsParams
                            ) { _, productDetailsList ->
                                for (productDetails in productDetailsList) {
                                    val offerToken = productDetails.subscriptionOfferDetails
                                        ?.get(0)?.offerToken

                                    ImmutableList.of(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .setOfferToken(offerToken!!)
                                            .build()
                                    )
                                    purchaseModel!!.productName = productDetails.name
                                    purchaseModel!!.desc = productDetails.description
                                    val formattedPrice =
                                        productDetails.subscriptionOfferDetails!![0].pricingPhases
                                            .pricingPhaseList[0].formattedPrice
                                    val billingPeriod =
                                        productDetails.subscriptionOfferDetails!![0].pricingPhases
                                            .pricingPhaseList[0].billingPeriod
                                    val recurrenceMode =
                                        productDetails.subscriptionOfferDetails!![0].pricingPhases
                                            .pricingPhaseList[0].recurrenceMode
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
                                                productDetails.subscriptionOfferDetails!![0].pricingPhases
                                                    .pricingPhaseList[i].billingPeriod
                                            val price =
                                                productDetails.subscriptionOfferDetails!![0].pricingPhases
                                                    .pricingPhaseList[i].formattedPrice
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

                                            Log.d(
                                                "Tag",
                                                "onBillingSetupFinished: Price : ${purchaseModel!!.phases} "
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        purchaseListener!!.onPurchaseState(
                            state = SubscriptionState.ProductDetail(
                                purchaseModel!!
                            )
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    purchaseListener!!.onPurchaseState(
                        state = SubscriptionState.SubscriptionFailure(
                            error = "onBillingServiceDisconnected calling "
                        )
                    )
                }
            })

        } catch (error: Exception) {
            this.purchaseListener!!.onPurchaseState(
                state = SubscriptionState.SubscriptionFailure(
                    error = error.localizedMessage ?: "Unexpected Error"
                )
            )
        }
    }

    fun disConnected() {
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }


    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
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
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener { billingResult: BillingResult, _: String? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("TAG", "handlePurchase")
            }
        }
        billingClient!!.consumeAsync(consumeParams, listener)
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    purchaseListener!!.onPurchaseState(
                        state = SubscriptionState.SubscriptionFailure(
                            "Error : invalid Purchase"
                        )
                    )
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
                    purchaseListener!!.onPurchaseState(
                        state = SubscriptionState.Subscribed(
                            isSuccess = true
                        )
                    )
                } else {
                    purchaseListener!!.onPurchaseState(state = SubscriptionState.AlReadySubscribe)
                }
                purchaseListener!!.onPurchaseState(
                    state = SubscriptionState.SubscriptionFinished(
                        isPremium = true
                    )
                )
            }

            Purchase.PurchaseState.PENDING -> {
                purchaseListener!!.onPurchaseState(state = SubscriptionState.PendingSubscribe)
            }

            Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                purchaseListener!!.onPurchaseState(state = SubscriptionState.UnspecifiedState)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private var acknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchaseListener!!.onPurchaseState(state = SubscriptionState.Subscribed(true))
                purchaseListener!!.onPurchaseState(
                    state = SubscriptionState.SubscriptionFinished(
                        true
                    )
                )
            }
        }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            InAppSecurity.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }
}
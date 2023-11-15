package com.mobinators.ads.manager.ui.commons.utils


import android.annotation.SuppressLint
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
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pak.developer.app.managers.extensions.logD
import java.io.IOException
import java.util.concurrent.Executors


@SuppressLint("StaticFieldLeak")
object AppPurchaseUtils {
    private var isSuccess = false
    private var billingClient: BillingClient? = null
    private var activity: Activity? = null
    private var base64Key: String? = null
    private var listener: BillingCallback? = null
    fun initConnection(activity: Activity, base64Key: String, listener: BillingCallback) {
        this.listener = listener
        this.activity = activity
        this.base64Key = base64Key
        if (AdsUtils.isOnline(activity).not()) {
            this.listener!!.onRequiredNetwork()
            return
        }
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

    }

    @SuppressLint("SetTextI18n")
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            logD("Already Subscribed")
            isSuccess = true
            this.listener!!.onSubscribe(isSuccess, true, false)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
            logD("FEATURE_NOT_SUPPORTED")
            this.listener!!.onError("FEATURE_NOT_SUPPORTED")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            logD("BILLING_UNAVAILABLE")
            this.listener!!.onError("BILLING_UNAVAILABLE")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            logD("USER_CANCELED")
            this.listener!!.onError("USER_CANCELED")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
            logD("DEVELOPER_ERROR")
            this.listener!!.onError("DEVELOPER_ERROR")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
            logD("ITEM_UNAVAILABLE")
            this.listener!!.onError("ITEM_UNAVAILABLE")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.NETWORK_ERROR) {
            logD("NETWORK_ERROR")
            this.listener!!.onError("NETWORK_ERROR")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            logD("SERVICE_DISCONNECTED")
            this.listener!!.onError("SERVICE_DISCONNECTED")
        } else {
            this.listener!!.onError("Error : ${billingResult.debugMessage}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener { billingResult: BillingResult, _: String? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                logD("handlePurchase")
            }
        }
        billingClient!!.consumeAsync(consumeParams, listener)
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    this.listener!!.onError("Error : invalid Purchase")
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
                    logD("Subscribed")
                    isSuccess = true
                } else {
                    logD("Already Subscribed")
                }
                this.listener!!.onSubscribe(isSuccess, true, false)
            }

            Purchase.PurchaseState.PENDING -> {
                logD("Subscription Pending")
                this.listener!!.onError("Subscription Pending")
            }

            Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                logD("UNSPECIFIED_STATE")
                this.listener!!.onError("UNSPECIFIED_STATE")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private var acknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                isSuccess = true
                this.listener!!.onSubscribe(isSuccess, true, false)
            }
        }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            InAppSecurity.verifyPurchase(this.base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

    suspend fun getSubscriptionInfo(
        productId: String,
        price: (String?) -> Unit,
    ) {
        var phases: String?
        var des: String? = null
        var dur: String? = null

        if (AdsUtils.isOnline(this.activity!!).not()) {
            this.listener!!.onRequiredNetwork()
            return
        }
        withContext(Dispatchers.IO) {

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
                                    des = productDetails.description
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
                                        when (duration) {
                                            "M" -> {
                                                dur = " For $n Month "
                                            }

                                            "Y" -> {
                                                dur = " For $n Year "
                                            }

                                            "W" -> {
                                                dur = " For $n Week "
                                            }

                                            "D" -> {
                                                dur = " For $n Days "
                                            }
                                        }
                                    } else {
                                        when (bp) {
                                            "P1M" -> {
                                                dur = "/Monthly"
                                            }

                                            "P6M" -> {
                                                dur = "/Every 6 Month"
                                            }

                                            "P1Y" -> {
                                                dur = "/Yearly"
                                            }

                                            "P1W" -> {
                                                dur = "/Weekly"
                                            }

                                            "P3W" -> {
                                                dur = "/Every /3 Week"
                                            }
                                        }
                                    }
                                    phases = "$formattedPrice $dur"
                                    for (i in 0..productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList.size) {
                                        if (i > 0) {
                                            val period =
                                                productDetails.subscriptionOfferDetails!![0].pricingPhases
                                                    .pricingPhaseList[i].billingPeriod
                                            val price =
                                                productDetails.subscriptionOfferDetails!![0].pricingPhases
                                                    .pricingPhaseList[i].formattedPrice
                                            when (period) {
                                                "P1M" -> {
                                                    dur = "/Monthly"
                                                }

                                                "P6M" -> {
                                                    dur = "/Every 6 Month"
                                                }

                                                "P1Y" -> {
                                                    dur = "/Yearly"
                                                }

                                                "P1W" -> {
                                                    dur = "/Weekly"
                                                }

                                                "P3W" -> {
                                                    dur = "/Every /3 Week"
                                                }
                                            }
                                            phases += """
                                            
                                            $price$dur
                                            """.trimIndent()
                                        }
                                    }
                                }
                            }
                        }

                        activity!!.runOnUiThread {
                            try {
                                Thread.sleep(1000)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            price(des?.replace("\n\n", ""))
                        }

                    }
                }

                override fun onBillingServiceDisconnected() {}
            })
        }
    }


    suspend fun onSubscription(productId: String) {
        if (AdsUtils.isOnline(this.activity!!).not()) {
            this.listener!!.onRequiredNetwork()
            return
        }
        withContext(Dispatchers.IO) {


            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {}
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
                            billingClient!!.launchBillingFlow(
                                activity!!,
                                billingFlowParams
                            )
                        }
                    }
                }
            })
        }
    }

    fun clientDestroy() {
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }

    interface BillingCallback {
        fun onRequiredNetwork()
        fun onSubscribe(isSuccess: Boolean, isPremium: Boolean, isLocked: Boolean)
        fun onError(error: String)
    }
}
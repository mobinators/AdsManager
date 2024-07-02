package com.mobinators.ads.manager.extensions

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.bumptech.glide.Glide
import com.facebook.ads.AdSettings
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.mobinators.ads.manager.ui.commons.listener.AppListener
import com.mobinators.ads.manager.ui.commons.listener.AppRateUsCallback
import com.mobinators.ads.manager.ui.commons.listener.AppUpdateState
import com.mobinators.ads.manager.ui.commons.listener.PanelListener
import com.mobinators.ads.manager.ui.commons.listener.RateUsState
import com.mobinators.ads.manager.ui.commons.models.AdsModel
import com.mobinators.ads.manager.ui.commons.models.PanelModel
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.fragments.ExitBottomSheetFragment
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException
import pak.developer.app.managers.extensions.preferenceUtils
import java.util.Collections
import java.util.concurrent.Executors


private var appUpdateManager: AppUpdateManager? = null
fun Application.updateManifest(adsModel: AdsModel, onConfig: (String) -> Unit = {}) {
    try {
        adsModel.admobAppID?.let {
            if (AdsConstants.testMode.not()) {
                if (it == AdsConstants.TEST_ADMOB_APP_ID) {
                    logD("found test App id")
                    return
                }
            }
            val applicationInfo: ApplicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle: Bundle = applicationInfo.metaData
            val appKey: String? = bundle.getString(AdsConstants.ADMOB_META_KEY)
            logD("Name Found ADMOB : $appKey")
            applicationInfo.metaData.putString(AdsConstants.ADMOB_META_KEY, it)
            val apiKey: String? = bundle.getString(AdsConstants.ADMOB_META_KEY)
            logD("Name Found ADMOB : $apiKey")
            if (adsModel.strategy.toInt() == AdsConstants.AD_MOB) {
                onConfig("AdMOb")
            }
        }
        adsModel.maxAppId?.let {
            try {
                if (adsModel.strategy.toInt() == AdsConstants.MAX_MEDIATION) {
                    logD("Max Mediation STRATEGY : ${adsModel.strategy}")
                    if (it.isEmpty() || it.isBlank()) {
                        logD("Null AppLoving Sdk key")
                        return
                    }
                    initMaxMediation(it) {
                        onConfig("AppLoving")
                    }
                } else {
                    logD("Ads Strategy is wrong")
                }
            } catch (error: Exception) {
                logException("Failed to load AppLoving Meta-Data, SDK key Error : ${error.localizedMessage}")
            }
        }
    } catch (error: PackageManager.NameNotFoundException) {
        logException("Failed to load meta-data, NameNotFound: ${error.localizedMessage}")
    } catch (error: NullPointerException) {
        logException("Failed to load meta-data, NullPointer: ${error.localizedMessage}")
    }
}

private fun Application.initMaxMediation(sdkKey: String, onConfig: () -> Unit = {}) {
    try {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            // Make sure to set the mediation provider value to "max" to ensure proper functionality
            AdSettings.setDataProcessingOptions(arrayOf<String>())
            val initConfigBuilder = AppLovinSdkInitializationConfiguration.builder(sdkKey, this)
            initConfigBuilder.mediationProvider = AppLovinMediationProvider.MAX
            val currentGaid = AdvertisingIdClient.getAdvertisingIdInfo(this).id
            if (AdsConstants.testMode) {
                logD("Test Ads GAID ID  : $currentGaid  : ${AdsConstants.testMode}")
                if (currentGaid != null) {
                    initConfigBuilder.testDeviceAdvertisingIds =
                        Collections.singletonList(currentGaid)
                }
            }
            AppLovinSdk.getInstance(this).mediationProvider = AppLovinMediationProvider.MAX
            AppLovinSdk.getInstance(this).initialize(initConfigBuilder.build()) { sdkConfig ->
                logD("Apploving Sdk : $sdkConfig")
                onConfig()
            }
            executor.shutdown()
        }
    } catch (error: Exception) {
        logException("Max Mediation Error : ${error.message}")
    }
}

inline fun <reified T> T.convertToJson(
    context: Context, obj: T, isPreference: Boolean, key: String
): String? {
    val gson = Gson()
    return try {
        return if (isPreference) {
            if (TextUtils.isEmpty(key).not()) {
                logD("Json obj: ${gson.toJson(obj)}")
                val result = context.preferenceUtils.setStringValue(key, gson.toJson(obj))
                return if (result) {
                    "Save Value"
                } else {
                    "Save Value Failed"
                }
            } else {
                "your value is not save in preference because key is empty"
            }
        } else {
            gson.toJson(obj)
        }
    } catch (error: Exception) {
        logException("convertToJson Error : ${error.localizedMessage}")
        "convertToJson Error : ${error.localizedMessage}"
    }
}

inline fun <reified T> Application.convertToModel(
    json: String? = null, isPreference: Boolean = false, key: String? = null
): T? {
    return try {
        val dataJson = if (isPreference) {
            key?.let {
                preferenceUtils.getStringValue(it)
            }
        } else {
            json
        }
        Gson().fromJson(dataJson, T::class.java)
    } catch (error: Exception) {
        logException("convertToModel Error : ${error.localizedMessage}")
        null
    }
}

fun ImageView.createThumbNail(context: Context, imageUri: String) = apply {
    Glide.with(context).load(Uri.parse(imageUri)).into(this)
}

fun View.setBackgroundColors(context: Context, @ColorRes colorId: Int) = apply {
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorId))
}

fun Activity.appSharing(appName: String, appPackageName: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_VIEW)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
        var shareMessage = "\nLet me recommend you this application\n\n"
        shareMessage =
            """${shareMessage}https://play.google.com/store/apps/details?id=$appPackageName""".trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Share App"))
    } catch (error: Exception) {
        logException("AppSharing Error: ${error.localizedMessage}")
    }
}

fun Activity.appRateUs(listener: AppRateUsCallback) {
    when (AdsConstants.selectedStore) {
        AdsConstants.GOOGLE_PLAY_STORE -> {
            val manager = ReviewManagerFactory.create(this)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val flow = manager.launchReviewFlow(this, task.result)
                    flow.addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.isComplete) {
                                listener.onRateUsState(rateState = RateUsState.RATE_US_COMPLETED)
                                logD("appRateUs: Result : ${it.result} : isComplete :${it.isComplete}")
                            } else {
                                listener.onRateUsState(rateState = RateUsState.RATE_US_CANCEL)
                                logD("appRateUs: Result : ${it.result} :  cancel : ${it.isCanceled}")
                            }
                        } else {
                            listener.onRateUsState(rateState = RateUsState.RATE_US_ERROR)
                            logD("appRateUs: Error : ${it.exception?.message} : isComplete :${it.isComplete}  :  cancel : ${it.isCanceled}")
                        }
                    }.addOnCanceledListener {
                        listener.onRateUsState(rateState = RateUsState.RATE_US_CANCEL)
                        logD("appRateUs: cancel listener ")
                    }.addOnFailureListener {
                        listener.onRateUsState(rateState = RateUsState.RATE_US_FAILED)
                        logD("appRateUs: failure listener ")
                    }
                } else {
                    listener.onRateUsState(rateState = RateUsState.RATE_US_ERROR)
                    logException("appRateUp Error : ${task.exception?.localizedMessage} ")
                }
            }
        }

        AdsConstants.AMAZON_APP_STORE -> listener.onRateUsState(rateState = RateUsState.AMAZON_STORE)
        AdsConstants.HUAWEI_APP_GALLERY -> listener.onRateUsState(rateState = RateUsState.HUAWEI_STORE)
        else -> listener.onRateUsState(rateState = RateUsState.WRONG_STORE)
    }
}

fun Activity.appUpdate(listener: AppListener, requestCode: Int) {
    when (AdsConstants.selectedStore) {
        AdsConstants.GOOGLE_PLAY_STORE -> {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            appUpdateManager!!.registerListener(object : InstallStateUpdatedListener {
                override fun onStateUpdate(state: InstallState) {
                    if (state.installStatus() == InstallStatus.DOWNLOADED) {
                        listener.onDownload()

                    } else if (state.installStatus() == InstallStatus.INSTALLED) {
                        listener.onInstalled()
                        appUpdateManager!!.unregisterListener(this)
                    } else if (state.installStatus() == InstallStatus.CANCELED) {
                        listener.onCancel()
                    }
                }
            })
            appUpdateManager!!.appUpdateInfo.addOnSuccessListener {
                if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(
                        AppUpdateType.FLEXIBLE
                    )
                ) {
                    appUpdateManager!!.startUpdateFlowForResult(
                        it,
                        AppUpdateType.FLEXIBLE,
                        this,
                        requestCode
                    )
                } else {
                    listener.onNoUpdateAvailable()
                }
            }.addOnFailureListener {
                listener.onFailure(error = it)
            }.addOnCanceledListener {
                listener.onCancel()
            }
        }

        AdsConstants.AMAZON_APP_STORE -> listener.onStore(updateState = AppUpdateState.AMAZON_STORE)
        AdsConstants.HUAWEI_APP_GALLERY -> listener.onStore(updateState = AppUpdateState.HUAWEI_STORE)
        else -> listener.onStore(updateState = AppUpdateState.WRONG_STORE)
    }

}


fun Activity.exitPanel(
    fragmentManager: FragmentManager,
    listener: PanelListener,
    model: PanelModel? = null
) {
    val exitFragment = ExitBottomSheetFragment()
    exitFragment.setPanelModel(model = model ?: PanelModel())
    exitFragment.setListener(listener = listener)
    exitFragment.show(fragmentManager, exitFragment.tag)
}

inline fun <reified T> sdk30AndUp(onSdk30: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return onSdk30()
    } else null
}

inline infix fun <T> Boolean.then(param: () -> T): T? = if (this) param() else null
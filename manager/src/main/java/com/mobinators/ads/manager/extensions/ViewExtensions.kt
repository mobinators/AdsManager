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
import com.bumptech.glide.Glide
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
import com.mobinators.ads.manager.ui.commons.listener.PanelListener
import com.mobinators.ads.manager.ui.commons.models.PanelModel
import com.mobinators.ads.manager.ui.fragments.ExitBottomSheetFragment
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException
import pak.developer.app.managers.extensions.preferenceUtils


private var appUpdateManager: AppUpdateManager? = null
fun Application.updateManifest(appId: String, maxAppId: String) {
    try {
        val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val bundle: Bundle = applicationInfo.metaData
        val appKey: String? = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
        logD("Name Found ADMOB : $appKey")
        applicationInfo.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", appId)
        val apiKey: String? = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
        logD("Name Found ADMOB : $apiKey")
        // Applovin Sdk Key
        val maxAppKey: String? = bundle.getString("applovin.sdk.key")
        logD("Name Found APPLOVIN : $maxAppKey")
        applicationInfo.metaData.putString("applovin.sdk.key", maxAppId)
        val maxAiKey: String? = bundle.getString("applovin.sdk.key")
        logD("Name Found APPLOVIN : $maxAiKey")
    } catch (error: PackageManager.NameNotFoundException) {
        logException("Failed to load meta-data, NameNotFound: ${error.localizedMessage}")
    } catch (error: NullPointerException) {
        logException("Failed to load meta-data, NullPointer: ${error.localizedMessage}")
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

fun Activity.appRateUs() {
    val manager = ReviewManagerFactory.create(this)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val flow = manager.launchReviewFlow(this, task.result)
            flow.addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.isComplete) {
                        logD("appRateUs: Result : ${it.result} : isComplete :${it.isComplete}")
                    } else {
                        logD("appRateUs: Result : ${it.result} :  cancel : ${it.isCanceled}")
                    }
                } else {
                    logD("appRateUs: Error : ${it.exception?.message} : isComplete :${it.isComplete}  :  cancel : ${it.isCanceled}")
                }
            }.addOnCanceledListener {
                logD("appRateUs: cancel listener ")
            }.addOnFailureListener {
                logD("appRateUs: failure listener ")
            }
        } else {
            logException("appRateUp Error : ${task.exception?.localizedMessage} ")
        }
    }
}

fun Activity.appUpdate(listener: AppListener, requestCode: Int) {
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

package com.mobinators.ads.manager.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

class NotifyService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logD("Remote Message : ${remoteMessage.data}  : App Running : ${AdsUtils.isAppRunning(this)}")
        when (remoteMessage.data[AdsConstants.APP_UPDATE_KEY]) {
            AdsConstants.APP_UPDATE -> {
                try {
                    AdsUtils.isExistApp(this, packageName).then {
                        val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                        val updateVersion = remoteMessage.data[AdsConstants.APP_UPDATE_VERSION_KEY]
                        if ((updateVersion?.toIntOrNull() ?: 0) > pInfo.versionCode) {
                            logD("Application Update Required : ${remoteMessage.data}")
                            remoteMessage.notification?.let {
                                it.body?.let {
                                    sendNotification(remoteMessage)
                                }
                            } ?: run {
                                logD("Notification null")
                            }
                        } else {
                            logD("Already Update Application")
                        }
                    } ?: run {
                        logD("Application is not install")
                    }
                } catch (error: Exception) {
                    logException("NotifyService Error : ${error.localizedMessage}")
                }
            }

            AdsConstants.APP_RATE_US_DIALOG -> {
                remoteMessage.notification?.let {
                    it.body?.let {
                        sendNotification(remoteMessage)
                    }
                } ?: run {
                    logD("Notification null")
                }
            }

            else -> {
                logD("Wrong Key value")
            }
        }
    }


    override fun onNewToken(token: String) {
        logD("Refreshed token: $token")
        sendRegistrationToServer(token)
    }


    private fun sendRegistrationToServer(token: String?) {
        logD("sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            AdsUtils.openPlayStore(packageName),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_id)
        val pattern = longArrayOf(0, 1000, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = ""
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.setVibrationPattern(pattern)
            notificationChannel.enableVibration(true)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = mNotificationManager.getNotificationChannel(channelId)
            channel.canBypassDnd()
        }
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat
                .Builder(this, channelId)
                .setColor(ContextCompat.getColor(this, R.color.lightGray))
                .setContentTitle(
                    when (remoteMessage.data[AdsConstants.APP_UPDATE_KEY]) {
                        AdsConstants.APP_UPDATE -> "App Update"
                        AdsConstants.APP_RATE_US_DIALOG -> "App Rate Us"
                        else -> "App Update"
                    }
                )
                .setContentText(remoteMessage.notification?.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.notification)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        when (remoteMessage.data[AdsConstants.APP_UPDATE_KEY]) {
                            AdsConstants.APP_UPDATE -> "App Update"
                            AdsConstants.APP_RATE_US_DIALOG -> "App Rate Us"
                            else -> "App Update"
                        }
                    )
                )
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)

        mNotificationManager.notify(1000, notificationBuilder.build())
    }

}
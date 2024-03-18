package com.mobinators.ads.manager.services


import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobinators.ads.manager.GeneralWorker
import pak.developer.app.managers.extensions.logD
class GeneralService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        fcmListener?.onMessageReceive(remoteMessage)
        logD("From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            logD("Message data payload: ${remoteMessage.data}")
            if (isLongRunningJob()) {
                scheduleJob()
            } else {
                handleNow()
            }
        }
    }

    private fun isLongRunningJob() = true
    override fun onNewToken(token: String) {
        logD("Refreshed token: $token")
        sendRegistrationToServer(token)
        fcmListener?.onToken(token)
    }

    private fun scheduleJob() {
        val work = OneTimeWorkRequest.Builder(GeneralWorker::class.java).build()
        WorkManager.getInstance(this).beginWith(work).enqueue()
    }

    private fun handleNow() {
        logD("Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        logD("sendRegistrationTokenToServer($token)")
    }
    companion object {
        private var fcmListener: FCMListener? = null
        fun setListener(listener: FCMListener) {
            this.fcmListener = listener
        }
    }

    interface FCMListener {
        fun onMessageReceive(remoteMessage: RemoteMessage)
        fun onToken(token: String)
    }
}
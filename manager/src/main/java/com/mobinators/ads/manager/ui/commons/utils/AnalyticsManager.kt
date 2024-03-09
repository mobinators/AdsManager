package com.mobinators.ads.manager.ui.commons.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

class AnalyticsManager private constructor() {
    companion object {
        private var instance: AnalyticsManager? = null
        fun getInstance(): AnalyticsManager {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AnalyticsManager()
                    }
                }
            }
            return instance!!
        }
    }

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun setAnalytics(analytics: FirebaseAnalytics) {
        this.firebaseAnalytics = analytics
    }

    fun getAnalytics(): FirebaseAnalytics? = this.firebaseAnalytics
    fun setAnalyticsEvent(eventKey: String, key: String, value: String) {
        try {
            if (eventKey.isEmpty() || key.isEmpty() || value.isEmpty()) {
                logD("Firebase Analytics event key or key or value is empty")
            } else {
                this.firebaseAnalytics?.let {
                    it.logEvent(eventKey+key, Bundle().apply {
                        putString(key, value)
                    })
                } ?: logD("calling setAnalytics method first")
            }
        } catch (error: Exception) {
            logException("Firebase Analytics Event Error : ${error.localizedMessage}")
        }
    }

    fun setAnalyticsUserProperty(key: String, value: String) {
        try {
            if (key.isEmpty() || value.isEmpty()) {
                logD("Firebase Analytics user property key or value is empty")
            } else {
                this.firebaseAnalytics?.setUserProperty(key, value) ?: logD("calling setAnalytics method first")
            }
        } catch (error: Exception) {
            logException("Firebase Analytics User Property error: ${error.localizedMessage} ")
        }
    }

    fun setUserId(userId:String){
        try {
            if (userId.isEmpty()){
                logD("Firebase Analytics user id id is empty")
            }else{
                this.firebaseAnalytics?.setUserId(userId) ?:logD("calling setAnalytics method first")
            }
        }catch (error:Exception){
            logException("Firebase Analytics User Id Error: ${error.localizedMessage}")
        }
    }

    fun setReport(){
        try {
            
        }catch (error: Exception){
            logException("Firebase Analytics Report Error : ${error.localizedMessage}")
        }
    }
}
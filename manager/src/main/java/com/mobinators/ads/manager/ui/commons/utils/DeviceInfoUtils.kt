package com.mobinators.ads.manager.ui.commons.utils


import android.os.Build
import com.mobinators.ads.manager.ui.commons.models.DeviceModel

object DeviceInfoUtils {
    fun getDeviceInfo(): DeviceModel {
        return DeviceModel(
            deviceModel = Build.MODEL,
            deviceBrand = Build.MANUFACTURER,
            deviceName = Build.DEVICE,
            versionCode = Build.VERSION.RELEASE
        )
    }
}
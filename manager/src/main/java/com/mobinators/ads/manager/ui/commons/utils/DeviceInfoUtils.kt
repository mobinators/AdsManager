package com.mobinators.ads.manager.ui.commons.utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.mobinators.ads.manager.ui.commons.models.DeviceModel
import java.net.Inet4Address

object DeviceInfoUtils {
    fun getDeviceInfo(): DeviceModel {
        return DeviceModel(
            deviceModel = Build.MODEL,
            deviceBrand = Build.MANUFACTURER,
            deviceName = Build.DEVICE,
            versionCode = Build.VERSION.RELEASE
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getLocalIpAddress(context: Context, callback: (String?) -> Unit) {
        val connectivityManager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        val request = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val linkProperties = connectivityManager.getLinkProperties(network)
                linkProperties?.linkAddresses?.forEach { linkAddress ->
                    val address = linkAddress.address
                    if (address is Inet4Address) {
                        callback(address.hostAddress)
                        return@forEach
                    }
                }
            }
        }
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }


}
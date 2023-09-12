package com.mobinators.ads.manager.ui.commons.listener

import android.widget.ImageView

interface ImageProvider {
    fun onProviderImage(imageView: ImageView, imageUrl: String)
}
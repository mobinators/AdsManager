package com.mobinators.ads.manager.ui.commons.models

import android.graphics.drawable.Drawable
import com.google.android.gms.ads.MediaContent


data class NativeBannerModel(
    var sponsorLabel: String? = null,
    var title: String? = null,
    var body: String? = null,
    var adCollection: String? = null,
    var logoUrl: String? = null,
    var imageIcon: Drawable? = null,
    var mediaView: MediaContent? = null
)
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe

-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn android.media.LoudnessCodecController
-dontwarn com.mobinators.ads.manager.applications.AdsApplication
-dontwarn com.mobinators.ads.manager.extensions.ViewExtensionsKt
-dontwarn com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation$BannerAdListener
-dontwarn com.mobinators.ads.manager.ui.commons.banner.BannerAdMediation
-dontwarn com.mobinators.ads.manager.ui.commons.collapsiblebanner.CollapseBannerState
-dontwarn com.mobinators.ads.manager.ui.commons.collapsiblebanner.MediationCollapsibleBanner$BannerAdListener
-dontwarn com.mobinators.ads.manager.ui.commons.collapsiblebanner.MediationCollapsibleBanner
-dontwarn com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial$AdsShowCallback
-dontwarn com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
-dontwarn com.mobinators.ads.manager.ui.commons.listener.AppListener
-dontwarn com.mobinators.ads.manager.ui.commons.listener.AppRateUsCallback
-dontwarn com.mobinators.ads.manager.ui.commons.listener.FetchRemoteCallback
-dontwarn com.mobinators.ads.manager.ui.commons.listener.PanelListener
-dontwarn com.mobinators.ads.manager.ui.commons.models.DeviceModel
-dontwarn com.mobinators.ads.manager.ui.commons.models.PanelModel
-dontwarn com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds$ShowNativeAdsCallback
-dontwarn com.mobinators.ads.manager.ui.commons.nativead.MediationNativeAds
-dontwarn com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd$AdsShowAppOpenCallback
-dontwarn com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
-dontwarn com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd$RewardLoadCallback
-dontwarn com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd$ShowRewardedAdsCallback
-dontwarn com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
-dontwarn com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd$ShowRewardAdsCallback
-dontwarn com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
-dontwarn com.mobinators.ads.manager.ui.commons.utils.AdsUtils
-dontwarn com.mobinators.ads.manager.ui.commons.utils.AnalyticsManager$Companion
-dontwarn com.mobinators.ads.manager.ui.commons.utils.AnalyticsManager
-dontwarn com.mobinators.ads.manager.ui.commons.utils.AppPurchaseUtils$BillingCallback
-dontwarn com.mobinators.ads.manager.ui.commons.utils.AppPurchaseUtils
-dontwarn com.mobinators.ads.manager.ui.commons.utils.DeviceInfoUtils
-dontwarn com.mobinators.ads.manager.ui.compose.BannerAdsKt
-dontwarn com.mobinators.ads.manager.ui.compose.BannerAdsListener
-dontwarn com.mobinators.ads.manager.ui.compose.BottomSheetKt
-dontwarn com.mobinators.ads.manager.ui.compose.NativeAdsKt
-dontwarn com.mobinators.ads.manager.ui.compose.RateUsDialogKt
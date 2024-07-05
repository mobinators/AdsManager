# AdsManager

-> add Project level gradle

```project level gradle
      allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
			 maven { url "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea" }
            maven { url 'https://artifacts.applovin.com/android' }
		}
	}
```

-> add module level gradle

```add module lvel gradle
  implementation 'com.github.mobinators:AdsManager:1.2.5'
```

-> add Firebase classpath in Project level gradle

```add Project level gradle

       classpath 'com.google.gms:google-services:4.3.15'
       classpath 'com.google.firebase:firebase-crashlytics-gradle:2.9.9'
```

-> add dependency in Module level gradle

```add Module level gradle
   plugins {
    id 'com.google.gms.google-services'
    id 'com.google.firebase.crashlytics'
    }
    
     defaultConfig {
        ...
        multiDexEnabled true
    }
    
    
    
    Dependency 
    
    implementation platform('com.google.firebase:firebase-bom:32.2.3')
    implementation 'com.google.firebase:firebase-analytics-ktx:21.3.0'
    implementation 'com.google.firebase:firebase-config-ktx:21.4.1'
    implementation 'com.google.firebase:firebase-crashlytics-ktx:18.4.1'
    
    implementation 'androidx.multidex:multidex:2.0.1'    
```

-> Ad this line in Manifest file

```
    <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="@string/app_ads_id" />
       
        <meta-data
            android:name="com.google.android.gms.ads.flag.NATIVE_AD_DEBUGGER_ENABLED"
            android:value="true" />
            
             
```

-> Ad this line String File

```
     <string name="app_ads_id">ca-app-pub-3940256099942544~3347511713</string>  provide origin App id for show original ads
   
```

-> Ads Strategy

``` 

    0-> ads is off
    1-> Admob Mediation enable and change the value of ADMOB_MEDIATION_KEY is true
    2-> Admob Ads enable
    3-> Max Mediation enable

    if you want enable App Open ads so change the value of ADMOB_OPEN_AD_ENABLE is true

    remote_config_maanger_json_file is a folder which contain the remote config json file because any
    one create new Firebase project with remote config so import it file after that imported it then
    change the value of ads key

```

-> Ad this line in Application then register it in Manifest File

```

     MultiDex.install(this)
     
      Google Play Store-> 1 or AdsConstants.GOOGLE_PLAY_STORE
      Amazon App Store-> 2 or AdsConstants.AMAZON_APP_STORE
      Huawei App Gallery-> 3 or AdsConstants.HUAWEI_APP_GALLERY
      
    AdsApplication.getValueFromConfig(
            FirebaseRemoteConfig.getInstance(),
            this,
            AdsConstants.GOOGLE_PLAY_STORE,
            object : FetchRemoteCallback {
                override fun onFetchValuesSuccess() {
                    logD("onFetchValuesSuccess")
                }

                override fun onFetchValuesFailed() {
                    logD("onFetchValuesFailed")
                }

                override fun onUpdateSuccess(adsModel: AdsModel) {
                    logD("onUpdateSuccess : App Id : ${adsModel.admobAppID}   : MAX App Id: ${adsModel.maxAppId}")
                    updateManifest(adsModel = adsModel){
                       logD("Sdk Name :$it")
                    }

                }
            })
        AdsApplication.setAnalytics(FirebaseAnalytics.getInstance(this))
        
        This app register in Manifest file
```

-> Banner Ads setup

```
  BannerAdMediation.showBannerAds(
            this,
            false,
            binding.adContainer,
            object : BannerAdMediation.BannerAdListener {
                override fun onAdsLoaded() {
                    logD("MainActivity Banner Ads : Ads Loaded")
                }
                 override fun onAdsState(adsShowState: AdsShowState) {
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Banner Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Banner Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Banner Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Banner Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Banner Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Banner Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Banner Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Banner Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Banner Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Banner Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Banner Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Banner Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Banner Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Banner Ads : Ads Open")
                    }
                }
            })
   
```

-> Interstitial Ads setup

```


    // Calling This line First show Interstitial Ads in onCreate() function or Application class 
        MediationAdInterstitial.loadInterstitialAds(
                        this@AdsManagerApplication.applicationContext,
                        false,
                        object : MediationAdInterstitial.LoadCallback {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("Interstitial Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("Interstitial Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("Interstitial Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("Interstitial Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("Interstitial Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("Interstitial Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("Interstitial Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("Interstitial Ads : Ads  load failed")
                                }
                            }
                        })
                        
       // After Calling below function when show the ads
     
      MediationAdInterstitial.showInterstitialAds(
            this,
            false,
            object : MediationAdInterstitial.AdsShowCallback {
                override fun onAdsShowState(adsShowState: AdsShowState) {
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity  Interstitial Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity  Interstitial Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Interstitial Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Interstitial Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Interstitial Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Interstitial Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Interstitial Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Interstitial Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Interstitial Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Interstitial Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Interstitial Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Interstitial Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Interstitial Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Interstitial Ads : Ads Open")
                    }
                }
            })
```

-> Native Ads setup

```
    // Requiered native Ads width is match_paren and height is 300dp or above 
    
    
    // Calling This line First show Native Ads in onCreate() function or Application class 
    MediationNativeAds.loadNativeAds(
                        applicationContext,
                        false,
                        object : MediationNativeAds.NativeLoadAdsCallback {
                           override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                                AnalyticsManager.getInstance().setAnalyticsEvent(
                                    resources.getString(R.string.app_name),
                                    "NativeAds",
                                    adsLoadingState.name
                                )
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("NativeAds Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("NativeAds Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("NativeAds Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("NativeAds Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("NativeAds Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("NativeAds Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("NativeAds Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("NativeAds Ads : Ads  load failed")
                                }
                            }
                        })
                        
    // After Calling below function when show the ads
     MediationNativeAds.showNativeAds(
            this,
            false,
            binding.adContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
                override fun onAdsSwipe() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads swipe"
                    )
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Native Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Native Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Native Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Native Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Native Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Native Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Native Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Native Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Native Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Native Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Native Ads : Ads Open")
                    }
                }
            }
        )
        
        
        or 
        
        
     MediationNativeAds.showNativeAds(
            this,
            false,
            binding.adContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
               override fun onAdsSwipe() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        "Ads swipe"
                    )
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "NativeAds",
                        adsShowState.name
                    )
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Native Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Native Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Native Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Native Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Native Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Native Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Native Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Native Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Native Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Native Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Native Ads : Ads Open")
                    }
                }
            }, true
        )
        
        
        
```

-> Reward Ads setup

```

    // Calling This line First show Interstitial Ads in onCreate() function or Application class 
   MediationRewardedAd.loadRewardAds(
            this,
            false,
            object : MediationRewardedAd.RewardLoadCallback {
                 override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                    when (adsLoadingState) {
                        AdsLoadingState.APP_PURCHASED -> logD("MainActivity Reward Ads : You have Purchased your app")
                        AdsLoadingState.NETWORK_OFF -> logD("MainActivity Reward Ads : Internet Off")
                        AdsLoadingState.ADS_OFF -> logD("MainActivity Reward Ads : onAdsOff")
                        AdsLoadingState.ADS_STRATEGY_WRONG -> logD("MainActivity Reward Ads : Ads Strategy wrong")
                        AdsLoadingState.ADS_ID_NULL -> logD("MainActivity Reward Ads : Ads ID is Null found")
                        AdsLoadingState.TEST_ADS_ID -> logD("MainActivity Reward Ads : Test Id found in released mode your app")
                        AdsLoadingState.ADS_LOADED -> logD("MainActivity Reward Ads : onAdsLoaded")
                        AdsLoadingState.ADS_LOAD_FAILED -> logD("MainActivity Reward Ads : Ads load failed")
                    }
                }

            })
            
            
   // After Calling below function when show the ads
   MediationRewardedAd.showRewardAds(
            this,
            false,
            object : MediationRewardedAd.ShowRewardedAdsCallback {
                 override fun onRewardEarned(item: Int, type: String) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardAds",
                        "Amount : $item  : Type : $type"
                    )
                    logD("MainActivity Reward onRewardEarned : Amount : $item  : Type : $type")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity Reward Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity Reward Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity Reward Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Reward Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity Reward Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity Reward Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Reward Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity Reward Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Reward Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity Reward Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity Reward Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity Reward Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity Reward Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity Reward Ads : Ads Open")
                    }
                }

            })
```

-> Reward Interstitial Ads setup

```
      // Calling This line First show Interstitial Ads in onCreate() function or Application class 
       MediationRewardedInterstitialAd.loadRewardedInterstitialAds(
                        applicationContext,
                        false,
                        object : MediationRewardedInterstitialAd.RewardedLoadAds {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("RewardInterstitialAds Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("RewardInterstitialAds Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("RewardInterstitialAds Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("RewardInterstitialAds Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("RewardInterstitialAds Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("RewardInterstitialAds Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("RewardInterstitialAds Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("RewardInterstitialAds Ads : Ads  load failed")
                                }
                            }
                        })
    
    
       // After Calling below function when show the ads
     MediationRewardedInterstitialAd.showRewardedInterstitialAd(
            this,
            false,
            object : MediationRewardedInterstitialAd.ShowRewardAdsCallback {
                 override fun onAdsReward(item: RewardItem) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads RewardItem : $item"
                    )
                    logD("MainActivity Reward Interstitial Ads Reward : $item")
                }
                override fun onAdsDismiss(item: RewardItem) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "RewardInterstitialAds",
                        "Ads Dismiss"
                    )
                    logD(" MainActivity Reward Interstitial Ads Dismiss : $item")
                }

                override fun onAdsShowState(adsShowState: AdsShowState) {
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity RewardInterstitial Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity RewardInterstitial Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity RewardInterstitial Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity RewardInterstitial Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity RewardInterstitial Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity RewardInterstitial Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity RewardInterstitial Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity RewardInterstitial Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity RewardInterstitial Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity RewardInterstitial Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity RewardInterstitial Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity RewardInterstitial Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity RewardInterstitial Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity RewardInterstitial Ads : Ads Open")
                    }
                }
            })
   
```

-> App Open Ads setup

```

    // Calling This line First show Interstitial Ads in onCreate() function or Application class 
    MediationOpenAd.loadAppOpenAds(
                        applicationContext,
                        false,
                        object : MediationOpenAd.AdsLoadedCallback {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {                             
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("AppOpenAds Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("AppOpenAds Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("AppOpenAds Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("AppOpenAds Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("AppOpenAds Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("AppOpenAds Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("AppOpenAds Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("AppOpenAds Ads : Ads  load failed")
                                }
                            }
                        })
                        
                        
                        
    // After Calling below function when show the ads
    MediationOpenAd.showAppOpenAds(
            this,
            false,
            object : MediationOpenAd.AdsShowAppOpenCallback {
                override fun onAdsShowState(adsShowState: AdsShowState) {                 
                    when (adsShowState) {
                        AdsShowState.APP_PURCHASED -> logD("MainActivity  AppOpen Ads : You have Purchased your app")
                        AdsShowState.NETWORK_OFF -> logD("MainActivity  AppOpen Ads : Internet Off")
                        AdsShowState.ADS_OFF -> logD("MainActivity AppOpen Ads : Ads Off")
                        AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity AppOpen Ads : Ads Strategy wrong")
                        AdsShowState.ADS_ID_NULL -> logD("MainActivity AppOpen Ads : Ads ID is Null found")
                        AdsShowState.TEST_ADS_ID -> logD("MainActivity AppOpen Ads : Test Id found in released mode your app")
                        AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity AppOpen Ads : Ads load failed")
                        AdsShowState.ADS_DISMISS -> logD("MainActivity AppOpen Ads : Ads Dismiss")
                        AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity AppOpen Ads : Display Ads failed")
                        AdsShowState.ADS_DISPLAY -> logD("MainActivity AppOpen Ads : Ads Display")
                        AdsShowState.ADS_IMPRESS -> logD("MainActivity AppOpen Ads : Ads Impress Mode")
                        AdsShowState.ADS_CLICKED -> logD("MainActivity AppOpen Ads : Ads Clicked")
                        AdsShowState.ADS_CLOSED -> logD("MainActivity AppOpen Ads : Ads Closed")
                        AdsShowState.ADS_OPEN -> logD("MainActivity AppOpen Ads : Ads Open")
                    }
                }

            })
```

-> Collapsible Banner Ads setup

```

  binding.bannerContainer is a viewBinding template sample if any one do use without bindingView then declare the variable after that initialize the variable and get findViewById() function
  binding.bannerContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.bannerContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                
                // Calling MediaCollapsibleBanner Class 
                 MediationCollapsibleBanner.showCollapsibleBannerAds(
                     this,
                     false,
                     binding.bannerContainer,
                     CollapseBannerState.BOTTOM,
            object : MediationCollapsibleBanner.BannerAdListener {
                 override fun onAdsShowState(adsShowState: AdsShowState) {
                    when (adsShowState) {
                        APP_PURCHASED -> logD("CollapseBannerActivity Ads You have purchased")
                        NETWORK_OFF -> logD("CollapseBannerActivity Ads Network Off")
                        ADS_OFF -> logD("CollapseBannerActivity Ads Of")
                        ADS_STRATEGY_WRONG -> logD("CollapseBannerActivity Ads Strategy Wrong")
                        ADS_ID_NULL -> logD("CollapseBannerActivity Ads Null Id")
                        TEST_ADS_ID -> logD("CollapseBannerActivity Ads Test ID")
                        ADS_LOAD_FAILED -> logD("CollapseBannerActivity Ads Load Failed")
                        ADS_DISMISS -> logD("CollapseBannerActivity Ads Dismiss")
                        ADS_DISPLAY_FAILED -> logD("CollapseBannerActivity Ads Display Failed")
                        ADS_DISPLAY -> logD("CollapseBannerActivity Ads Display")
                        ADS_IMPRESS -> logD("CollapseBannerActivity Ads Impress")
                        ADS_CLICKED -> logD("CollapseBannerActivity Ads Clicked")
                        ADS_CLOSED -> logD("CollapseBannerActivity Ads Closed")
                        ADS_OPEN -> logD("CollapseBannerActivity Ads Open")
                    }
                }
            })   
          }
        })
        
```

-> In App Purchase

```

      AppPurchaseUtils.initConnection(
            this,
            "base64_key_example",
             isAppPurchased = true,  //  if you wants purchased app then pass tru value otherwise pass false for subscribtion
            object : AppPurchaseUtils.BillingCallback {
                override fun onRequiredNetwork() {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "InAppPurchased ",
                        "RequiredNetwork"
                    )
                    logD("Internet is not available")
                }

                override fun onSubscribe(
                    isSuccess: Boolean,
                    isPremium: Boolean,
                    isLocked: Boolean
                ) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "InAppPurchased ",
                        "onSubscriber : isSuccess : $isSuccess , isPremium: $isPremium, isLocked: $isLocked"
                    )
                    logD("isSuccess : $isSuccess , isPremium: $isPremium, isLocked: $isLocked")
                }

                override fun onBillingState(billingState: AppPurchaseUtils.BillingState) {
                    when (billingState) {
                        AppPurchaseUtils.BillingState.FEATURE_NOT_SUPPORTED -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.BILLING_UNAVAILABLE -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.USER_CANCELED -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.DEVELOPER_ERROR -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.ITEM_UNAVAILABLE -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.NETWORK_ERROR -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.SERVICE_DISCONNECTED -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.PENDING -> logD("FEATURE_NOT_SUPPORTED")
                        AppPurchaseUtils.BillingState.UNSPECIFIED_STATE -> logD("FEATURE_NOT_SUPPORTED")
                    }
                }

                override fun onBillingError(error: String) {
                    AnalyticsManager.getInstance().setAnalyticsEvent(
                        resources.getString(R.string.app_name),
                        "In App Purchased ",
                        "Error : $error"
                    )
                    logD(error)
                }
            })
  

         // Only Subscritption Funtion like Subscribe and info
        
        // Subscription 
         CoroutineScope(Dispatchers.Main).launch {
             AppPurchaseUtils.inAppSubscription("product_id_example")
        }

         // info for Subcription
         CoroutineScope(Dispatchers.Main).launch {
           AppPurchaseUtils.getInAppSubscriptionInfo("product_id_example"){
                        logD("Info Detail : $it")
                    }
        }


          // OR


        // Only In-App Purchase function and info


       // In-App Purchased
        CoroutineScope(Dispatchers.Main).launch {
          AppPurchaseUtils.inAppPurchase("product_id_example")
        }

        // info for In-App Puchased
         CoroutineScope(Dispatchers.Main).launch {
           AppPurchaseUtils.getInAppPurchaseInfo("product_id_example"){
                        logD("Info Detail : $it")
                    }
        }


         
         // Disconnect 
        AppPurchaseUtils.clientDestroy()

```

-> Exit Panel

```

     exitPanel(supportFragmentManager, object : PanelListener {
             override fun onExit() {
               logD("Exit Panel")
            }

            override fun onCancel() {
                logD("Cancel Panel ")
            }
        }, model = PanelModel().apply {
            this.title = "App Exit"
            this.titleColor = R.color.black
            this.desc = "Your app is exit?"
            this.descColor = R.color.black
            this.cancelBgColor = R.color.lightGray
            this.cancelButtonText = "Cancel"
            this.cancelButtonTitleColor = R.color.black
            this.exitButtonBgColor = R.color.black
            this.exitButtonText = "exit"
            this.exitButtonTextColor = R.color.white
            this.panelBackgroundColor = R.color.lightGray
            this.isAdsShow = false  // true value is show ads , false  value is not show ads
        })

```

-> App Update

```

      appUpdate(object : AppListener {
            override fun onDownload() {
                logD("OnDownload")
             }

            override fun onInstalled() {
                logD("onInstalled")
            }

            override fun onCancel() {
                logD("onCancel")
            }

            override fun onFailure(error: Exception) {
                logD("onFailure : $error")
            }

            override fun onNoUpdateAvailable() {
                logD("onNoUpdateAvailable")
            }

            override fun onStore(updateState: AppUpdateState) {
                when (updateState) {
                    AppUpdateState.WRONG_STORE -> logD("Wrong Selected Store ID")
                    AppUpdateState.AMAZON_STORE -> logD("Amazon Store Selected")
                    AppUpdateState.HUAWEI_STORE -> logD("Huawei Store Selected")
                }
            }

        }, 1) // 1 or Any Update Key

```

-> App Rate Us

```

    appRateUs(object : AppRateUsCallback {
            override fun onRateUsState(rateState: RateUsState) {
                when (rateState) {
                    RateUsState.RATE_US_COMPLETED -> logD("Rate US Completed")
                    RateUsState.RATE_US_CANCEL -> logD("Rate US Cancel")
                    RateUsState.RATE_US_FAILED -> logD("Rate US Failed")
                    RateUsState.RATE_US_ERROR -> logD("Rate US Error")
                    RateUsState.AMAZON_STORE -> logD("Rate US : Amazon Store Selected")
                    RateUsState.HUAWEI_STORE -> logD("Rate US : Huawei Store Selected")
                    RateUsState.WRONG_STORE -> logD("Rate US : Wrong Store Selected")
                }
            }
        })  

```

-> Analytics

```

    // Calling this line firstly in Application class
     AnalyticsManager.getInstance().setAnalytics(FirebaseAnalytics.getInstance(this))

    // After that we can use it for logEvent likely below
     AnalyticsManager.getInstance().setAnalyticsEvent(
     resources.getString(R.string.app_name), // Event name
     "RewardAds", // Key
     "Ads loaded" // value
      )

```

-> Rate Us Dialog Box

```

    RateUsDialog.getInstance().showDialog(this) // Calling show Rate US Dialog Box
    RateUsDialog.getInstance().dismissDialog()  // Calling Dismiss Rate Us Dialog Box
    RateUsDialog.getInstance().setRateBtnTextColor(R.color.black)  // set Rate Button Background Color so calling this line
    RateUsDialog.getInstance().setRateButtonBg(R.color.black) // set Rate Button Text Color so calling this line
    RateUsDialog.getInstance().setTextColor(R.color.black) // set Rate Text Color so calling this line

```

->  Compose Banner Ads

```

    ShowBannerAds(modifier = Modifier.height(50.dp),
                        false,
                        object : BannerAdsListener {
                            override fun onBannerAdsState(adsState: AdsState) {
                                when (adsState) {
                                    AdsState.ADS_OFF -> logD("ADS_OFF")
                                    AdsState.NETWORK_OFF -> logD("NETWORK_OFF")
                                    AdsState.APP_PURCHASED -> logD("APP_PURCHASED")
                                    AdsState.ADS_STRATEGY_WRONG -> logD("ADS_STRATEGY_WRONG")
                                    AdsState.ADS_ID_NULL -> logD("ADS_ID_NULL")
                                    AdsState.TEST_ADS_ID -> logD("TEST_ADS_ID")
                                    AdsState.ADS_LOAD_FAILED -> logD("ADS_LOAD_FAILED")
                                    AdsState.ADS_DISMISS -> logD("ADS_DISMISS")
                                    AdsState.ADS_DISPLAY_FAILED -> logD("ADS_DISPLAY_FAILED")
                                    AdsState.ADS_DISPLAY -> logD("ADS_DISPLAY")
                                    AdsState.ADS_IMPRESS -> logD("ADS_IMPRESS")
                                    AdsState.ADS_LOADED -> logD("ADS_LOADED")
                                    AdsState.ADS_CLICKED -> logD("ADS_CLICKED")
                                    AdsState.ADS_CLOSED -> logD("ADS_CLOSED")
                                    AdsState.ADS_OPENED -> logD("ADS_OPENED")
                                }
                            }
                        })

```

-> Compose Native Ads

```

     // Requiered native Ads width is match_paren and height is 300dp or above 
    
    
    // Calling This line First show Native Ads in onCreate() function or Application class 
    MediationNativeAds.loadNativeAds(
                        applicationContext,
                        false,
                        object : MediationNativeAds.NativeLoadAdsCallback {
                            override fun onAdsLoadState(adsLoadingState: AdsLoadingState) {                               
                                when (adsLoadingState) {
                                    AdsLoadingState.APP_PURCHASED -> logD("NativeAds Ads : App Purchased")
                                    AdsLoadingState.NETWORK_OFF -> logD("NativeAds Ads : Internet Off")
                                    AdsLoadingState.ADS_OFF -> logD("NativeAds Ads is Off")
                                    AdsLoadingState.ADS_STRATEGY_WRONG -> logD("NativeAds Ads : Ads Strategy wrong")
                                    AdsLoadingState.ADS_ID_NULL -> logD("NativeAds Ads : Ads Is Null found")
                                    AdsLoadingState.TEST_ADS_ID -> logD("NativeAds Ads : Test Id found in released mode your app")
                                    AdsLoadingState.ADS_LOADED -> logD("NativeAds Ads Loaded")
                                    AdsLoadingState.ADS_LOAD_FAILED -> logD("NativeAds Ads : Ads  load failed")
                                }
                            }
                        })
                        
    // After Calling below function when show the ads
      ShowNativeAds(activity = this@ComposeAdsActivity,
                        isPurchased = false,
                        listener = object : MediationNativeAds.ShowNativeAdsCallback {
                            override fun onAdsSwipe() {                              
                                logD("MainActivity onAdsSwipe")
                            }

                            override fun onAdsShowState(adsShowState: AdsShowState) {                              
                                when (adsShowState) {
                                    AdsShowState.APP_PURCHASED -> logD("MainActivity Native Ads : You have Purchased your app")
                                    AdsShowState.NETWORK_OFF -> logD("MainActivity Native Ads : Internet Off")
                                    AdsShowState.ADS_OFF -> logD("MainActivity Native Ads : Ads Off")
                                    AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads : Ads Strategy wrong")
                                    AdsShowState.ADS_ID_NULL -> logD("MainActivity Native Ads : Ads ID is Null found")
                                    AdsShowState.TEST_ADS_ID -> logD("MainActivity Native Ads : Test Id found in released mode your app")
                                    AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads : Ads load failed")
                                    AdsShowState.ADS_DISMISS -> logD("MainActivity Native Ads : Ads Dismiss")
                                    AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads : Display Ads failed")
                                    AdsShowState.ADS_DISPLAY -> logD("MainActivity Native Ads : Ads Display")
                                    AdsShowState.ADS_IMPRESS -> logD("MainActivity Native Ads : Ads Impress Mode")
                                    AdsShowState.ADS_CLICKED -> logD("MainActivity Native Ads : Ads Clicked")
                                    AdsShowState.ADS_CLOSED -> logD("MainActivity Native Ads : Ads Closed")
                                    AdsShowState.ADS_OPEN -> logD("MainActivity Native Ads : Ads Open")
                                }
                            }
                        },
                       false
                    )
        or 
        
        
      ShowNativeAds(activity = this@ComposeAdsActivity,
                        isPurchased = false,
                        listener = object : MediationNativeAds.ShowNativeAdsCallback {
                             override fun onAdsSwipe() {                               
                                logD("MainActivity onAdsSwipe")
                            }

                            override fun onAdsShowState(adsShowState: AdsShowState) {                               
                                when (adsShowState) {
                                    AdsShowState.APP_PURCHASED -> logD("MainActivity Native Ads : You have Purchased your app")
                                    AdsShowState.NETWORK_OFF -> logD("MainActivity Native Ads : Internet Off")
                                    AdsShowState.ADS_OFF -> logD("MainActivity Native Ads : Ads Off")
                                    AdsShowState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads : Ads Strategy wrong")
                                    AdsShowState.ADS_ID_NULL -> logD("MainActivity Native Ads : Ads ID is Null found")
                                    AdsShowState.TEST_ADS_ID -> logD("MainActivity Native Ads : Test Id found in released mode your app")
                                    AdsShowState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads : Ads load failed")
                                    AdsShowState.ADS_DISMISS -> logD("MainActivity Native Ads : Ads Dismiss")
                                    AdsShowState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads : Display Ads failed")
                                    AdsShowState.ADS_DISPLAY -> logD("MainActivity Native Ads : Ads Display")
                                    AdsShowState.ADS_IMPRESS -> logD("MainActivity Native Ads : Ads Impress Mode")
                                    AdsShowState.ADS_CLICKED -> logD("MainActivity Native Ads : Ads Clicked")
                                    AdsShowState.ADS_CLOSED -> logD("MainActivity Native Ads : Ads Closed")
                                    AdsShowState.ADS_OPEN -> logD("MainActivity Native Ads : Ads Open")
                                }
                            }

                        },
                        true
                    )

```

-> Other AdMob Ads in calling in compose same way because native and banner ads required view
therefore create the new function for those ads

-> Compose Rate Us Dialog Box

```

    val rateDialog = remember {
            mutableStateOf(false)
        }
        RateUsDialog(context = this@ComposeAdsActivity, showDialog = rateDialog)

```

-> Compose Exit Panel

```

    val scope = rememberCoroutineScope()
        val isBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        isBottomSheetVisible.value.then {
            BottomSheet(
                this@ComposeAdsActivity,
                isBottomSheetVisible = isBottomSheetVisible.value,
                sheetState = sheetState,
                panelTitle = "App Exit",
                panelTitleColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelDes = "Your app is exit?",
                panelDesColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelCancelBtnBgColor = Color.LightGray,
                panelCancelTitleColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelExitBtnBgColor = Color(ContextCompat.getColor(this, R.color.black)),
                panelExitTitleColor = Color.White,
                isAdsShow = true,
                onDismiss = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { isBottomSheetVisible.value = false }
                },
                onExit = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { isBottomSheetVisible.value = false }
                    finishAffinity()
                }
            )
        }

```

-> DeviceInfoUtils

```
    // get short device info
    DeviceInfoUtils.getDeviceInfo() 

   // get local IP Address 

   DeviceInfoUtils.getLocalIpAddress(this) {
            logD(" Local Ip Address : $it")
        }

```
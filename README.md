# AdsManager

-> add Project level gradle

```project level gradle
      allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

-> add module level gradle

```add module lvel gradle
  implementation 'com.github.mobinators:AdsManager:1.1.6'
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
            android:name="applovin.sdk.key"
            android:value="@string/SDK_KEY" />
        <meta-data
            android:name="com.google.android.gms.ads.flag.NATIVE_AD_DEBUGGER_ENABLED"
            android:value="true" />
            
             
```

-> Ad this line String File

```
     <string name="app_ads_id">ca-app-pub-3940256099942544~3347511713</string>  provide origin App id for show original ads
    <string name="SDK_KEY">sVWGuOQVG4gzyhb-2Qb6sRTv8qavlPzA-5V-9Nol8469q4z7rp11DcTfCWvHWTNRTTB12ENHdoQyLpX5LVcPGq</string>  provide original AppLovin Sdk id
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

                override fun onUpdateSuccess(appId: String, maxAppId: String) {
                    logD("onUpdateSuccess : App Id : $appId  : MAX App Id: $maxAppId")
                    updateManifest(appId = appId, maxAppId = maxAppId)
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
                override fun onAdsOff() {
                    logD("MainActivity Banner Ads : Ads Off")
                }

                override fun onAdsLoaded() {
                    logD("MainActivity Banner Ads : Ads Loaded")
                }

                override fun onAdsClicked() {
                    logD("MainActivity Banner Ads : Ads Clicked")
                }

                override fun onAdsClosed() {
                    logD("MainActivity Banner Ads : Ads Closed")
                }

                override fun onAdsOpened() {
                    logD("MainActivity Banner Ads : Ads Open")
                }

                override fun onAdsError(adsErrorState: AdsErrorState) {
                    when (adsErrorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Banner Ads : Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Banner Ads : You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Banner Ads : Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Banner Ads : Ads ID is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Banner Ads : Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Banner Ads : Ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Banner Ads : Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Banner Ads : Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Banner Ads : Ads Impress Mode")
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
                            override fun onAdsLoaded() {
                                logD("Interstitial Ads Loaded")
                            }

                            override fun onAdsError(errorState: AdsErrorState) {
                                when (errorState) {
                                    AdsErrorState.NETWORK_OFF -> logD("Interstitial Ads : Internet Off")
                                    AdsErrorState.APP_PURCHASED -> logD("Interstitial Ads : You have Purchased your app")
                                    AdsErrorState.ADS_STRATEGY_WRONG -> logD("Interstitial Ads : Ads Strategy wrong")
                                    AdsErrorState.ADS_ID_NULL -> logD("Interstitial Ads : Ads Is Null found")
                                    AdsErrorState.TEST_ADS_ID -> logD("Interstitial Ads : Test Id found in released mode your app")
                                    AdsErrorState.ADS_LOAD_FAILED -> logD("Interstitial Ads : Ads  load failed")
                                    AdsErrorState.ADS_DISMISS -> logD("Interstitial Ads : Ads Dismiss")
                                    AdsErrorState.ADS_DISPLAY_FAILED -> logD("Interstitial Ads : Display Ads failed")
                                    AdsErrorState.ADS_IMPRESS -> logD("Interstitial Ads : Ads Impress Mode")
                                }
                            }

                            override fun onAdsOff() {
                                logD("Interstitial Ads is Off")
                            }

                        })
                        
       // After Calling below function when show the ads
     
      MediationAdInterstitial.showInterstitialAds(
            this,
            false,
            object : MediationAdInterstitial.AdsShowCallback {
                override fun onAdsOff() {
                    logD("MainActivity Interstitial Ads is off")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Interstitial Ads : Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Interstitial Ads : You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Interstitial Ads : Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Interstitial Ads : Ads ID is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Interstitial Ads : Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Interstitial Ads : Ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Interstitial Ads : Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Interstitial Ads : Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Interstitial Ads : Ads Impress Mode")
                    }
                }

                override fun onAdsClicked() {
                    logD("MainActivity Interstitial Ads click")
                }
            })
```

-> Native Ads setup

```
    // Requiered native Ads width is match_paren and height is 300dp or above 
    
    
    // Calling This line First show Interstitial Ads in onCreate() function or Application class 
    MediationNativeAds.loadNativeAds(
                        applicationContext,
                        false,
                        object : MediationNativeAds.NativeLoadAdsCallback {
                            override fun onAdsOff() {
                                logD("Native Ads Loaded off")
                            }

                            override fun onAdsLoaded() {
                                logD("Native Ads Loaded")
                            }

                            override fun onAdsError(errorState: AdsErrorState) {
                                when (errorState) {
                                    AdsErrorState.NETWORK_OFF -> logD("Native Ads Internet Off")
                                    AdsErrorState.APP_PURCHASED -> logD("Native AdsYou have Purchased your app")
                                    AdsErrorState.ADS_STRATEGY_WRONG -> logD("Native Ads Strategy wrong")
                                    AdsErrorState.ADS_ID_NULL -> logD("Native Ads  Is Null found")
                                    AdsErrorState.TEST_ADS_ID -> logD("Native Test Id found in released mode your app")
                                    AdsErrorState.ADS_LOAD_FAILED -> logD("Native Ads  load failed")
                                    AdsErrorState.ADS_DISMISS -> logD("Native Ads Dismiss")
                                    AdsErrorState.ADS_DISPLAY_FAILED -> logD("Native Display Ads failed")
                                    AdsErrorState.ADS_IMPRESS -> logD("Native  Ads Impress Mode")
                                }
                            }
                        })
                        
    // After Calling below function when show the ads
     MediationNativeAds.showNativeAds(
            this,
            false,
            binding.adContainer,
            object : MediationNativeAds.ShowNativeAdsCallback {
                override fun onAdsOff() {
                    logD("MainActivity Ads Off")
                }

                override fun onAdsOpen() {
                    logD("MainActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    logD("MainActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    logD("MainActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Native Ads Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Native Ads You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Native Ads Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Native Ads Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Native Ads Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Native Ads Ads Impress Mode")
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
                override fun onAdsOff() {
                    logD("MainActivity Ads Off")
                }

                override fun onAdsOpen() {
                    logD("MainActivity onAdsOpen")
                }

                override fun onAdsClicked() {
                    logD("MainActivity onAdsClicked")
                }

                override fun onAdsClosed() {
                    logD("MainActivity onAdsClosed")
                }

                override fun onAdsSwipe() {
                    logD("MainActivity onAdsSwipe")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Native Ads Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Native Ads You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Native Ads Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Native Ads Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Native Ads Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Native Ads ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Native Ads Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Native Ads Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Native Ads Ads Impress Mode")
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
                override fun onAdsLoaded() {
                    logD("MainActivity Reward Ads : onAdsLoaded")
                }

                override fun onAdsOff() {
                    logD("MainActivity Reward Ads : onAdsOff")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("MainActivity Reward Ads : Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("MainActivity Reward Ads : You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("MainActivity Reward Ads : Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("MainActivity Reward Ads : Ads ID is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("MainActivity Reward Ads : Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("MainActivity Reward Ads : Ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("MainActivity Reward Ads : Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("MainActivity Reward Ads : Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("MainActivity Reward Ads : Ads Impress Mode")
                    }
                }

            })
            
            
   // After Calling below function when show the ads
   MediationRewardedAd.showRewardAds(
            this,
            false,
            object : MediationRewardedAd.ShowRewardedAdsCallback {
                override fun onAdsOff() {
                    logD("MainActivity Reward onAdsOff")
                }

                override fun onRewardEarned(item: Int, type: String) {
                    logD("MainActivity Reward onRewardEarned : Amount : $item  : Type : $type")
                }

                override fun onAdsClicked() {
                    logD("MainActivity Reward onAdsClicked")
                }

                override fun onAdsDisplay() {
                    logD("MainActivity Reward onAdsDisplay")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("Ads Impress Mode")
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
                            override fun onAdsLoaded() {
                                logD("Reward Interstitial Ads Loaded")
                            }

                            override fun onAdsOff() {
                                logD("Reward Interstitial Ads is off")
                            }

                            override fun onAdsError(error: String) {
                                logException(error)
                            }

                        })
    
    
       // After Calling below function when show the ads
     MediationRewardedInterstitialAd.showRewardedInterstitialAd(
            this,
            false,
            object : MediationRewardedInterstitialAd.ShowRewardAdsCallback {
                override fun onAdsOff() {
                    logD("MainActivity Reward Interstitial Ads is off")
                }

                override fun onAdsError(error: String) {
                    logD("MainActivity Reward Interstitial Ads Error : $error")
                }

                override fun onAdsReward(item: RewardItem) {
                    logD("MainActivity Reward Interstitial Ads Reward : $item")
                }

                override fun onAdsClicked() {
                    logD("MainActivity Reward Interstitial Ads Clicked")
                }

                override fun onAdsImpress() {
                    logD("MainActivity Reward Interstitial Ads Impress")
                }

                override fun onAdsDismiss(item: RewardItem) {
                    logD(" MainActivity Reward Interstitial Ads Dismiss : $item")
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
                            override fun onAdsOff() {
                                logD("AppOpen Ads is off")
                            }

                            override fun onAdsLoaded() {
                                logD("AppOpen Ads onAdsLoaded")
                            }

                            override fun onAdsError(errorState: AdsErrorState) {
                                when (errorState) {
                                    AdsErrorState.NETWORK_OFF -> logD("Internet Off")
                                    AdsErrorState.APP_PURCHASED -> logD("You have Purchased your app")
                                    AdsErrorState.ADS_STRATEGY_WRONG -> logD("App Open Ads Strategy wrong")
                                    AdsErrorState.ADS_ID_NULL -> logD("App Open Ads Is Null found")
                                    AdsErrorState.TEST_ADS_ID -> logD("App Open Test Id found in released mode your app")
                                    AdsErrorState.ADS_LOAD_FAILED -> logD("App Open Ads  load failed")
                                    AdsErrorState.ADS_DISMISS -> logD("App Open Ads Dismiss")
                                    AdsErrorState.ADS_DISPLAY_FAILED -> logD("App Open Display Ads failed")
                                    AdsErrorState.ADS_IMPRESS -> logD("App Open Ads Impress Mode")
                                }
                            }
                        })
                        
                        
                        
    // After Calling below function when show the ads
    MediationOpenAd.showAppOpenAds(
            this,
            false,
            object : MediationOpenAd.AdsShowAppOpenCallback {
                override fun onAdsOff() {
                    logD("MainActivity App Open Ads is off")
                }

                override fun onAdsClicked() {
                    logD("MainActivity App Open Ads Clicked")
                }

                override fun onAdsDisplay() {
                    logD("MainActivity App Open Ads onAdsDisplay")
                }

                override fun onAdsError(errorState: AdsErrorState) {
                    when (errorState) {
                        AdsErrorState.NETWORK_OFF -> logD("Internet Off")
                        AdsErrorState.APP_PURCHASED -> logD("You have Purchased your app")
                        AdsErrorState.ADS_STRATEGY_WRONG -> logD("Ads Strategy wrong")
                        AdsErrorState.ADS_ID_NULL -> logD("Ads Is Null found")
                        AdsErrorState.TEST_ADS_ID -> logD("Test Id found in released mode your app")
                        AdsErrorState.ADS_LOAD_FAILED -> logD("ads load failed")
                        AdsErrorState.ADS_DISMISS -> logD("Ads Dismiss")
                        AdsErrorState.ADS_DISPLAY_FAILED -> logD("Display Ads failed")
                        AdsErrorState.ADS_IMPRESS -> logD("Ads Impress Mode")
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
                override fun onAdsOff() {
                    logD("CollapseBannerActivity Ads Of")
                }

                override fun onAdsLoaded() {
                    logD("CollapseBannerActivity Ads Loaded")
                }

                override fun onAdsError(adsErrorState: AdsErrorState) {
                    when (adsErrorState) {
                        NETWORK_OFF -> logD("CollapseBannerActivity Ads Network Off")
                        APP_PURCHASED -> logD("CollapseBannerActivity Ads You have purchased")
                        ADS_STRATEGY_WRONG -> logD("CollapseBannerActivity Ads Strategy Wrong")
                        ADS_ID_NULL -> logD("CollapseBannerActivity Ads Null Id")
                        TEST_ADS_ID -> logD("CollapseBannerActivity Ads Test ID")
                        ADS_LOAD_FAILED -> logD("CollapseBannerActivity Ads Load Failed")
                        ADS_DISMISS -> logD("CollapseBannerActivity Ads Dismiss")
                        ADS_DISPLAY_FAILED -> logD("CollapseBannerActivity Ads Display Failed")
                        ADS_IMPRESS -> logD("CollapseBannerActivity Ads Impress")
                    }
                }

            })   
          }
        })
        
        


-> In App Purchase

```

     AppPurchaseUtils.initConnection(
            this,
           base64_key_example,
            object : PurchaseUtils.BillingCallback {
                override fun onRequiredNetwork() {
                   Log.d("Tag","Internet is not available")
                }

                override fun onSubscribe(
                    isSuccess: Boolean,
                    isPremium: Boolean,
                    isLocked: Boolean
                ) {
                    Log.d("Tag","isSuccess : $isSuccess , isPremium: $isPremium, isLocked: $isLocked")
                }

                override fun onError(error: String) {
                   Log.d("Tag","$error")
                }
            })
  
        
        // Subscription 
         CoroutineScope(Dispatchers.Main).launch {
            AppPurchaseUtils.onSubscription("product_id_example")
        }
        
    
         // info for Subcription
         CoroutineScope(Dispatchers.Main).launch {
           AppPurchaseUtils.getSubscriptionInfo(AppConstants.WEEKLY_SUBSCRIPTION) {
              Log.d("Tag","Price: $it")
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



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
  implementation 'com.github.mobinators:AdsManager:1.0.4'
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
    
    Dependency 
    
    implementation platform('com.google.firebase:firebase-bom:32.2.3')
    implementation 'com.google.firebase:firebase-analytics-ktx:21.3.0'
    implementation 'com.google.firebase:firebase-config-ktx:21.4.1'
    implementation 'com.google.firebase:firebase-crashlytics-ktx:18.4.1'
```

-> Banner Ads setup

```
  BannerAdMediation.showBannerAds(
            this,
            false,
            binding.adContainer,
            object : BannerAdListener {
                override fun onLoaded(adType: Int) {
                    
                }

                override fun onAdClicked(adType: Int) {
                   
                }

                override fun onError(error: String) {
                    
                }

                override fun onFacebookAdCreated(facebookBanner: AdView) {
                    
                }

                override fun isEnableAds(isAds: Boolean) {
                    
                }

                override fun isOffline(offline: Boolean) {
                   
                }
            })
   
```

-> Interstitial Ads setup

```
    MediationAdInterstitial.showInterstitialAd(this, false, object : InterstitialAdsListener {
            override fun onLoaded(adType: Int) {
               
            }

            override fun onClicked(adType: Int) {
                
            }

            override fun onBeforeAdShow() {
                
            }

            override fun onDismisses(adType: Int) {
                
            }

            override fun onError(error: String) {
               
            }

            override fun isEnableAds(isAds: Boolean) {
                
            }

            override fun isOffline(offline: Boolean) {
               
            }
        })
```

-> Native Ads setup

```
    private var mediationNativeAd: MediationNativeAd? = null
    mediationNativeAd = MediationNativeAd(this, false, binding.adContainer, true)
    mediationNativeAd!!.loadAd(object : OnNativeAdListener {
            override fun onError(error: String) {
                logD("MainActivity Error : $error")
            }

            override fun onLoaded(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun onAdClicked(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
        
        
        or 
        
        
    private var mediationNativeAd: MediationNativeAd? = null
    mediationNativeAd = MediationNativeAd(this, false, binding.adContainer)
    mediationNativeAd!!.loadAd(object : OnNativeAdListener {
            override fun onError(error: String) {
                logD("MainActivity Error : $error")
            }

            override fun onLoaded(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun onAdClicked(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
        
        
        
```

-> Native Banner Ads setup

```
   MediationNativeBanner.loadAd(this, false, binding.adContainer, object : OnNativeAdListener {
            override fun onError(error: String) {
                logD("MainActivity Error : $error")
            }

            override fun onLoaded(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun onAdClicked(adType: Int) {
                logD("MainActivity Ads Type : $adType")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        }, object : ImageProvider {
            override fun onProviderImage(imageView: ImageView, imageUrl: String) {
                imageView.createThumbNail(this@MainActivity, imageUrl)
            }
        })
```

-> Reward Ads setup

```
   MediationRewardedAd.loadRewardedAd(this, false,  object : OnRewardedAdListener {
            override fun onError(error: String) {
                logD("MainActivity onError Error : $error")
            }

            override fun onAdLoaded(adType: Int) {
                logD("MainActivity onAdLoaded Ads Type : $adType")
            }

            override fun onClicked(adType: Int) {
                logD("MainActivity onClicked Ads Type : $adType")
            }

            override fun onDismissClick(adType: Int) {
                logD("MainActivity onDismissClick Ads Type : $adType")
            }

            override fun onRewarded(item: RewardItem) {
                logD("MainActivity onRewarded  : ${item.type} : rewarded : ${item.amount}")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }

        })
```

-> Reward Interstitial Ads setup

```
    MediationRewardedInterstitialAd.loadRewardedInterstitialAd(
            this,
            false,
            object : OnRewardedAdListener {
                override fun onError(error: String) {
                    logD("MainActivity onError Error : $error")
                }

                override fun onAdLoaded(adType: Int) {
                    logD("MainActivity onAdLoaded Ads Type : $adType")
                }

                override fun onClicked(adType: Int) {
                    logD("MainActivity onClicked Ads Type : $adType")
                }

                override fun onDismissClick(adType: Int) {
                    logD("MainActivity onDismissClick Ads Type : $adType")
                }

                override fun onRewarded(item: RewardItem) {
                    logD("MainActivity onRewarded  : ${item.type} : rewarded : ${item.amount}")
                }

                override fun isEnableAds(isAds: Boolean) {
                    logD("MainActivity isEnableAds : $isAds")
                }

                override fun isOffline(offline: Boolean) {
                    logD("Ads is Offline : $offline")
                }
            })
```

-> App Open Ads setup

```
   MediationOpenAd.loadAppOpenAd(this, false, object : OpenAddCallback {
            override fun onDismissClick() {
                logD("MainActivity onDismissClick")
            }

            override fun onErrorToShow(error: String) {
                logD("MainActivity onErrorToShow Error : $error")
            }

            override fun isEnableAds(isAds: Boolean) {
                logD("MainActivity isEnableAds : $isAds")
            }

            override fun isOffline(offline: Boolean) {
                logD("Ads is Offline : $offline")
            }
        })
```

-> In App Purchase

```
    private var inAppPurchaseUtils: InAppPurchaseUtils? = null
     inAppPurchaseUtils =
            InAppPurchaseUtils(
                this,
                "product_id_example",
                "base64_key_example",
                object : BillingCallback {
                    override fun onSubscribe(msg: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD(msg)
                        }
                    }

                    override fun onAlreadySubscribe(msg: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD(msg)
                        }
                    }

                    override fun onFeatureNotSupported() {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD("")
                        }
                    }

                    override fun onBillingError(error: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD(" Error : $error")
                        }
                    }

                    override fun onSubscriptionPending(msg: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD(msg)
                        }
                    }

                    override fun onUnspecifiedState(msg: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD(msg)
                        }
                    }

                    override fun onProductDetail(productDetail: InAppPurchasedModel) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD("Purchase Detail :${productDetail}")
                        }
                    }

                    override fun isOffline(offline: Boolean) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD("isOffline : $offline")
                        }
                    }

                    override fun onServiceDisConnected() {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD("ServiceDisConnected")
                        }
                    }

                    override fun onBillingFinished(state: ConnectionState) {
                        CoroutineScope(Dispatchers.Main).launch {
                            logD("premium : ${state.premium}  : locked : ${state.locked}")
                        }
                    }
                })
       inAppPurchaseUtils!!.startConnection()  // Create object 
       inAppPurchaseUtils!!.getSubscriptionInfo() // get information of SubScription 


      inAppPurchaseUtils!!.startSubSubscription() // SubScription 
      
      
      
      
      ----------------------------------OR----------------------------- 
                       
                       
      AppPurchaseUtils.startConnection(this, "base64_key_example", object : PurchaseCallBack {
            override fun onPurchaseState(state: SubscriptionState) {
                when (state) {
                    is SubscriptionState.AlReadySubscribe -> Log.d("Tag", "onPurchaseState: AlReadySubscribe ")
                    is SubscriptionState.PendingSubscribe -> Log.d("Tag", "onPurchaseState: PendingSubscribe ")
                    is SubscriptionState.ProductDetail ->{
                        Log.d("Tag", "onPurchaseState: ProductDetail : ${state.model} ")
                        val productDetail=state.model  // Detail for Subscription
                
                    }
                    is SubscriptionState.Subscribed ->  Log.d("Tag", "onPurchaseState: Subscribed: ${state.isSuccess} ")
                    is SubscriptionState.SubscriptionFailure ->  Log.d("Tag", "onPurchaseState: SubscriptionFailure : ${state.error} ")
                    is SubscriptionState.SubscriptionFinished -> {
                        Log.d("Tag", "onPurchaseState: SubscriptionFinished ${state.isPremium} ")
                      
                    }
                    is SubscriptionState.UnspecifiedState ->  Log.d("Tag", "onPurchaseState: UnspecifiedState ")
                }
            }
        })
        
        
        // Subscription 
        AppPurchaseUtils.startSubscription("product_id_example)
         
         // info for Subcription
        AppPurchaseUtils.getSubscriptionInfo("product_id_example")
         
         
         // Disconnect 
        AppPurchaseUtils.disConnected()
         
     
     
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


-> Ads Strategy 

``` 
  0-> ads is off
  1-> Admob Mediation enable and change the value of ADMOB_MEDIATION_KEY  is true
  2-> Admob Ads enable
  3-> Max Mediation enable 

  if you want enable App Open ads so change the value of  ADMOB_OPEN_AD_ENABLE is true
  
  remote_config_maanger_json_file is a folder which contain the remote config json file because any one create new Firebase project with remote config so import it file after that imported it then change the value of ads key 
  
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
    <string name="SDK_KEY">sVWGuOQVG4gzyhb-2Qb6sRTv8qavlPzA-5V-1DcTfCWvHWTNRTTB12ENHdoQyLpX5LVcPGq9Nol8469q4z7rp1</string>  provide original AppLovin Sdk id
```

-> Ad this line in Application
```
    AdsApplication.getValueFromConfig(
            FirebaseRemoteConfig.getInstance(),
            this,
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
  
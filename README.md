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
  implementation 'com.github.mobinators:AdsManager:1.0.1'
```

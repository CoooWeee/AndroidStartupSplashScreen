# Android Startup Splash Screen
A Java Android Activity which will be displayed a curtain time before the MainActivity is launched. Internet connection will be checked too.

1.
Example for Manifest code:
```
        <activity
            android:name="com.coooweee.splash.SplashActivity"
            android:label="@string/my_title_activity_main"
            android:theme="@style/AppTheme.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
```

2.
Create activity_splash.xml

3.
adjusting delay and mainActivity in SplashActivity 
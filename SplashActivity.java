package com.coooweee.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coooweee.coooweee.R;
import com.coooweee.coooweee.activities.BubbletabActivity;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * <pre>
 *      author : GA
 *      time   : 15/06/2019
 *      desc   :    Activity which is show on startup of an app.
 *                  In the background the internet connection will be checked.
 *                  The layout is what ever you like stored in activity_splash.xml.
 * </pre>
 */
public class SplashActivity extends AppCompatActivity {
    // loader for configs
    private SplashLoader loader;

    // how long the splash will be displayed
    private final int delay = 1;

    // which acticity should be loaded after the delay
    private final Class<?> mainActivity = BubbletabActivity.class;

    /**
     * Check whether the network is connected
     *
     * @return true if connected
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();
        start();
    }


    private void start() {
        //load
        loader.process(() -> {
            //Go to the main page
            Log.e("SplashLoader", "onComplete：" + System.currentTimeMillis());
            startActivity(new Intent(SplashActivity.this, mainActivity));
            finish();
        }, throwable -> {
            Log.e("SplashLoader", "onError：" + System.currentTimeMillis());
            Toast.makeText(SplashActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void init() {
        Observable<Object> localConfig = Observable.create(e -> {
            Thread.sleep(500);
            e.onNext(new Object());
            e.onComplete();
        }).map(result -> {
            Log.e("local", "The local configuration is loaded：" + System.currentTimeMillis());
            return result;
        }).subscribeOn(Schedulers.io());

        Observable<Object> necessaryNetworkConfig = Observable.create(e -> {
            Thread.sleep(2_000);
            e.onNext(new Object());
            e.onComplete();
        }).map(result -> {
            Log.e("online", "Online configuration is loaded：" + System.currentTimeMillis());
            return result;
        }).subscribeOn(Schedulers.io());


        Observable<Object> offlineConfig = Observable.create(e -> {
            Thread.sleep(1_000);
            e.onNext(new Object());
            e.onComplete();
        }).map(result -> {
            Log.e("offline", "Offline configuration is loaded：" + System.currentTimeMillis());
            return result;
        }).subscribeOn(Schedulers.io());

        loader = new SplashLoader.Builder()
                .delayMilli(delay * 1000)
                .localConfig(localConfig)
                .onlineConfig(necessaryNetworkConfig)
                .offlineConfig(offlineConfig)
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // dispose the loader
        loader.recycle();
    }
}
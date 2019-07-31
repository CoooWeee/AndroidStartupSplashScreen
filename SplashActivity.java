package com.coooweee.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
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

    // how long the splash will be displayed in seconds
    private final int delay = 2;  // secs

    public enum broadCastDataType {
        exit,
        exitText
    }

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


        if (getIntent().getBooleanExtra(broadCastDataType.exit.toString(), false)) {
            String text = getIntent().getStringExtra(broadCastDataType.exitText.toString());
            if(text != null)
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();

            new Handler().postDelayed(() -> { //Handler(Looper.getMainLooper())
                finish();
            }, delay * 1000);
            return;
        }

        init();
    }

    public void exit(String text) {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(broadCastDataType.exit.toString(), true);
        if (text != null)
            intent.putExtra(broadCastDataType.exitText.toString(), text);
        finish();
        startActivity(intent);
    }

    private void init() {
        // [GA]: no internet no access ATM
        Observable<Object> offlineConfig = Observable.create(e -> exit("Please connect to the internet.")).map(result -> {
            Log.d("offline", "Offline configuration is loaded：" + System.currentTimeMillis());
            return result;
        }).subscribeOn(Schedulers.io());

        loader = new SplashLoader.Builder()
                .delayMilli(delay * 1000)
                .offlineConfig(offlineConfig)
                .build();

        //load
        loader.process(() -> {
            //Go to the main page
            Log.d("SplashLoader", "onComplete：" + System.currentTimeMillis());
            startActivity(new Intent(this, mainActivity));
            finish();
        }, throwable -> {
            Log.e("SplashLoader", "onError：" + System.currentTimeMillis());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // dispose the loader
        if(loader != null)
            loader.recycle();
    }
}
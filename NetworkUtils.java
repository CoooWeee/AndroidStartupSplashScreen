package com.coooweee.splash;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * <pre>
 *      author : GA
 *      time   : 15/06/2019
 *      desc   : Network tool
 * </pre>
 */
public final class NetworkUtils {
    private NetworkUtils() {
        throw new IllegalStateException();
    }

    /**
     * Check whether the network is connected
     *
     * @return true if connected
     */
    public static boolean isConnected() {
        NetworkInfo info = ((ConnectivityManager) App.getApp().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
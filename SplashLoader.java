package com.coooweee.splash;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Timed;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * <pre>
 *      author : GA
 *      time : 15/06/2019
 *      desc :
 * </pre>
 */

public class SplashLoader {
    // In case we wanna skip without waiting for the delay
    private final Subject<Boolean> skipSubject = BehaviorSubject.create();

    // Placeholder for empty objects
    private final static Object OBJ = new Object();

    // Local configuration
    private Observable<Object> localConfig;

    // Configuration when there is a network
    private Observable<Object> onlineConfig;

    // Configuration without network
    private Observable<Object> offlineConfig;

    // How long to show the splash
    private long delayMilli;

    // Jump over
    private boolean doSkip;

    // RX life cycle cleanup
    private Disposable processDisposable;


    /**
     * Configure Splash
     */
    private SplashLoader(Builder builder) {
        localConfig = builder.localConfig;
        onlineConfig = builder.onlineConfig;
        offlineConfig = builder.offlineConfig;
        delayMilli = builder.delayMilli;
    }

    /**
     * Begin execution
     *
     * @param onComplete start was successful
     * @param onError start failed
     */
    void process(@NonNull final Action onComplete, @NonNull final Consumer<? super Throwable> onError) {
        if (localConfig == null) {
            localConfig = Observable.just(OBJ);
        }
        if (onlineConfig == null) {
            onlineConfig = Observable.just(OBJ);
        }
        if (offlineConfig == null) {
            offlineConfig = Observable.just(OBJ);
        }
        localConfig
                // start timeER
                .timestamp()
                .flatMap((Function<Timed<Object>, ObservableSource<Timed<Object>>>) timedResult -> {
                    if (NetworkUtils.isConnected()) {
                        return delayAndResponseSkip(timedWrapper(onlineConfig, timedResult), delayMilli, skipSubject);
                    } else {
                        //No network
                        return delayAndResponseSkip(timedWrapper(offlineConfig, timedResult), delayMilli, skipSubject);
                    }
                })
                .map(Timed::value)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> processDisposable = disposable)
                .doOnError(onError)
                .doOnComplete(onComplete)
                .subscribe();
    }

    /**
     * If the timer should be skipped earlier
     */
    public void doSkip() {
        if (!doSkip) {
            doSkip = true;
            skipSubject.onNext(true);
        }
    }

    /**
     * Should be Called in onDestroy of the activity
     */
    void recycle() {
        if (processDisposable != null && !processDisposable.isDisposed()) {
            // cleaning up the rx life cycle
            processDisposable.dispose();
        }
    }

    /**
     * Delay after operation, and the delay period can be skipped
     *
     * @param observable the wrapped configuration
     * @param delayMilli    time to wait
     * @param skipObservable do we skip
     * @param <T>   type if Observable
     *
     * @return wrapped observable
     */
    private static <T> Observable<Timed<T>> delayAndResponseSkip(final Observable<Timed<T>> observable, final long delayMilli, final Observable<?> skipObservable) {
        return observable.flatMap((Function<Timed<T>, ObservableSource<Timed<T>>>) tTimed -> Observable.just(tTimed)
                .delay(tTimed1 -> {
                    // Time to get consumption
                    long waited = System.currentTimeMillis() - tTimed1.time();
                    Observable<T> result = Observable.just(tTimed1.value());
                    if (waited >= delayMilli) {
                        //It takes more than delayMilli
                        return result;
                    }
                    return result.delay(delayMilli - waited, TimeUnit.MILLISECONDS);
                }).takeUntil(skipObservable));
    }

    /**
     * Finishing time
     *
     * @param observable the configuration
     * @param timed      timed result
     * @param <T>        type if Observable
     *
     * @return wrapped observable
     */
    private static <T> Observable<Timed<T>> timedWrapper(final Observable<T> observable, final Timed<T> timed) {
        return observable.map(t -> new Timed<>(t, timed.time(), timed.unit()));
    }

    public static final class Builder {
        private Observable<Object> localConfig;
        private Observable<Object> onlineConfig;
        private Observable<Object> offlineConfig;
        private long delayMilli;

        Builder() {
        }

        /**
         * Local configuration
         *
         * @param localConfig config for splash activity
         * @return 'this' to be able to chain
         */
        Builder localConfig(@Nullable Observable<Object> localConfig) {
            this.localConfig = localConfig;
            return this;
        }

        /**
         * Required network request
         *
         * @param onlineConfig to do if there is internet
         * @return 'this' to be able to chain
         */
        Builder onlineConfig(@Nullable Observable<Object> onlineConfig) {
            this.onlineConfig = onlineConfig;
            return this;
        }


        /**
         * Offline configuration
         *
         * @param offlineConfig to do if there is no internet
         * @return 'this' to be able to chain
         */
        Builder offlineConfig(@Nullable Observable<Object> offlineConfig) {
            this.offlineConfig = offlineConfig;
            return this;
        }

        /**
         * Interface delay time
         *
         * @param delayMilli how long the splash will be shown in ms
         * @return 'this' to be able to chain
         */
        Builder delayMilli(long delayMilli) {
            this.delayMilli = delayMilli;
            return this;
        }

        public SplashLoader build() {
            return new SplashLoader(this);
        }
    }
}
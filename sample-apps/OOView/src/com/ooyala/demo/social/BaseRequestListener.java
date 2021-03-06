package com.ooyala.demo.social;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Skeleton base class for RequestListeners, providing default error
 * handling. Applications should handle these error conditions.
 */
public abstract class BaseRequestListener implements AsyncFacebookRunner.RequestListener {

    public void onFacebookError(FacebookError e, final Object state) {
        Log.e("Facebook", e.getMessage());
        DebugMode.logE(TAG, "Caught!", e);
    }

    public void onFileNotFoundException(FileNotFoundException e,
                                        final Object state) {
        Log.e("Facebook", e.getMessage());
        DebugMode.logE(TAG, "Caught!", e);
    }

    public void onIOException(IOException e, final Object state) {
        Log.e("Facebook", e.getMessage());
        DebugMode.logE(TAG, "Caught!", e);
    }

    public void onMalformedURLException(MalformedURLException e,
                                        final Object state) {
        Log.e("Facebook", e.getMessage());
        DebugMode.logE(TAG, "Caught!", e);
    }

}

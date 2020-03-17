package com.taocoder.dashout;

import android.app.Application;
import android.location.Geocoder;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Locale;

public class Controller extends Application {
    private static Controller controller;
    private static Geocoder geocoder;
    private static final String TAG = Controller.class.getSimpleName();
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        controller = this;
    }

    public static synchronized Controller getInstance() {
        return controller;
    }

    public Geocoder getGeocoder() {
        if (geocoder == null)
            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        return geocoder;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public <T> void addRequestQueue(Request<T> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        getRequestQueue().add(request);
    }

    public <T> void addRequestQueue(Request<T> request) {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequest(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
package com.iftalab.chuckjokes;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

/**
 * Created by Acer PC on 11/21/2016.
 */

public class IftaVolleyQueue {
    private static IftaVolleyQueue instance;
    private static Context context;
    private RequestQueue requestQueue;
    public IftaVolleyQueue(Context context)
    {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized IftaVolleyQueue getInstance(Context context) {
        if (instance == null) {
            instance = new IftaVolleyQueue(context);
        }
        return instance;
    }
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            Cache cache = new DiskBasedCache(context.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = new RequestQueue(cache, network);
            // Don't forget to start the volley request queue
            requestQueue.start();
        }
        return requestQueue;
    }
}

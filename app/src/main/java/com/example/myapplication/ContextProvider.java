package com.example.myapplication;

import android.content.Context;

import java.lang.ref.WeakReference;

public class ContextProvider {
    private static WeakReference<Context> contextWeakReference;

    public static void setContext(Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    public static Context getContext() {
        return contextWeakReference.get();
    }
}

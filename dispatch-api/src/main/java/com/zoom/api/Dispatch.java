package com.zoom.api;

import android.content.Context;

public class Dispatch {

    private static volatile Dispatch mDispatch;

    public static Dispatch getDefault() {
        if (null == mDispatch) {
            synchronized (Dispatch.class) {
                if (null == mDispatch) {
                    mDispatch = new Dispatch();
                }
            }
        }
        return mDispatch;
    }

    public void init(Context context) {
       DistributionCenter.getInstance().init(context);
    }

    public void navigation(String dispatch, Object event, DispatchListener mDispatchListener) {
        DistributionCenter.getInstance().navigation(dispatch, event, mDispatchListener);
    }
}

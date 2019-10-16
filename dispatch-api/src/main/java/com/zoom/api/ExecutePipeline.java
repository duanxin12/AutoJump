package com.zoom.api;

import android.util.Log;

import com.zoom.annotation.DispatchMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExecutePipeline implements Runnable{

    private static final String TAG = "ExecutePipeline";
    private Object event;
    private DispatchMeta dispatchMeta;
    private DispatchListener mDispatchListener;

    public ExecutePipeline(DispatchMeta dispatchMeta, Object event, DispatchListener mDispatchListener) {
        this.event = event;
        this.dispatchMeta = dispatchMeta;
        this.mDispatchListener = mDispatchListener;
    }

    @Override
    public void run() {
        if (mDispatchListener != null)
            mDispatchListener.startExcuteEvent(event);

        excute();

        if (mDispatchListener != null)
            mDispatchListener.finishExcuteEvent(event);
    }

    private void excute() {

        try {

            Class cls = Class.forName(dispatchMeta.getMethodAssociatedClass().getName());

            if (cls != null) {

                String methodName = dispatchMeta.getMethodName();

                if (methodName != null) {
                    Method mth = cls.getDeclaredMethod(methodName, event.getClass());
                    if (mth != null) {
                        mth.setAccessible(true);
                        mth.invoke(cls.newInstance(), event);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}

package com.zoom.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.zoom.annotation.DispatchMeta;
import com.zoom.compile.Constant;
import com.zoom.compile.IAutoDispatch;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DistributionCenter {

    private static final String TAG = "DistributionCenter";
    private static boolean registerByPlugin;
    private ExecutorService mExecutorService = Executors.newScheduledThreadPool(1);
    private Map<String, DispatchMeta> dispatchs;
    private static volatile DistributionCenter mDistributionCenter;

    public static DistributionCenter getInstance() {
        if (null == mDistributionCenter) {
            synchronized (DistributionCenter.class) {
                if (null == mDistributionCenter) {
                    mDistributionCenter = new DistributionCenter();
                }
            }
        }
        return mDistributionCenter;
    }

    public void init(Context context) {
        dispatchs = new HashMap<>();

        loadDispatchMap();
        if (registerByPlugin) {
            Log.w(TAG, "register by plugin!");
        } else {
            List<String> classNames = ClassUtils.scanFileNameByPackageName(context, Constant.PACKAGE_OF_GENERATE_FILE);

            if (classNames != null && classNames.size() > 0) {
                for (String className : classNames) {
                    register(className);
                }
            }
        }
    }

    private void loadDispatchMap() {
        registerByPlugin = false;
        /**
         * registerByPlugin(....)
         * registerByPlugin(....)
         * registerByPlugin(....)
         */
    }

    private void registerByPlugin(String className) {
        register(className);
        markRegisteredByPlugin();
    }

    /**
     * @param className
     */
    private void register(String className) {
        if (TextUtils.isEmpty(className)) {
            Log.w(TAG, className + " is not exit!");
        }

        try {
            Class<?> clazz = Class.forName(className);
            if (clazz != null) {
                Object obj = clazz.getConstructor().newInstance();

                if (obj instanceof IAutoDispatch) {
                    ((IAutoDispatch) obj).loadIntoDispatchs(dispatchs);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于标记是否通过插件加载数据
     */
    private void markRegisteredByPlugin() {
        if (!registerByPlugin) {
            registerByPlugin = true;
        }
    }

    public void navigation(String dispatch, Object event, DispatchListener mDispatchListener) {
        if (dispatchs == null || dispatch.length() == 0) {
            Log.w(TAG, "dispatchs is null, please call init() first!");
            return;
        }

        mExecutorService.submit(new ExecutePipeline(dispatchs.get(dispatch), event, mDispatchListener));
    }
}

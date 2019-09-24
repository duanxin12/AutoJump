package com.zoom.autojump;

import android.util.Log;

import com.zoom.annotation.AutoDispatch;

public class TestAutoDispatch {

    @AutoDispatch(dispatch = "/app/dispatch")
    public void testAutoDispatch(MainActivity.OrderEvent event) {
        Log.w("TestAutoDispatch", ((MainActivity.OrderEvent) event).data);
    }
}

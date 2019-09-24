package com.zoom.test;

import android.util.Log;

import com.zoom.annotation.AutoDispatch;


public class TestAutoDispatch {

    @AutoDispatch(dispatch = "/test/dispatch")
    public void testAutoDispatch(String str) {
        Log.w("TestAutoDispatch", str);
    }
}

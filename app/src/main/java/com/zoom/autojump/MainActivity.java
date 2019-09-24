package com.zoom.autojump;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zoom.api.Dispatch;
import com.zoom.api.DispatchListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 1000; i++) {
            Dispatch.getDefault().navigation("/test/dispatch", "hello" + i, new DispatchListener() {
                @Override
                public void startExcuteEvent(Object event) {
                    Log.w(TAG, "startExcuteEvent = " + System.currentTimeMillis());
                }

                @Override
                public void finishExcuteEvent(Object event) {
                    Log.w(TAG, "finishExcuteEvent = " + System.currentTimeMillis());
                }
            });
        }
    }

    class OrderEvent extends Event {
        String data;

        public OrderEvent(String data) {
            this.data = data;
        }
    }
}

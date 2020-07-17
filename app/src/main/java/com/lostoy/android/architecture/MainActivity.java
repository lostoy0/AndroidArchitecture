package com.lostoy.android.architecture;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lostoy.android.architecture.utils.Logger;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_RUN_SECONDS = 3;
    private int frameCount = 0;
    private long lastFrameNanos;
    private int seconds = 0;

    private Handler handle = new Handler();

    private ActivityManager activityManager;

    private TextView contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        contentTextView = findViewById(R.id.contentTextView);

        findViewById(R.id.clickMe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrack();
                handle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 10000; i++) {
                            Integer[] arr = new Integer[100000];
                        }
                    }
                }, 20);
            }
        });

        startTrack();
    }

    private void printMemory() {
        int pid = android.os.Process.myPid();
        Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(new int[]{pid});

        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();

        Logger.d("activity manager memory infos totalPss：" + memoryInfos[0].getTotalPss() / 1024f);
        Logger.d("runtime max memory：" + maxMemory / (1024*1024f));
        Logger.d("runtime total memory：" + totalMemory / (1024*1024f));
        Logger.d("runtime free memory：" + freeMemory / (1024*1024f));

        Logger.d("didi dokit getMemory: " + getMemoryData());

        StringBuilder builder = new StringBuilder();
        builder.append("activity manager memory infos totalPss：" + memoryInfos[0].getTotalPss() / 1024f).append("\n")
                .append("runtime max memory：" + maxMemory / (1024*1024f)).append("\n")
                .append("runtime total memory：" + totalMemory / (1024*1024f)).append("\n")
                .append("runtime free memory：" + freeMemory / (1024*1024f)).append("\n")
                .append("didi dokit getMemory: " + getMemoryData()).append("\n");
        contentTextView.setText(builder.toString());
    }

    private void startTrack() {
        seconds = 0;
        frameCount = 0;
        lastFrameNanos = 0;
        Choreographer.getInstance().postFrameCallback(frameCallback);
//        handle.postDelayed(fpsRunnable, 1000);
    }

    final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (lastFrameNanos <= 0) {
                lastFrameNanos = frameTimeNanos;
            } else {
                frameCount++;
                long frameCostMs = (long) ((frameTimeNanos - lastFrameNanos) * 0.000001f);
                Logger.d("------------");
                Logger.d("frameCount = " + frameCount + ", frameCostMs = " + frameCostMs);
                Logger.d("------------");
                printMemory();
                lastFrameNanos = frameTimeNanos;
            }
            if (frameCount < MAX_RUN_SECONDS) {
                Choreographer.getInstance().postFrameCallback(this);
            }
        }
    };

    final Runnable fpsRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d("================ frameCount = " + frameCount + " ================");
            frameCount = 0;
            seconds++;
//            if (frameCount < MAX_RUN_SECONDS) {
//                handle.postDelayed(this, 1000);
//            }
        }
    };

    private float getMemoryData() {
        float mem = 0.0F;
        try {
            Debug.MemoryInfo memInfo = null;
            //28 为Android P
            if (Build.VERSION.SDK_INT > 28) {
                // 统计进程的内存信息 totalPss
                memInfo = new Debug.MemoryInfo();
                Debug.getMemoryInfo(memInfo);
            } else {
                //As of Android Q, for regular apps this method will only return information about the memory info for the processes running as the caller's uid;
                // no other process memory info is available and will be zero. Also of Android Q the sample rate allowed by this API is significantly limited, if called faster the limit you will receive the same data as the previous call.

                Debug.MemoryInfo[] memInfos = activityManager.getProcessMemoryInfo(new int[]{Process.myPid()});
                if (memInfos != null && memInfos.length > 0) {
                    memInfo = memInfos[0];
                }
            }

            int totalPss = memInfo.getTotalPss();
            if (totalPss >= 0) {
                // Mem in MB
                mem = totalPss / 1024.0F;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mem;
    }

}

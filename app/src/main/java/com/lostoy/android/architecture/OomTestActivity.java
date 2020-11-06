package com.lostoy.android.architecture;

import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.yuanfudao.android.apm.util.ThreadUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OomTestActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String ERROR_HINT = "Error ! please input a number in upper EditText First";
    public static final float UNIT_M = 1024 * 1024;
    private TextView dashboard;
    private EditText etDigtal;
    private int digtal = -1;
    private List<byte[]> heap = new ArrayList<>();
    private Set<BufferedReader> readers = new HashSet<>();
    private Runnable increaseFDRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/status"));
                readers.add(br);
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    };
    private Runnable emptyRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oom_test_second);
        dashboard = (TextView) findViewById(R.id.tv_dashboard);
        etDigtal = (EditText) findViewById(R.id.et_digtal);
        findViewById(R.id.bt1).setOnClickListener(this);
        findViewById(R.id.bt2).setOnClickListener(this);
        findViewById(R.id.bt3).setOnClickListener(this);
        findViewById(R.id.bt4).setOnClickListener(this);
        findViewById(R.id.bt5).setOnClickListener(this);
        findViewById(R.id.bt6).setOnClickListener(this);
        findViewById(R.id.bt7).setOnClickListener(this);
        findViewById(R.id.bt8).setOnClickListener(this);
        findViewById(R.id.threadLimits).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        try {
            digtal = Integer.valueOf(etDigtal.getText().toString());
            readers.clear();
        } catch (Exception e) {
            digtal = -1;
        }
        switch (view.getId()) {
            case R.id.bt1:
//                showFileContent("/proc/" + Process.myPid() + "/limits");
                getFdCountLimits();
                break;
            case R.id.bt2:
                if (digtal <= 0) {
                    dashboard.setText(ERROR_HINT);
                } else {
                    for (int i = 0; i < digtal; i++) {
                        new Thread(increaseFDRunnable).start();
                    }
                }
                break;
            case R.id.bt3:
                File fdFile = new File("/proc/" + Process.myPid() + "/fd");
                File[] files = fdFile.listFiles();
                if (files != null) {
                    dashboard.setText("current FD numbler is " + files.length);
                } else {
                    dashboard.setText("/proc/pid/fd is empty ");
                }
                break;
            case R.id.bt4:
//                showFileContent("/proc/" + Process.myPid() + "/status");
                getThreadCount();
                break;
            case R.id.bt5:
                if (digtal <= 0) {
                    dashboard.setText(ERROR_HINT);
                } else {
                    for (int i = 0; i < digtal; i++) {
                        new Thread(emptyRunnable).start();
                    }
                }
                break;
            case R.id.bt6:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Java Heap Max : ").append(Runtime.getRuntime().maxMemory() / UNIT_M).append(" MB\r\n");
                stringBuilder.append("Current used  : ").append((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / UNIT_M).append(" MB\r\n");
                dashboard.setText(stringBuilder.toString());
                break;
            case R.id.bt7:
                if (digtal <= 0) {
                    dashboard.setText(ERROR_HINT);
                } else {
                    byte[] bytes = new byte[digtal];
                    heap.add(bytes);
                }
                break;
            case R.id.bt8:
                heap = new ArrayList<>();
                System.gc();
                break;
            case R.id.threadLimits:
                // no permission
                showFileContent("/proc/sys/kernel/threads-max");
                break;
        }
    }

    private void showFileContent(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
            StringBuilder stringBuilder = new StringBuilder();
            String s;
            while ((s = randomAccessFile.readLine()) != null) {
                stringBuilder.append(s).append("\r\n");
            }
            dashboard.setText(stringBuilder.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer getThreadCount() {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile("/proc/" + Process.myPid() + "/status", "r");
            StringBuilder stringBuilder = new StringBuilder();
            String s;
            while ((s = randomAccessFile.readLine()) != null) {
                stringBuilder.append(s).append("\r\n");
            }
            stringBuilder.append("\n").append("threadCount_active: ").append(Thread.activeCount()).append("\r\n");
            stringBuilder.append("\n").append("threadCount_active_2: ").append(ThreadUtil.getThreadCount()).append("\r\n");
            stringBuilder.append("\n").append("threadCount2: ").append(getThreadCount2()).append("\r\n");
            dashboard.setText(stringBuilder.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private Integer getThreadCount2() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;
        // 遍历线程组树，获取根线程组
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        // 激活的线程数再加一倍，防止枚举时有可能刚好有动态线程生成
        int slackSize = topGroup.activeCount() * 2;
        Thread[] slackThreads = new Thread[slackSize];
        // 获取根线程组下的所有线程，返回的actualSize便是最终的线程数
        int actualSize = topGroup.enumerate(slackThreads);
        Thread[] atualThreads = new Thread[actualSize];
        // 复制slackThreads中有效的值到atualThreads
        System.arraycopy(slackThreads, 0, atualThreads, 0, actualSize);
        System.out.println("Threads size is " + atualThreads.length);
        for (Thread thread : atualThreads) {
            System.out.println("Thread name : " + thread.getName());
        }
        return actualSize;
    }

    private Integer getFdCountLimits() {
        String result = "0";
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile("/proc/" + Process.myPid() + "/limits", "r");
            StringBuilder stringBuilder = new StringBuilder();
            String s;
            String[] array;
            while ((s = randomAccessFile.readLine()) != null) {
                stringBuilder.append(s).append("\r\n");
                array = s.split("\\s+");
                Log.e("raymond", new Gson().toJson(array));
                if (s.startsWith("Max open files")) {
                    result = array[3];
                }
            }
            stringBuilder.append("\n\n").append("---------------fd max limits: ").append(result);
            dashboard.setText(stringBuilder.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(result);
    }
}

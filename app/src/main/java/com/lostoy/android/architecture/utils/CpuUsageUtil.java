package com.lostoy.android.architecture.utils;

import android.text.TextUtils;

import com.yuanfudao.android.apm.demo.utils.CpuFreqFile;import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CpuUsageUtil {

    private static final String SYS_CPU_PATH = "/sys/devices/system/cpu/";

    public static String getProcessCpuFreqRate() {
        Integer cores = getAvailableCpuCores(); // 该进程可用 CPU 核心
        if (cores <= 0) {
            return "";
        }

        List<Long> curFreqs = getAvailableCpuCoreFreqs(cores, CpuFreqFile.SCALING_CUR_FREQ); // 该进程可用核心的当前频率
        if (curFreqs.size() == 0) {
            return "";
        }

        List<Long> maxFreqs = getAvailableCpuCoreFreqs(cores, CpuFreqFile.CPUINFO_MAX_FREQ);

//        Log.e("raymond", "------------ freq cores " + cores + " size " + freqs.size() + " -------------\n");

        float weight = (float) 1 / curFreqs.size();
        float rate = 0;
        for (int i = 0; i < cores; i++) {
            long freq = curFreqs.get(i);
            long maxFreq = maxFreqs.get(i); // 单个可用 CPU 核心能达到的最大频率
//            Log.e("raymond", i + " max: " + maxFreq + " cur: " + freq);
            rate += 100 * weight * (float) freq / maxFreq;
        }

        return rate + "%";
    }

    public static List<Long> getAvailableCpuCoreFreqs(int cores, String freqFile) {
        List<Long> freqs = new ArrayList<>();
        for (int i = 0; i < cores; i++) {
            freqs.add(getCpuFreqByFReader(i, freqFile));
        }
        return freqs;
    }

    private static Integer getAvailableCpuCores() {
        int cores;
        try {
            cores = Objects.requireNonNull(new File(SYS_CPU_PATH).listFiles(CPU_FILTER)).length;
        } catch (SecurityException | NullPointerException e) {
            cores = -1;
        }

        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            //regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };

    /*
     * 计算某个时间段内AppCpuTime与TotalCpuTime的变化，然后按照比例换算成该应用的Cpu使用率。

     * Android系统本省也有一个类是用来显示Cpu使用率的：

     * android/system/frameworks/base/packages/SystemUI/src/com/android/systemui/LoadAverageService.java
     * 阅读源码发现也是读取/proc目录下的文件来计算Cpu使用率
     * */
    public static float getProcessCpuRate() {

        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getProcessCpuTime(android.os.Process.myPid());
        try {
            Thread.sleep(20);//360太大会卡顿UI
        } catch (Exception e) {
        }

        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getProcessCpuTime(android.os.Process.myPid());

        float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);

        return cpuRate;
    }

    public static long getTotalCpuTime() { // 获取系统总CPU使用时间
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assert cpuInfos != null;
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    public static long getProcessCpuTime(int pid) { // 获取应用占用的CPU时间
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }

    public static Long getCpuFreqByCatCmd(int cpuIndex, String filename) {
        Long result = -1L;
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu" + cpuIndex + "/cpufreq/" + filename};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            StringBuilder temp = new StringBuilder();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                temp.append(new String(re));
            }
            if (!TextUtils.isEmpty(temp.toString().trim())) {
//                Log.e("raymond", "----------- cat " + filename + " ----------\n" + temp.toString().trim());
                result = Long.valueOf(temp.toString().trim());
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Long getCpuFreqByFReader(int cpuIndex, String filename) {
        Long result = -1L;
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu" + cpuIndex + "/cpufreq/" + filename);
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            if (!TextUtils.isEmpty(text.trim())) {
//                Log.e("raymond", "----------- reader " + filename + " ----------\n" + text.trim());
                result = Long.valueOf(text.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }
}

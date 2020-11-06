package com.lostoy.android.architecture.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MemoryUtils {

    public static long getTotalMem() {
        try {
            FileReader fr = new FileReader("/proc/meminfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split("\\s+");
            // 单位为KB
            return Long.parseLong(array[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

}

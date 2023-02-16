package com.zeewain.base.config;

import android.os.Environment;

public class LogFileConfig {
    public final static String directoryPath = Environment.getExternalStorageDirectory() + "/crash/";
    public final static int maxSize = 10;
}

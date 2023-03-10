package com.zwn.launcher.host;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

import com.qihoo360.replugin.RePlugin;

public class HostManager {
    private volatile static IHostManager hostManager;
    private static volatile SharedPreferences hostSp;

    public static IHostManager getHostManager() {
        if (hostManager == null) {
            synchronized (HostManager.class) {
                if (hostManager == null) {
                    IBinder iBinder = RePlugin.getGlobalBinder("HostManager");
                    hostManager = IHostManager.Stub.asInterface(iBinder);
                }
            }
        }
        return hostManager;
    }

    public static SimulateHostManager getSimulateHostManager() {
        return new SimulateHostManager();
    }

    public static SharedPreferences getHostSp() {
        if (hostSp == null) {
            synchronized (HostManager.class) {
                if (hostSp == null) {
                    hostSp = RePlugin.getHostContext().getSharedPreferences("spUtils", Context.MODE_PRIVATE);
                }
            }
        }
        return hostSp;
    }

    public static void installPlugin(String fileId) {
        try {
            if (BuildConfig.FLAVOR == "plugin")
                getHostManager().installPlugin(fileId);
            else getSimulateHostManager().installPlugin(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getInstallingFileId() {
        try {
            if (BuildConfig.FLAVOR == "plugin")
                return getHostManager().getInstallingFileId();
            else return getSimulateHostManager().getInstallingFileId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLastPluginPackageName() {
        try {
            if (BuildConfig.FLAVOR == "plugin")
                return getHostManager().getLastPluginPackageName();
            else return getSimulateHostManager().getLastPluginPackageName();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastPluginPackageName(String packageName) {
        try {
            if (BuildConfig.FLAVOR == "plugin")
                getHostManager().setLastPluginPackageName(packageName);
            else getSimulateHostManager().setLastPluginPackageName(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getLastUnzipDonePlugin() {
        try {
            if (BuildConfig.FLAVOR == "plugin")
                return getHostManager().getLastUnzipDonePlugin();
            else return getSimulateHostManager().getLastUnzipDonePlugin();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastUnzipDonePlugin(String fileId) {
        try {
            getHostManager().setLastUnzipDonePlugin(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getHostSpString(String key, String defValue) {
        if (BuildConfig.FLAVOR == "plugin") {
            return getHostSp().getString(key, defValue);
        } else {
            if ("UserToken".equals(key)) {
                return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ6ZWV3YWluIiwiYXBwQ29kZSI6Im1hbGxfdW1zIiwidXNlcklkIjoxODk4MzI1NjMwODE0MzMwOTUsInVzZXJDb2RlIjoiYWlib3hfcmtfMDAxIiwiZXhwaXJlVGltZSI6MTY3ODUxNzIwNzMwMiwidXNlclR5cGUiOjIsImV4cCI6MTY3ODUxNzIwN30.G25mrCrW_VaYxwVsHHizHPUMPaiaVPB5MTeo1ulR0sE";
            } else if ("PlatformInfo".equals(key)) {
                return "AndroidRockchipTVAIIP/1.3.2 (ZWN_AIIP_003 1.0; Android 12)";
            } else if ("BaseUrl".equals(key)) {
                return "https://www.zeewain.com";
            } else if ("BasePath".equals(key)) {
                return "/aiip-debug/api";
            } else {
                return null;
            }
        }
    }

    public static void logoutClear() {
        try {
            if (BuildConfig.FLAVOR == "plugin")
                getHostManager().logoutClear();
            else getSimulateHostManager().logoutClear();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void gotoLoginPage(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(RePlugin.getHostContext().getPackageName(), "com.zee.launcher.login.ui.LoginActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static Context getUseContext(Context context) {
        if (BuildConfig.FLAVOR == "plugin") {
            return RePlugin.getHostContext();
        } else return context;
    }

    public static boolean isGestureAiEnable(){
        if (BuildConfig.FLAVOR == "plugin") {
            try {
                return getHostManager().isGestureAiEnable();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void startGestureAi(boolean withActive){
        try {
            getHostManager().startGestureAi(withActive);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void stopGestureAi(){
        try {
            getHostManager().stopGestureAi();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGestureAIActive(){
        try {
            return getHostManager().isGestureAIActive();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return false;
    }

    static class SimulateHostManager {

        void installPlugin(String fileId) throws RemoteException {
        }

        String getInstallingFileId() throws RemoteException {
            return null;
        }

        String getLastPluginPackageName() throws RemoteException {
            return null;
        }

        void setLastPluginPackageName(String packageName) throws RemoteException {
        }

        String getLastUnzipDonePlugin() throws RemoteException {
            return null;
        }

        void setLastUnzipDonePlugin(String fileId) throws RemoteException {
        }

        void logoutClear() throws RemoteException {
        }

        void gotoLoginPage() {
        }
    }
}

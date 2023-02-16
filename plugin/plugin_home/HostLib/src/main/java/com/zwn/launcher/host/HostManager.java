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

    public static IHostManager getHostManager(){
        if(hostManager == null){
            synchronized (HostManager.class){
                if(hostManager == null){
                    IBinder iBinder = RePlugin.getGlobalBinder("HostManager");
                    hostManager = IHostManager.Stub.asInterface(iBinder);
                }
            }
        }
        return hostManager;
    }

    /*public static SimulateHostManager getHostManager(){
        return new SimulateHostManager();
    }*/

    public static SharedPreferences getHostSp(){
        if(hostSp == null){
            synchronized (HostManager.class){
                if(hostSp == null){
                    hostSp = RePlugin.getHostContext().getSharedPreferences("spUtils", Context.MODE_PRIVATE);
                }
            }
        }
        return hostSp;
    }

    public static void installPlugin(String fileId){
        try {
            getHostManager().installPlugin(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getInstallingFileId(){
        try {
            return getHostManager().getInstallingFileId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLastPluginPackageName(){
        try {
            return getHostManager().getLastPluginPackageName();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastPluginPackageName(String packageName){
        try {
            getHostManager().setLastPluginPackageName(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getLastUnzipDonePlugin(){
        try {
            return getHostManager().getLastUnzipDonePlugin();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastUnzipDonePlugin(String fileId){
        try {
            getHostManager().setLastUnzipDonePlugin(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getHostSpString(String key, String defValue){
        /*if("UserToken".equals(key)){
            return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ6ZWV3YWluIiwiYXBwQ29kZSI6Im1hbGxfdW1zIiwidXNlcklkIjoxNTI3Njk0MTI2MzkwNzYzNTksInVzZXJDb2RlIjoiend5X3Rlc3RfMDAxIiwiZXhwaXJlVGltZSI6MTY3NjUzNjEyNzcwNywidXNlclR5cGUiOjIsImV4cCI6MTY3NjUzNjEyN30._M4XqFHrqofumj-RNiHl-HKvWobIRczgb1lcW1sYu6s";
        }else if("PlatformInfo".equals(key)){
            return "AndroidRockchipTVAIIP/1.2.3 (ZWN_AIIP_003 1.0; Android 12)";
        }else if("BaseUrl".equals(key)){
            return "https://www.zeewain.com";
        }else if("BasePath".equals(key)){
            return "/aiip-debug/api";
        }else{
            return null;
        }*/
        return getHostSp().getString(key, defValue);
    }

    public static void logoutClear(){
        try {
            getHostManager().logoutClear();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void gotoLoginPage(Context context){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(RePlugin.getHostContext().getPackageName(), "com.zee.launcher.login.ui.LoginActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static Context getUseContext(Context context){
        return RePlugin.getHostContext();
        //return context;
    }

    static class SimulateHostManager{

        void installPlugin(String fileId) throws RemoteException {}

        String getInstallingFileId() throws RemoteException {
            return null;
        }

        String getLastPluginPackageName() throws RemoteException {
            return null;
        }

        void setLastPluginPackageName(String packageName) throws RemoteException {}

        String getLastUnzipDonePlugin() throws RemoteException {
            return null;
        }

        void setLastUnzipDonePlugin(String fileId) throws RemoteException {}

        void logoutClear() throws RemoteException {}

        void gotoLoginPage(){}
    }
}

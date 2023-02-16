package com.zeewain.base.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.core.view.ViewCompat;

import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;

import java.io.File;

public class CommonUtils {

    public static String getFileUsePath(String fileId, String version, int type, Context context){
        if(type == BaseConstants.DownloadFileType.HOST_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.MANAGER_APP || type == BaseConstants.DownloadFileType.SETTINGS_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.PLUGIN_APP){
            return context.getExternalCacheDir().getPath() + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.HOST_PLUGIN_PKG){
            return context.getFilesDir().getPath() + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.SHARE_LIB){
            return context.getFilesDir().getPath() + "/" + fileId + "_" + version + ".zip";
        }else{
            return fileId;
        }
    }

    public static String getModelStorePath(String modelFileName, Context context){
        String path = context.getFilesDir().getPath() + "/models";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        return path + "/" + modelFileName;
    }

    public static boolean createOrClearPluginModelDir(){
        File file = new File(BaseConstants.PLUGIN_MODEL_PATH);
        if(!file.exists()){
            return file.mkdirs();
        }else {
            File[] files = file.listFiles();
            for(File tmpFile : files){
                if (tmpFile.isFile()){
                    tmpFile.delete();
                }
            }
        }
        return true;
    }

    public static boolean isUserLogin(){
        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
        if(userToken != null && !userToken.isEmpty()){
            return true;
        }
        return false;
    }

    public static void savePluginCareInfo(){
        SPUtils.getInstance().put(SharePrefer.platformInfo, BaseApplication.platformInfo);
        SPUtils.getInstance().put(SharePrefer.baseUrl, BaseConstants.baseUrl);
        SPUtils.getInstance().put(SharePrefer.basePath, BaseConstants.basePath);
        SPUtils.getInstance().put(SharePrefer.DeviceSN, CommonUtils.getDeviceSn());
    }

    public static void logoutClear(){
        SPUtils.getInstance().remove(SharePrefer.userToken);
        SPUtils.getInstance().remove(SharePrefer.userAccount);
        SPUtils.getInstance().remove(SharePrefer.akSkInfo);
        CommonVariableCacheUtils.getInstance().token = "";
    }

    public static void scaleView(View view, float scale){
        ViewCompat.animate(view)
                .setDuration(200)
                .scaleX(scale)
                .scaleY(scale)
                .start();
    }

    public static String getDeviceSn(){
        //return "1111020123010001";
        //return "HD13213213223213213";
        return SystemProperties.get("ro.serialno");
    }

    public static String getHardwarePlatformInfo(){
        if("amlogic".equals(Build.HARDWARE)){
            return "AndroidAmlogicTVAIIP";
        }else if("rk30board".equals(Build.HARDWARE)){
            return "AndroidRockchipTVAIIP";
        }
        return "AndroidTVAIIP";
    }

    public static void startSettingsActivity(Context context){
        try {
            Intent intent = new Intent();
            intent.setAction(BaseConstants.ZEE_SETTINGS_ACTIVITY_ACTION);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

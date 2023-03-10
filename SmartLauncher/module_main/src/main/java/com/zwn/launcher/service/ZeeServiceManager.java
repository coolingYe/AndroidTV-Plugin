package com.zwn.launcher.service;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.zee.manager.IZeeManager;
import com.zeewain.ai.ZeeAiManager;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.FileUtils;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.protocol.response.UpgradeResp;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.AppLibInfo;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ZeeServiceManager {
    private static final String TAG = "ZeeService";
    private static Context appContext;
    private CareBroadcastReceiver careBroadcastReceiver;
    private static final ExecutorService mFixedPool = Executors.newFixedThreadPool(1);
    public static String installingFileId = null;
    public static String lastUnzipDonePlugin = null;
    public static String lastPluginPackageName = null;
    public static final ConcurrentLinkedQueue<String> installingQueue = new ConcurrentLinkedQueue<>();
    private static final List<DownloadListener> downloadListenerList = new ArrayList<>();
    private static volatile ZeeServiceManager instance;

    public static void init(Context context){
        if(instance == null){
            synchronized (ZeeServiceManager.class){
                if(instance == null){
                    instance = new ZeeServiceManager(context);
                }
            }
        }
    }

    public static ZeeServiceManager getInstance() {
        return instance;
    }

    private ZeeServiceManager(Context context){
        appContext = context.getApplicationContext();
        registerBroadCast();
        scheduleNextPing(appContext);
    }

    public void registerDownloadListener(DownloadListener downloadListener){
        downloadListenerList.add(downloadListener);
    }

    public void unRegisterDownloadListener(DownloadListener downloadListener){
        downloadListenerList.remove(downloadListener);
    }

    public DownloadService.DownloadBinder downloadBinder = null;

    public DownloadService.DownloadBinder getDownloadBinder() {
        return downloadBinder;
    }

    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onProgress(fileId, progress, loadedSize, fileSize);
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onSuccess(fileId, type, file);
            }
            if(BaseConstants.DownloadFileType.PLUGIN_APP == type || BaseConstants.DownloadFileType.MANAGER_APP == type
                    || BaseConstants.DownloadFileType.SETTINGS_APP == type || BaseConstants.DownloadFileType.ZEE_GESTURE_AI_APP == type){
                addToQueueNextCheckInstall(fileId);
            }else if(BaseConstants.DownloadFileType.HOST_APP == type){
                handleHostInstall(file.getPath(), fileId);
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onFailed(fileId, type, code);
            }
        }

        @Override
        public void onPaused(String fileId) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onPaused(fileId);
            }
        }

        @Override
        public void onCancelled(String fileId) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onCancelled(fileId);
            }
        }

        @Override
        public void onUpdate(String fileId) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onUpdate(fileId);
            }
        }
    };

    public static void addToQueueNextCheckInstall(String fileId){
        installingQueue.offer(fileId);
        handleCommonApkInstall();
    }

    public static boolean isInQueue(String fileId){
        return installingQueue.contains(fileId);
    }

    private synchronized static void handleCommonApkInstall(){
        if(installingFileId == null){
            installingFileId = installingQueue.poll();
            Log.i(TAG, "handleCommonApkInstall() prepare for installation fileId=" + installingFileId);
            if(installingFileId != null){
                final DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(installingFileId);
                if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS &&  FileUtils.isFileExist(downloadInfo.filePath)){
                    mFixedPool.execute(() -> {
                        AppLibInfo appLibInfo = new AppLibInfo(downloadInfo.fileId, downloadInfo.mainClassPath);
                        boolean addResult = CareController.instance.addAppLibInfo(appLibInfo);
                        if(!addResult){
                            CareController.instance.updateAppLibInfo(appLibInfo);
                        }

                        //used for third party app default enable all Permission
                            /*String pkgNames = SystemProperties.get(BaseConstants.PERSIST_SYS_PERMISSION_PKG);
                            if(!TextUtils.isEmpty(pkgNames)){
                                SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, pkgNames + ";" + downloadInfo.mainClassPath);
                            }else{
                                SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, downloadInfo.mainClassPath);
                            }*/
                        Intent intent = new Intent();
                        intent.setAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
                        intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, downloadInfo.fileId);
                        intent.putExtra(BaseConstants.EXTRA_INSTALLED_PACKAGE_NAME, downloadInfo.mainClassPath);
                        intent.putExtra(BaseConstants.EXTRA_INSTALLED_APP_TYPE, downloadInfo.type);
                        intent.putExtra(BaseConstants.EXTRA_PLUGIN_FILE_PATH, downloadInfo.filePath);
                        intent.setPackage(appContext.getPackageName());

                        Log.i(TAG, "handleCommonApkInstall() prepare install fileId = " + downloadInfo.fileId);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE );
                        IntentSender statusReceiver = pendingIntent.getIntentSender();
                        boolean success = ApkUtil.installApkSession(appContext, downloadInfo.filePath, statusReceiver);
                        if (!success) {
                            Log.e(TAG, "handleCommonApkInstall() failed to install " + installingFileId);
                            installingFileId = null;
                            handleCommonApkInstall();
                        }
                    });
                }else{
                    installingFileId = null;
                    handleCommonApkInstall();
                }
            }else{
                Log.i(TAG, "handleCommonApkInstall() install app done!");
            }
        } else{
            Log.i(TAG, "handleCommonApkInstall() fileId=" + installingFileId + " is installing!");
        }
    }

    public void handleHostInstall(String hostApkPath, String fileId){
        Intent intent = new Intent(BaseConstants.MANAGER_INSTALLED_ACTION);
        ComponentName componentName = new ComponentName(BaseConstants.MANAGER_PACKAGE_NAME, BaseConstants.MANAGER_INSTALL_ACTIVITY);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BaseConstants.EXTRA_APK_PATH, hostApkPath);
        intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, fileId);
        intent.putExtra(BaseConstants.EXTRA_DONE_TO_PACKAGE_NAME, appContext.getPackageName());
        intent.putExtra(BaseConstants.EXTRA_DONE_TO_CLASS_NAME, BaseConstants.DONE_TO_CLASS_NAME);
        appContext.startActivity(intent);
    }

    private static void checkRebootPluginAppLib(Context context) {
        List<AppLibInfo> appLibInfoList = CareController.instance.getInstalledAppLibInfo();
        if(appLibInfoList.size() > 0){
            PackageManager packageManager = context.getPackageManager();
            for (AppLibInfo appLibInfo : appLibInfoList){
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(appLibInfo.packageName, 0);
                    File libMainFile = new File(packageInfo.applicationInfo.nativeLibraryDir + "/" + AppLibInfo.LIB_NAME);
                    if(libMainFile.exists()){
                        String fileMD5 = FileUtils.file2MD5(libMainFile);
                        Log.i(TAG, "checkRebootPluginAppLib() packageName=" + appLibInfo.packageName + ", libMainFile md5=" + fileMD5);
                        if(appLibInfo.libMd5.equals(fileMD5)){
                            //that's nice;
                        }else{
                            Log.w(TAG, libMainFile.getAbsolutePath() + " is " + fileMD5 + ", but DB is " + appLibInfo.libMd5);
                            DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(appLibInfo.fileId);
                            if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                                File apkFile = new File(downloadInfo.filePath);
                                if(apkFile.exists()){
                                    if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(apkFile))) {
                                        addToQueueNextCheckInstall(downloadInfo.fileId);
                                    }else{
                                        if(apkFile.delete() && CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_PENDING) > 0){
                                            if(getInstance().getDownloadBinder() != null){
                                                getInstance().getDownloadBinder().startDownload(downloadInfo.fileId);
                                            }
                                        }else{
                                            CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                                        }
                                    }
                                }else if(FileUtils.isFileExist(packageInfo.applicationInfo.sourceDir)){
                                    File baseApk = new File(packageInfo.applicationInfo.sourceDir);
                                    if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(baseApk))) {
                                        com.qihoo360.replugin.utils.FileUtils.moveFile(new File(packageInfo.applicationInfo.sourceDir), new File(downloadInfo.filePath));
                                        addToQueueNextCheckInstall(downloadInfo.fileId);
                                    }else{
                                        CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                                    }
                                }else{
                                    CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                                }
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void checkPluginAppRelay(Context context){
        List<DownloadInfo> downloadInfoList = CareController.instance.getAllDownloadInfo("(status=" + DownloadInfo.STATUS_SUCCESS + " and type<0)");
        Log.e(TAG, "checkPluginAppRelay()  downloadInfoList.size()=" + downloadInfoList.size());
        for(DownloadInfo downloadInfo: downloadInfoList){
            if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                File downloadedFile = new File(downloadInfo.filePath);
                if(downloadedFile.exists()){
                    String fileMD5 = FileUtils.file2MD5(downloadedFile);
                    if(!downloadInfo.packageMd5.equals(fileMD5)){
                        Log.e(TAG, "checkPluginAppRelay() file md5 not match! " + downloadInfo.filePath);
                        if(downloadedFile.delete()){
                            CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_PENDING);
                        }else {
                            CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_STOPPED);
                        }
                    }
                }else{
                    Log.e(TAG, "checkPluginAppRelay() file lost " + downloadInfo.filePath);
                    CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_PENDING);
                }
            }
        }

        File licenseFile = new File(BaseConstants.LICENSE_V2_FILE_PATH);
        if(licenseFile.exists()){
            if(licenseFile.length() == 0){
                licenseFile.delete();
            }else{
                boolean isNullContent = FileUtils.isNullContentFile(licenseFile, 3*1024);
                if(isNullContent){
                    licenseFile.delete();
                }
            }
        }


        checkRebootPluginAppLib(context);
    }

    private final ServiceConnection downloadServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            if (downloadBinder != null) {
                downloadBinder.registerDownloadListener(downloadListener);
            }

            List<DownloadInfo> downloadInfoList = CareController.instance.getLatestPendingList();
            if(downloadInfoList.size() > 0){
                downloadBinder.startDownload(downloadInfoList.get(0));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    public void bindDownloadService(Context context) {
        Intent bindIntent = new Intent(context, DownloadService.class);
        context.bindService(bindIntent, downloadServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
        intentFilter.addAction(ACTION_CARE_PING);
        careBroadcastReceiver = new CareBroadcastReceiver();
        appContext.registerReceiver(careBroadcastReceiver, intentFilter);
    }

    private void unRegisterBroadCast(){
        appContext.unregisterReceiver(careBroadcastReceiver);
    }

    static class CareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BaseConstants.PACKAGE_INSTALLED_ACTION.equals(intent.getAction())){
                Bundle extras = intent.getExtras();
                int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                final String pluginName = extras.getString(BaseConstants.EXTRA_PLUGIN_NAME);
                if(lastUnzipDonePlugin != null){
                    if(lastUnzipDonePlugin.equals(pluginName)){
                        lastUnzipDonePlugin = null;
                    }
                }
                Log.i(TAG, "onReceive install action, install status=" + status + ", pluginName=" + pluginName);
                if(PackageInstaller.STATUS_SUCCESS == status){
                    int appType = extras.getInt(BaseConstants.EXTRA_INSTALLED_APP_TYPE, -10000);
                    if(appType == BaseConstants.DownloadFileType.PLUGIN_APP){
                        String pluginPackageName = extras.getString(BaseConstants.EXTRA_INSTALLED_PACKAGE_NAME);
                        String pluginFilePath = extras.getString(BaseConstants.EXTRA_PLUGIN_FILE_PATH);
                        if(pluginPackageName != null && pluginFilePath != null){
                            PackageManager packageManager = context.getPackageManager();
                            try {
                                PackageInfo packageInfo = packageManager.getPackageInfo(pluginPackageName, 0);
                                File libMainFile = new File(packageInfo.applicationInfo.nativeLibraryDir + "/" + AppLibInfo.LIB_NAME);
                                if(libMainFile.exists()){
                                    String fileMD5 = FileUtils.file2MD5(libMainFile);
                                    Log.i(TAG, "onReceive install action, pluginPackageName=" + pluginPackageName + ", libMainFile md5=" + fileMD5);
                                    if(!fileMD5.isEmpty()){
                                        AppLibInfo appLibInfo = new AppLibInfo();
                                        appLibInfo.fileId = pluginName;
                                        appLibInfo.libPath = libMainFile.getAbsolutePath();
                                        appLibInfo.libMd5 = fileMD5;
                                        appLibInfo.packageName = pluginPackageName;
                                        appLibInfo.status = AppLibInfo.STATUS_INSTALLED;
                                        appLibInfo.saveTime = System.currentTimeMillis();
                                        int updateResult = CareController.instance.updateAppLibInfo(appLibInfo);
                                        if(updateResult > 0){
                                            FileUtils.deleteFile(pluginFilePath);
                                        }
                                    }else{
                                        FileUtils.deleteFile(pluginFilePath);
                                    }
                                }else{
                                    FileUtils.deleteFile(pluginFilePath);
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                                Log.e(TAG, "handleCommonApkInstall() NameNotFoundException=" + pluginPackageName);
                                FileUtils.deleteFile(pluginFilePath);
                            }
                        }
                    }else{
                        String pluginFilePath = extras.getString(BaseConstants.EXTRA_PLUGIN_FILE_PATH);
                        FileUtils.deleteFile(pluginFilePath);

                        if(BaseConstants.ZEE_GESTURE_AI_APP_SOFTWARE_CODE.equals(pluginName)){
                            sendGestureAiServiceCheck(context);
                        }
                    }

                    installingFileId = null;
                    handleCommonApkInstall();
                }else{
                    installingFileId = null;
                    handleCommonApkInstall();
                }
                /*if(installingMap.size() == 0){
                    SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, "");
                }*/
            }else if(ACTION_CARE_PING.equals(intent.getAction())) {
                scheduleNextPing(context);
                if(CommonUtils.isUserLogin())
                    updateDeviceHealth();
            }
        }
    }

    private static void sendGestureAiServiceCheck(Context context){
        Intent intent = new Intent();
        intent.setAction(BaseConstants.GESTURE_AI_SERVICE_CHECK_ACTION);
        context.sendBroadcast(intent);
    }

    private static void updateDeviceHealth(){
        DataRepository.getInstance().getDeviceHealth(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {}

                    @Override
                    public void onError(@NonNull Throwable e) {}

                    @Override
                    public void onComplete() {}
                });
    }

    public boolean handleManagerAppUpgrade(final UpgradeResp upgradeResp){
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.MANAGER_APP_SOFTWARE_CODE);
        if(downloadInfo != null){
            if(downloadInfo.version.equals(upgradeResp.getSoftwareVersion())){//mean already add
                if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()){
                        if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(file))) {
                            if (!isInQueue(downloadInfo.fileId)) {
                                addToQueueNextCheckInstall(downloadInfo.fileId);
                            }
                        }else{
                            if(file.delete() && CareController.instance.deleteDownloadInfo(downloadInfo.fileId) > 0){
                                return downloadBinder.startDownload(downloadInfo);
                            }else{
                                Log.e(TAG, "ZeeManager APK file damage, clear failed!");
                                return false;
                            }
                        }
                    }else{//something wrong? the file removed or same version update?
                        CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                        return downloadBinder.startDownload(downloadInfo);
                    }
                }else {
                    return downloadBinder.startDownload(downloadInfo);
                }
            }else{//old version in db
                return downloadBinder.startDownload(DownloadHelper.buildManagerUpgradeDownloadInfo(appContext, upgradeResp));
            }
        }else{
            return downloadBinder.startDownload(DownloadHelper.buildManagerUpgradeDownloadInfo(appContext, upgradeResp));
        }
        return true;
    }

    public boolean handleCommonAppUpgrade(final UpgradeResp upgradeResp, final String softwareCode){
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(softwareCode);
        if(downloadInfo != null){
            if(downloadInfo.version.equals(upgradeResp.getSoftwareVersion())){//mean already add
                if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()){
                        if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(file))) {
                            if (!isInQueue(downloadInfo.fileId)) {
                                addToQueueNextCheckInstall(downloadInfo.fileId);
                            }
                        }else{
                            if(file.delete() && CareController.instance.deleteDownloadInfo(downloadInfo.fileId) > 0){
                                return downloadBinder.startDownload(downloadInfo);
                            }else{
                                Log.e(TAG, "Common APK file damage, clear failed!");
                                return false;
                            }
                        }
                    }else{//something wrong? the file removed or same version update?
                        CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                        return downloadBinder.startDownload(downloadInfo);
                    }
                }else {
                    return downloadBinder.startDownload(downloadInfo);
                }
            }else{//old version in db
                return downloadBinder.startDownload(DownloadHelper.buildCommonAppDownloadInfo(appContext, upgradeResp, softwareCode));
            }
        }else{
            return downloadBinder.startDownload(DownloadHelper.buildCommonAppDownloadInfo(appContext, upgradeResp, softwareCode));
        }
        return true;
    }

    private IZeeManager zeeManager = null;
    public void bindManagerService(Context context) {
        Intent bindIntent = new Intent(BaseConstants.MANAGER_SERVICE_ACTION);
        bindIntent.setPackage(BaseConstants.MANAGER_PACKAGE_NAME);
        context.bindService(bindIntent, managerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindManagerService(Context context) {
        if(zeeManager != null) {
            context.unbindService(managerServiceConnection);
        }
    }

    private final ServiceConnection managerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()");
            zeeManager = IZeeManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()");
            zeeManager = null;
        }
    };

    public void removeRecentTask(String packageName){
        if(zeeManager != null){
            try {
                zeeManager.removeRecentTask(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleRemoveRecentTask(){
        if(zeeManager != null && lastPluginPackageName != null){
            try {
                zeeManager.removeRecentTask(lastPluginPackageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleRemoveAllRecentTasks(Context context){
        if(zeeManager != null){
            try {
                String excludePackageName = context.getPackageName() + "," + BaseConstants.MANAGER_PACKAGE_NAME;
                zeeManager.removeAllRecentTasks(excludePackageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void release(Context context){
        //unRegisterBroadCast();
        Log.i(TAG, "release()");
        if(downloadBinder != null){
            downloadBinder.unRegisterDownloadListener(downloadListener);
            context.unbindService(downloadServiceConnection);
            downloadBinder = null;
        }

        if(zeeManager != null){
            context.unbindService(managerServiceConnection);
            zeeManager = null;
        }

        ZeeAiManager.getInstance().unbindAiService(context);
    }

    public void bindGestureAiService(Context context) {
        ZeeAiManager.getInstance().setOnServiceStateListener(new ZeeAiManager.OnServiceStateListener() {
            @Override
            public void onServiceConnected() {

            }

            @Override
            public void onServiceDisconnected() {
                sendGestureAiServiceCheck(appContext);
            }
        });
        ZeeAiManager.getInstance().bindAiService(context);
    }

    public void startGestureAi(boolean withActive){
        ZeeAiManager.getInstance().startGestureAI(withActive);
    }

    public void stopGestureAi(){
        ZeeAiManager.getInstance().stopGestureAI();
    }

    public boolean isGestureAIActive(){
        return ZeeAiManager.getInstance().isGestureAIActive();
    }

    public static boolean isSettingGestureAIEnable(){
        try {
            Context settingContext = appContext.createPackageContext(BaseConstants.SETTINGS_APP_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = settingContext.getSharedPreferences("spUtils", Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            String gesture = sp.getString("Gesture", "");
            Log.i(TAG, "gesture -> " + gesture);
            if("open".equals(gesture)){
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "isSettingGestureAIEnable() err -> " + e);
            e.printStackTrace();
        }
        return false;
    }

    public static final String CLIENT_ID = "ZeeWain";
    public static final String ACTION_CARE_PING = CLIENT_ID + "_CARE.PING";
    private static void scheduleNextPing(Context context) {
        int keepAliveSeconds = 30;
        Log.i(TAG, "scheduleNextPing() " + keepAliveSeconds + " seconds");
        Intent intent = new Intent(ACTION_CARE_PING);
        intent.setPackage(context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar wakeUpTime = Calendar.getInstance();
        wakeUpTime.add(Calendar.SECOND, keepAliveSeconds);
        AlarmManager aMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        aMgr.cancel(pendingIntent);
        aMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(), pendingIntent);
    }
}

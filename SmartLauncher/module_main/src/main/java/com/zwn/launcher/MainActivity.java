package com.zwn.launcher;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.qihoo360.replugin.RePlugin;
import com.zee.guide.ui.GuideActivity;
import com.zee.launcher.login.ui.LoginActivity;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.utils.SPUtils;

import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.UpgradeResp;
import com.zwn.launcher.service.ZeeServiceManager;
import com.zwn.launcher.ui.upgrade.UpgradeDialogActivity;
import com.zwn.launcher.ui.upgrade.UpgradeTipDialogActivity;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.qihoo360.replugin.model.PluginInfo;


public class MainActivity extends BaseActivity{
    private static final String TAG = "MainActivity";
    private final int REQUEST_CODE_PERMISSIONS = 1;
    private final int REQUEST_CODE_SDCARD_PERMISSION = 201;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
    };

    private MainViewModel mainViewModel;
    private LoadingView loadingViewMain;
    private NetworkErrView networkErrViewMain;
    private LinearLayout layoutMainDeviceOperate;
    private MaterialCardView cardDeviceReboot;
    private MaterialCardView cardDeviceShutdown;
    private TextView tvMainTip;
    private String hostPluginFileId;
    private String hostMainPlugin;
    private String hostMainPluginClassPath;
    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private boolean isLastGestureAIActive = false;
    private long lastProgressTime = 0;
    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            if (fileId.equals(hostPluginFileId)) {
                runOnUiThread(() -> {
                    long waitTimeSec = 0;
                    if(lastProgressTime == 0) {
                        lastProgressTime = System.currentTimeMillis();
                    }else{
                        waitTimeSec = (((System.currentTimeMillis() - lastProgressTime) * (100 - progress)) + 5000);
                        lastProgressTime = System.currentTimeMillis();
                    }

                    if(loadingViewMain != null && loadingViewMain.getVisibility() == View.VISIBLE) {
                        long minutes = waitTimeSec/1000/60;
                        if(minutes > 1) {
                            loadingViewMain.setText("正在更新组件，大概需要 " + minutes + " 分钟");
                        }else{
                            loadingViewMain.setText("正在更新组件，大概需要 1 分钟");
                        }
                    }
                });
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            if (fileId.equals(hostPluginFileId)) {
                runOnUiThread(() -> {
                    lastProgressTime = 0;
                    installPlugin(fileId, file.getAbsolutePath());
                });
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(hostPluginFileId)) {
                runOnUiThread(() -> {
                    lastProgressTime = 0;
                    showNetworkErr();
                });
            }
        }

        @Override
        public void onPaused(String fileId) {}

        @Override
        public void onCancelled(String fileId) {}

        @Override
        public void onUpdate(String fileId) {}
    };

    private void installPlugin(final String fileId, final String filePath){
        mThreadPool.execute(() -> {
            long currentTime = System.currentTimeMillis();
            PluginInfo info = RePlugin.install(filePath);
            Log.d(TAG, "installPlugin() cost= " + (System.currentTimeMillis() - currentTime) + ", filePath=" + filePath);
            if(info != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(loadingViewMain != null && loadingViewMain.getVisibility() == View.VISIBLE) {
                            loadingViewMain.setText("加载中");
                        }

                        startMainPluginActivity();
                    }
                });
                Log.d(TAG, "installPlugin() 2 cost= " + (System.currentTimeMillis() - currentTime));
            }else{//安装插件失败 need retry?
                Log.e(TAG, "installPlugin() failed! fileId=" + fileId + ", filePath=" + filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingViewMain.setVisibility(View.GONE);
                        layoutMainDeviceOperate.setVisibility(View.VISIBLE);
                        tvMainTip.setText("主题更新失败！请重启设备！");
                        cardDeviceReboot.requestFocus();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isGuideDone = SPUtils.getInstance().getBoolean(SharePrefer.GuideDone);
        if(!isGuideDone){
            Intent intent = new Intent();
            intent.setClass(this, GuideActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if(!CommonUtils.isUserLogin()){
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestPermission();
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "new version: " + ApkUtil.getAppVersionName(this));

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ZeeServiceManager.checkPluginAppRelay(MainActivity.this);
            }
        });

        checkApkInstallResult();
        ZeeServiceManager.getInstance().bindDownloadService(this);
        //ZeeServiceManager.getInstance().bindManagerService(this);
        ZeeServiceManager.getInstance().registerDownloadListener(downloadListener);

        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        initView();
        initListener();
        initViewObservable();

        registerBroadCast();

        if(!CommonUtils.createOrClearPluginModelDir()){
            showToast("模型目录创建失败！");
        }

        checkHostApkStats();

        CommonUtils.savePluginCareInfo();

        loadingViewMain.setVisibility(View.VISIBLE);
        loadingViewMain.startAnim();
        loadingViewMain.requestFocus();
        if (NetworkUtil.isNetworkAvailable(this)) {
            loadingViewMain.postDelayed(new Runnable() {
                @Override
                public void run() {
                    reqAppUpgrade();
                }
            }, 1000);
        } else {
            loadingViewMain.setTag(true);
            loadingViewMain.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkShouldInitData();
                }
            }, 5000);
        }
    }

    private void reqAppUpgrade(){
        mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
        mainViewModel.reqCommonAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.SETTINGS_APP_PACKAGE_NAME), BaseConstants.SETTINGS_APP_SOFTWARE_CODE);
        mainViewModel.reqCommonAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.ZEE_GESTURE_AI_APP_PACKAGE_NAME), BaseConstants.ZEE_GESTURE_AI_APP_SOFTWARE_CODE);
    }

    private synchronized void checkShouldInitData(){
        if(loadingViewMain.getTag() != null){
            boolean needLoadData = (Boolean)loadingViewMain.getTag();
            loadingViewMain.setTag(false);
            if(needLoadData){
                reqAppUpgrade();
            }
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_SDCARD_PERMISSION);
            }else{
                checkPermission();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    private void checkPermission(){
        if (allPermissionsGranted()) {

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SDCARD_PERMISSION){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()) {
                    checkPermission();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {
                showToast("请开启权限！");
            }
        }
    }

    private void checkApkInstallResult(){
        String apkPath = getIntent().getStringExtra(BaseConstants.EXTRA_APK_PATH);
        if(apkPath != null && !apkPath.isEmpty()){
            boolean apkInstallResult = getIntent().getBooleanExtra(BaseConstants.EXTRA_APK_INSTALL_RESULT, false);
            String fileId = getIntent().getStringExtra(BaseConstants.EXTRA_PLUGIN_NAME);
            Log.i(TAG, "handleApkInstallResult() apkPath: " + apkPath + ", result=" + apkInstallResult + ", fileId=" + fileId);
            if(apkInstallResult){
                showToast("版本更新成功！");
            }
            if(fileId != null && CareController.instance.deleteDownloadInfo(fileId) > 0) {
                FileUtils.deleteFile(apkPath);
            }
        }
    }

    private void checkHostApkStats(){
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
        if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
            File file = new File(downloadInfo.filePath);
            if (!file.exists()) {
                CareController.instance.deleteDownloadInfo(BaseConstants.HOST_APP_SOFTWARE_CODE);
            }
        }
    }

    private void setLauncherWallpaper(){
        boolean isSetWallpaperDone = SPUtils.getInstance().getBoolean(SharePrefer.SetWallpaperDone, false);
        if(!isSetWallpaperDone) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.getApplicationContext());
            try {
                wallpaperManager.setBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_black_bg));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                SPUtils.getInstance().put(SharePrefer.SetWallpaperDone, true);
            }
        }
    }

    private void initView(){
        loadingViewMain = findViewById(R.id.loadingView_main);
        networkErrViewMain = findViewById(R.id.networkErrView_main);
        layoutMainDeviceOperate = findViewById(R.id.ll_main_device_operate);
        cardDeviceReboot = findViewById(R.id.card_device_reboot);
        cardDeviceShutdown = findViewById(R.id.card_device_shutdown);
        tvMainTip = findViewById(R.id.tv_main_tip);
    }

    private void initListener() {
        cardDeviceReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hostPluginFileId != null && hostMainPlugin != null) {
                    CareController.instance.deleteDownloadInfo(hostPluginFileId);
                    RePlugin.uninstall(hostMainPlugin);
                }
                SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, 0);
                Intent intent = new Intent(Intent.ACTION_REBOOT);
                intent.putExtra("nowait", 1);
                intent.putExtra("interval", 1);
                intent.putExtra("window", 0);
                sendBroadcast(intent);
            }
        });

        cardDeviceReboot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cardDeviceReboot.setStrokeColor(0xFFFFFFFF);
                    final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
                    cardDeviceReboot.setStrokeWidth(strokeWidth);
                    CommonUtils.scaleView(v, 1.1f);
                } else {
                    cardDeviceReboot.setStrokeColor(0x00FFFFFF);
                    cardDeviceReboot.setStrokeWidth(0);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            }
        });

        cardDeviceShutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hostPluginFileId != null && hostMainPlugin != null) {
                    CareController.instance.deleteDownloadInfo(hostPluginFileId);
                    RePlugin.uninstall(hostMainPlugin);
                }
                SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, 0);
                Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                intent.putExtra("android.intent.extra.KEY_CONFIRM", false);//其中false换成true,会弹出是否关机的确认窗口
                startActivity(intent);
            }
        });

        cardDeviceShutdown.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cardDeviceShutdown.setStrokeColor(0xFFFFFFFF);
                    final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
                    cardDeviceShutdown.setStrokeWidth(strokeWidth);
                    CommonUtils.scaleView(v, 1.1f);
                } else {
                    cardDeviceShutdown.setStrokeColor(0x00FFFFFF);
                    cardDeviceShutdown.setStrokeWidth(0);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            }
        });

        networkErrViewMain.setRetryClickListener(() -> {
            if(LoadState.Failed == mainViewModel.mldManagerAppUpgradeState.getValue()){
                mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
            }else if(LoadState.Failed == mainViewModel.mldHostAppUpgradeState.getValue()){
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }else if(mainViewModel.mldThemeInfoLoadState.getValue() != null &&
                    LoadState.Failed == mainViewModel.mldThemeInfoLoadState.getValue().loadState){
                mainViewModel.reqThemeInfo();
            }else if(LoadState.Failed == mainViewModel.mldHostPluginPublishState.getValue()){
                mainViewModel.reqHostPluginPublishInfo(mainViewModel.mldThemeInfoLoadState.getValue().data);
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
                if(mainViewModel.hostPluginPublishResp != null)
                    handleHostPluginPublishResp(mainViewModel.hostPluginPublishResp);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewObservable() {
        mainViewModel.mldCommonAppUpgradeState.observe(this, upgradeLoadState -> {
            if (LoadState.Success == upgradeLoadState.loadState) {
                if(upgradeLoadState.upgradeResp != null){
                    if(ZeeServiceManager.getInstance().getDownloadBinder() != null) {
                        ZeeServiceManager.getInstance().handleCommonAppUpgrade(upgradeLoadState.upgradeResp, upgradeLoadState.softwareCode);
                    }
                }
            }
        });

        mainViewModel.mldManagerAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if(mainViewModel.managerAppUpgradeResp != null){
                    if(ZeeServiceManager.getInstance().getDownloadBinder() != null) {
                        ZeeServiceManager.getInstance().handleManagerAppUpgrade(mainViewModel.managerAppUpgradeResp);
                    }
                }
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }else if(LoadState.Failed == loadState){
                loadingViewMain.stopAnim();
                loadingViewMain.setVisibility(View.GONE);
                networkErrViewMain.setVisibility(View.VISIBLE);
                networkErrViewMain.requestFocus();
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
            }
        });

        mainViewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                UpgradeResp upgradeResp = mainViewModel.hostAppUpgradeResp;
                if (upgradeResp != null && upgradeResp.isForcible()) {
                    UpgradeDialogActivity.showUpgradeDialog(this, upgradeResp);
                }else{
                    mainViewModel.reqThemeInfo();
                }
            }else if(LoadState.Failed == loadState){
                loadingViewMain.stopAnim();
                loadingViewMain.setVisibility(View.GONE);
                networkErrViewMain.setVisibility(View.VISIBLE);
                networkErrViewMain.requestFocus();
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
            }
        });

        mainViewModel.mldThemeInfoLoadState.observe(this, new Observer<DataLoadState<String>>() {
            @Override
            public void onChanged(DataLoadState<String> dataLoadState) {
                if (LoadState.Success == dataLoadState.loadState) {
                    mainViewModel.reqHostPluginPublishInfo(dataLoadState.data);
                }else if(LoadState.Failed == dataLoadState.loadState){
                    loadingViewMain.stopAnim();
                    loadingViewMain.setVisibility(View.GONE);
                    networkErrViewMain.setVisibility(View.VISIBLE);
                    networkErrViewMain.requestFocus();
                }else{
                    if(loadingViewMain.getVisibility() != View.VISIBLE) {
                        loadingViewMain.setVisibility(View.VISIBLE);
                        loadingViewMain.startAnim();
                    }
                    networkErrViewMain.setVisibility(View.GONE);
                }
            }
        });

        mainViewModel.mldHostPluginPublishState.observe(this, new Observer<LoadState>() {
            @Override
            public void onChanged(LoadState loadState) {
                if (LoadState.Success == loadState) {
                    if(mainViewModel.hostPluginPublishResp != null)
                        handleHostPluginPublishResp(mainViewModel.hostPluginPublishResp);
                }else if(LoadState.Failed == loadState){
                    loadingViewMain.stopAnim();
                    loadingViewMain.setVisibility(View.GONE);
                    networkErrViewMain.setVisibility(View.VISIBLE);
                    networkErrViewMain.requestFocus();
                }else{
                    if(loadingViewMain.getVisibility() != View.VISIBLE) {
                        loadingViewMain.setVisibility(View.VISIBLE);
                        loadingViewMain.startAnim();
                    }
                    networkErrViewMain.setVisibility(View.GONE);
                }
            }
        });

        mainViewModel.mldToastMsg.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                showToast(msg);
            }
        });
    }

    private void showNetworkErr(){
        if(loadingViewMain != null && networkErrViewMain != null) {
            loadingViewMain.stopAnim();
            loadingViewMain.setVisibility(View.GONE);
            networkErrViewMain.setVisibility(View.VISIBLE);
            networkErrViewMain.requestFocus();
        }
    }

    private synchronized void handleHostPluginPublishResp(PublishResp publishResp){
        if(ZeeServiceManager.getInstance().getDownloadBinder() == null){
            Log.e(TAG, "handleHostPluginPublishResp(), but downloadBinder null!!!");
            return;
        }

        hostPluginFileId = publishResp.getSoftwareInfo().getSoftwareCode();
        hostMainPlugin = publishResp.getSoftwareInfo().getSoftwareExtendInfo().getMainPlugin();
        hostMainPluginClassPath = publishResp.getSoftwareInfo().getSoftwareExtendInfo().getMainClassPath();

        String lastVersion = publishResp.getSoftwareVersion();

        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(hostPluginFileId);
        boolean startDownloadResult = false;
        if (dbDownloadInfo != null) {
            if (!dbDownloadInfo.version.equals(lastVersion)){
                startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildHostPluginDownloadInfo(this, publishResp));
            }else{
                if(dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED){
                    startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(dbDownloadInfo);
                }else if(dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    if(RePlugin.isPluginInstalled(hostMainPlugin)){
                        startMainPluginActivity();
                        return;
                    }else{//未安装，文件存在，则进行安装
                        File file = new File(dbDownloadInfo.filePath);
                        if(file.exists() && dbDownloadInfo.packageMd5.equals(FileUtils.file2MD5(file))){
                            installPlugin(dbDownloadInfo.fileId, file.getAbsolutePath());
                            return;
                        }else{
                            file.delete();
                            CareController.instance.deleteDownloadInfo(dbDownloadInfo.fileId);
                            startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildHostPluginDownloadInfo(this, publishResp));
                        }
                    }
                }else{
                    startDownloadResult = true;
                }
            }
        }else{
            startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildHostPluginDownloadInfo(this, publishResp));
        }

        if(startDownloadResult){
            loadingViewMain.setText("正在更新组件，请稍候...");
        }else{
            CareController.instance.deleteDownloadInfo(hostPluginFileId);
            showNetworkErr();
        }
    }

    private void startMainPluginActivity(){
        Log.i(TAG, "startMainPluginActivity " + RePlugin.getPluginInfo(hostMainPlugin));
        boolean startResult = RePlugin.startActivity(this, RePlugin.createIntent(hostMainPlugin, hostMainPluginClassPath));
        if(!startResult){
            int failedTimes = SPUtils.getInstance().getInt(SharePrefer.StartPluginFailedTimes, 0);
            Log.e(TAG, "startMainPluginActivity failed!");
            showToast("配置主题失败！");
            failedTimes = failedTimes + 1;
            SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, failedTimes);
            if(failedTimes == 1){
                layoutMainDeviceOperate.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rebootMainProcess(getApplicationContext());
                    }
                }, 500);
            }else if(failedTimes == 2 || failedTimes == 3){
                CareController.instance.deleteDownloadInfo(hostPluginFileId);
                RePlugin.uninstall(hostMainPlugin);
                clearGlideCache();
                layoutMainDeviceOperate.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rebootMainProcess(getApplicationContext());
                    }
                }, 1000);
            }else{
                loadingViewMain.setVisibility(View.GONE);
                layoutMainDeviceOperate.setVisibility(View.VISIBLE);
                tvMainTip.setText("配置主题失败！请重启设备！");
                cardDeviceReboot.requestFocus();
                //todo only to clear all data?
            }
        }else{
            SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, 0);
        }
    }

    private void clearGlideCache(){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Glide.get(getApplication()).clearDiskCache();
            }
        });
    }

    private void rebootMainProcess(final Context context){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        mainViewModel.checkCrashLog();

        if(!NetworkUtil.isNetworkAvailable(this) && mainViewModel.hostAppUpgradeResp != null
                && mainViewModel.hostAppUpgradeResp.isForcible()){
            DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
            if ((dbDownloadInfo != null) && (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED)) {
                UpgradeDialogActivity.showUpgradeDialog(this, mainViewModel.hostAppUpgradeResp);
            }
        }else if(hostMainPlugin != null && RePlugin.isPluginInstalled(hostMainPlugin)){
            startMainPluginActivity();
        }else{
            Log.e(TAG, "onResume todo what?");
        }

        bindAiServiceAndCheckStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        ZeeServiceManager.getInstance().unRegisterDownloadListener(downloadListener);
        unRegisterBroadCast();
        ZeeServiceManager.getInstance().release(this);
        super.onDestroy();
    }

    private void bindAiServiceAndCheckStart(){
        ZeeServiceManager.getInstance().bindGestureAiService(this);
        networkErrViewMain.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(ZeeServiceManager.isSettingGestureAIEnable()){
                    ZeeServiceManager.getInstance().startGestureAi(false);
                }
            }
        }, 1000);
    }

    public void onNetChange() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork && activeNetwork.isConnected()) {
            if(null == mainViewModel.mldManagerAppUpgradeState.getValue()) {
                checkShouldInitData();
            }else if(LoadState.Failed == mainViewModel.mldManagerAppUpgradeState.getValue()){
                mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
            }else if(LoadState.Failed == mainViewModel.mldHostAppUpgradeState.getValue()){
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }else if(mainViewModel.mldThemeInfoLoadState.getValue() != null
                    && LoadState.Failed == mainViewModel.mldThemeInfoLoadState.getValue().loadState){
                mainViewModel.reqThemeInfo();
            }else if(LoadState.Failed == mainViewModel.mldHostPluginPublishState.getValue()){
                mainViewModel.reqHostPluginPublishInfo(hostPluginFileId);
            }

            if (mainViewModel.hostAppUpgradeResp != null && ZeeServiceManager.getInstance().getDownloadBinder() != null){
                DownloadInfo  dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
                if ((dbDownloadInfo!=null) && (dbDownloadInfo.status != DownloadInfo.STATUS_SUCCESS)) {
                    ZeeServiceManager.getInstance().getDownloadBinder().startDownload(dbDownloadInfo.fileId);
                }
            }
        }
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(BaseConstants.GESTURE_AI_SERVICE_CHECK_ACTION);
        intentFilter.addAction(BaseConstants.ACTION_CRASH_MSG);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        careBroadcastReceiver = new CareBroadcastReceiver();
        registerReceiver(careBroadcastReceiver, intentFilter);
    }

    private void unRegisterBroadCast() {
        if(careBroadcastReceiver != null)
            unregisterReceiver(careBroadcastReceiver);
    }

    private CareBroadcastReceiver careBroadcastReceiver;

    class CareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                onNetChange();
            }else if(BaseConstants.GESTURE_AI_SERVICE_CHECK_ACTION.equals(intent.getAction())){
                bindAiServiceAndCheckStart();
            }else if(BaseConstants.ACTION_CRASH_MSG.equals(intent.getAction())){
                String crashMsg = intent.getStringExtra(BaseConstants.EXTRA_CRASH_MSG);
                String pkg = intent.getStringExtra(BaseConstants.EXTRA_CRASH_PKG);
                Log.i(TAG, "crashMsg: " + crashMsg);
                Log.i(TAG, "pkg: " + pkg);
                if(crashMsg != null){
                    mainViewModel.reqUploadLog(crashMsg, pkg);
                }
            }else if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
                Log.i(TAG, "ACTION_SCREEN_ON" );
                if(ZeeServiceManager.isSettingGestureAIEnable()){
                    ZeeServiceManager.getInstance().startGestureAi(isLastGestureAIActive);
                }
            }else if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
                Log.i(TAG, "ACTION_SCREEN_OFF" );
                if(ZeeServiceManager.isSettingGestureAIEnable()){
                    isLastGestureAIActive = ZeeServiceManager.getInstance().isGestureAIActive();
                    ZeeServiceManager.getInstance().stopGestureAi();
                }

            }
        }
    }
}
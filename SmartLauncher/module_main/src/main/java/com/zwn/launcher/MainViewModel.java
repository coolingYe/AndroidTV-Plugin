package com.zwn.launcher;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.LogFileConfig;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DateTimeUtils;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.SPUtils;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.model.UpgradeLoadState;
import com.zwn.launcher.data.protocol.request.PublishReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zwn.launcher.data.protocol.request.UpgradeReq;
import com.zwn.launcher.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.ThemeInfoResp;
import com.zwn.launcher.data.protocol.response.UpgradeResp;

import java.io.File;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;


public class MainViewModel extends BaseViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private final DataRepository dataRepository;
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldHostAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldManagerAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<UpgradeLoadState> mldCommonAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<String>> mldThemeInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldHostPluginPublishState = new MutableLiveData<>();
    public UpgradeResp hostAppUpgradeResp;
    public UpgradeResp managerAppUpgradeResp;
    public PublishResp hostPluginPublishResp;

    public MainViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqCommonAppUpgrade(String version, final String softwareCode) {
        mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Loading, softwareCode));
        UpgradeReq upgradeReq = new UpgradeReq(version, softwareCode);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            UpgradeResp commonAppUpgradeResp = response.data;
                            if (commonAppUpgradeResp != null) {
                                if (commonAppUpgradeResp.getVersionId() == null || commonAppUpgradeResp.getVersionId().isEmpty()) {
                                    commonAppUpgradeResp = null;
                                }
                            }
                            mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Success, softwareCode, commonAppUpgradeResp));
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Failed, softwareCode));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Failed, softwareCode));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqManagerAppUpgrade(String version) {
        mldManagerAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.MANAGER_APP_SOFTWARE_CODE);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            managerAppUpgradeResp = response.data;
                            if (managerAppUpgradeResp != null) {
                                if (managerAppUpgradeResp.getVersionId() == null || managerAppUpgradeResp.getVersionId().isEmpty()) {
                                    managerAppUpgradeResp = null;
                                }
                            }
                            mldManagerAppUpgradeState.setValue(LoadState.Success);
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldManagerAppUpgradeState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldManagerAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqHostAppUpgrade(String version) {
        mldHostAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.HOST_APP_SOFTWARE_CODE);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            hostAppUpgradeResp = response.data;
                            if (hostAppUpgradeResp != null) {
                                if (hostAppUpgradeResp.getVersionId() == null || hostAppUpgradeResp.getVersionId().isEmpty()) {
                                    hostAppUpgradeResp = null;
                                }
                            }
                            mldHostAppUpgradeState.setValue(LoadState.Success);
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldHostAppUpgradeState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldHostAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqThemeInfo(){
        mldThemeInfoLoadState.setValue(new DataLoadState<>(LoadState.Loading));
        dataRepository.getThemeInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ThemeInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ThemeInfoResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            if(response.data != null && response.data.softwareCode != null
                                    && !response.data.softwareCode.isEmpty()) {
                                mldThemeInfoLoadState.setValue(new DataLoadState<>(LoadState.Success, response.data.softwareCode));
                            }else{
                                mldToastMsg.setValue("主题包配置错误！");
                                mldThemeInfoLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldThemeInfoLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络请求失败！");
                        mldThemeInfoLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqHostPluginPublishInfo(String softwareCode) {
        mldHostPluginPublishState.setValue(LoadState.Loading);
        dataRepository.getPublishedVersionInfo(new PublishReq(softwareCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<PublishResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            hostPluginPublishResp = response.data;
                            if(hostPluginPublishResp == null || hostPluginPublishResp.getSoftwareInfo() == null
                                    || hostPluginPublishResp.getSoftwareInfo().getSoftwareCode() == null){
                                hostPluginPublishResp = null;
                                mldToastMsg.setValue("未配置主题应用！");
                                mldHostPluginPublishState.setValue(LoadState.Failed);
                            }else{
                                mldHostPluginPublishState.setValue(LoadState.Success);
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldHostPluginPublishState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络请求失败！");
                        mldHostPluginPublishState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUploadLog(String message, String packageName) {
        String time = DateTimeUtils.formatDateToString(new Date(), LogFileConfig.LOG_TIME_FORMAT);
        UploadLogReq uploadLogReq = new UploadLogReq(packageName, time, message, packageName);
        dataRepository.uploadLog(uploadLogReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new ResourceObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {}

                    @Override
                    public void onError(@NonNull Throwable e) {}

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void uploadLog(String time, String message) {
        UploadLogReq uploadLogReq = new UploadLogReq(message, time);
        dataRepository.uploadLog(uploadLogReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new ResourceObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == 0) {
                            SPUtils.getInstance().put(SharePrefer.upLoadLogFile, "");
                            Log.w(TAG, "日志上传成功！");
                        } else {
                            Log.e(TAG, resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "日志上传错误！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void checkCrashLog() {
        List<File> fileList = FileUtils.getFiles(LogFileConfig.directoryPath);
        if (fileList != null && fileList.size() > LogFileConfig.maxSize) {
            for (int i = fileList.size() - 1; i >= LogFileConfig.maxSize; i--) {
                String deleteFilePath = fileList.get(i).getAbsolutePath();
                boolean deleteResult = FileUtils.deleteFile(deleteFilePath);
                if (!deleteResult) {
                    Log.e(TAG, fileList.get(i).getAbsolutePath() + "删除失败");
                }
            }
        }

        String logFileName = SPUtils.getInstance().getString(SharePrefer.upLoadLogFile);
        if (logFileName == null || logFileName.isEmpty() || logFileName.equals("null")) {
            return;
        }

        String time = DateTimeUtils.formatDateToString(new Date(), LogFileConfig.LOG_TIME_FORMAT);
        String logPath = LogFileConfig.directoryPath + logFileName;
        String logMessage = FileUtils.readFile(logPath);
        if (logMessage != null) {
            uploadLog(time, "STATE=重启后\n" + logMessage);
        }
    }

}

package com.zee.guide.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.guide.data.GuideRepository;
import com.zee.guide.data.protocol.response.DeviceInfoResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class GuideViewModel extends BaseViewModel {
    private final GuideRepository guideRepository;
    public MutableLiveData<LoadState> mldDeviceInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public DeviceInfoResp deviceInfoResp;

    public GuideViewModel(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    public void reqDeviceInfo(String deviceSn) {
        mldDeviceInfoLoadState.setValue(LoadState.Loading);
        guideRepository.getDeviceInfo(deviceSn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DeviceInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DeviceInfoResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            deviceInfoResp = resp.data;
                            mldDeviceInfoLoadState.setValue(LoadState.Success);
                        } else {
                            mldDeviceInfoLoadState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络设置！");
                        mldDeviceInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}

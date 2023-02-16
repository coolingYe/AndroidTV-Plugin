package com.zee.guide.data;

import com.zee.guide.data.protocol.response.DeviceInfoResp;
import com.zee.guide.data.source.http.service.GuideService;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;

import io.reactivex.Observable;

public class GuideRepository implements GuideService{
    private static volatile GuideRepository instance;
    private final GuideService guideService;

    private GuideRepository(GuideService guideService){
        this.guideService = guideService;
    }

    public static GuideRepository getInstance(){
        if(instance == null){
            synchronized (GuideRepository.class){
                if (instance == null){
                    instance = new GuideRepository(RetrofitClient.getInstance().create(GuideService.class));
                }
            }
        }
        return instance;
    }

    @Override
    public Observable<BaseResp<DeviceInfoResp>> getDeviceInfo(String deviceSn) {
        return guideService.getDeviceInfo(deviceSn);
    }
}

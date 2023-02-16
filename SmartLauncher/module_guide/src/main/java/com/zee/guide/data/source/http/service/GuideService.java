package com.zee.guide.data.source.http.service;

import com.zee.guide.data.protocol.response.DeviceInfoResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GuideService {

    @GET(BaseConstants.basePath + "/dmsmgr/purchase/device/info")
    Observable<BaseResp<DeviceInfoResp>> getDeviceInfo(@Query("deviceSn") String deviceSn);
}

package com.zee.launcher.login.data.source.http.service;

import com.zee.launcher.login.data.protocol.request.AkSkReq;
import com.zee.launcher.login.data.protocol.request.ImageCaptchaReq;
import com.zee.launcher.login.data.protocol.request.UserActivateReq;
import com.zee.launcher.login.data.protocol.request.UserPwdLoginReq;
import com.zee.launcher.login.data.protocol.response.AkSkResp;
import com.zee.launcher.login.data.protocol.response.ImageCaptchaResp;
import com.zee.launcher.login.data.protocol.response.LoginResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {

    @POST(BaseConstants.basePath + "/auth/client/get-ak-sk")
    Observable<BaseResp<AkSkResp>> getAkSkInfo(@Body AkSkReq akSkReq);

    @POST(BaseConstants.basePath + "/dmsmgr/purchase/device/activate")
    Observable<BaseResp<String>> userActivateReq(@Body UserActivateReq userActivateReq);

    @POST(BaseConstants.basePath + "/operation/sso/doLogin")
    Observable<BaseResp<LoginResp>> userPwdLoginReq(@Body UserPwdLoginReq userPwdLoginReq);

    @POST(BaseConstants.basePath + "/captcha/captcha/image")
    Observable<BaseResp<ImageCaptchaResp>> imageCaptchaReq(@Body ImageCaptchaReq imageCaptchaReq);
}

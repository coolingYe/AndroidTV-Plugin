package com.zee.launcher.login.data;

import com.zee.launcher.login.data.protocol.request.AkSkReq;
import com.zee.launcher.login.data.protocol.request.ImageCaptchaReq;
import com.zee.launcher.login.data.protocol.request.UserActivateReq;
import com.zee.launcher.login.data.protocol.request.UserPwdLoginReq;
import com.zee.launcher.login.data.protocol.response.AkSkResp;
import com.zee.launcher.login.data.protocol.response.ImageCaptchaResp;
import com.zee.launcher.login.data.protocol.response.LoginResp;
import com.zee.launcher.login.data.source.http.service.UserService;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zeewain.base.utils.SPUtils;


import io.reactivex.Observable;

public class UserRepository implements UserService {

    private static volatile UserRepository instance;
    private final UserService userService;

    private UserRepository(UserService userService){
        this.userService = userService;
    }

    public static UserRepository getInstance(){
        if(instance == null){
            synchronized (UserRepository.class) {
                if (instance == null){
                    instance = new UserRepository(RetrofitClient.getInstance().create(UserService.class));
                }
            }
        }
        return instance;
    }

    public Observable<BaseResp<AkSkResp>> getAkSkInfo(AkSkReq akSkReq){
        return userService.getAkSkInfo(akSkReq);
    }

    @Override
    public Observable<BaseResp<String>> userActivateReq(UserActivateReq userActivateReq) {
        return userService.userActivateReq(userActivateReq);
    }

    @Override
    public Observable<BaseResp<LoginResp>> userPwdLoginReq(UserPwdLoginReq userPwdLoginReq) {
        return userService.userPwdLoginReq(userPwdLoginReq);
    }

    @Override
    public Observable<BaseResp<ImageCaptchaResp>> imageCaptchaReq(ImageCaptchaReq imageCaptchaReq) {
        return userService.imageCaptchaReq(imageCaptchaReq);
    }

    public void putValue(String key, String value) {
        SPUtils.getInstance().put(key, value);
    }

    public String getString(String key) {
        return SPUtils.getInstance().getString(key);
    }

    public void removeValue(String key) {
        SPUtils.getInstance().remove(key);
    }
}

package com.zee.launcher.login.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.zee.launcher.login.data.UserRepository;
import com.zee.launcher.login.data.protocol.request.AkSkReq;
import com.zee.launcher.login.data.protocol.request.ImageCaptchaReq;
import com.zee.launcher.login.data.protocol.request.UserActivateReq;
import com.zee.launcher.login.data.protocol.request.UserPwdLoginReq;
import com.zee.launcher.login.data.protocol.response.AkSkResp;
import com.zee.launcher.login.data.protocol.response.ImageCaptchaResp;
import com.zee.launcher.login.data.protocol.response.LoginResp;
import com.zee.launcher.login.utils.AndroidHelper;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonVariableCacheUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends BaseViewModel {

    private final UserRepository mUserRepository;
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldUserActivateState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldUserPwdLoginState = new MutableLiveData<>();

    public MutableLiveData<LoadState> mldImgCodeReqState = new MutableLiveData<>();

    public ImageCaptchaResp imageCaptchaResp;
    public UserActivateReq usedUserActivateReq;

    public LoginViewModel(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    public String checkMobilePhone(String phoneNumber) {
        if (phoneNumber.trim().isEmpty()) return "手机号不能为空";
        if (phoneNumber.length() < 11) return "手机号位数错误";
        if (!AndroidHelper.isPhone(phoneNumber)) return "请输入正确的手机号";
        return "";
    }

    public String checkPassword(String password) {
        if (password.trim().isEmpty()) return "密码不能为空";
        if (password.length() < 8) return "密码不能小于8位";
        if (password.length() > 16) return "密码不能小于16位";
        if (!AndroidHelper.isCompliantPassword(password)) return "请输入符合要求的密码";
        return "";
    }

    public String checkConfirmPassword(String password, String confirmPassword) {
        if (!confirmPassword.equals(password)) return "两次密码输入不一致";
        return checkPassword(confirmPassword);
    }

    public String checkCaptcha(String captcha) {
        if (captcha.trim().isEmpty()) return "验证码不能为空";
        if (!AndroidHelper.isNumeric(captcha)) return "请输入正确的验证码";
        return "";
    }

    private void saveLoginData(String token, String account) {
        mUserRepository.putValue(SharePrefer.userToken, token);
        mUserRepository.putValue(SharePrefer.userAccount, account);
        CommonVariableCacheUtils.getInstance().token = token;
    }

    public void reqUserActivate(String userCode, String password, String deviceSn, String uuid, String code) {
        UserActivateReq userActivateReq = new UserActivateReq(userCode, password, deviceSn, uuid, code);
        mldUserActivateState.setValue(LoadState.Loading);
        mUserRepository.userActivateReq(userActivateReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            usedUserActivateReq = userActivateReq;
                            mldUserActivateState.setValue(LoadState.Success);
                        } else {
                            mldUserActivateState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldUserActivateState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("网络异常，请检查网络状态");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUserPwdLogin(String userCode, String password, String deviceSn, String uuid, String code) {
        UserPwdLoginReq userPwdLoginReq = new UserPwdLoginReq(userCode, password, deviceSn, uuid, code);
        mldUserPwdLoginState.setValue(LoadState.Loading);
        mUserRepository.userPwdLoginReq(userPwdLoginReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            saveLoginData(resp.data.token, userCode);
                            reqAkSkInfo();
                        } else {
                            mldUserPwdLoginState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldUserPwdLoginState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("登录失败，请检查网络状态");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqAkSkInfo() {
        mUserRepository.getAkSkInfo(new AkSkReq(BaseConstants.AUTH_SYSTEM_CODE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<AkSkResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<AkSkResp> resp) {
                        AkSkResp akSkResp = resp.data;
                        if (akSkResp != null && akSkResp.akCode != null && !akSkResp.akCode.isEmpty()) {
                            Gson gson = new Gson();
                            String akSkString = gson.toJson(akSkResp);
                            mUserRepository.putValue(SharePrefer.akSkInfo, akSkString);
                            mldUserPwdLoginState.setValue(LoadState.Success);
                        } else {
                            mldToastMsg.setValue(resp.message);
                            mldUserPwdLoginState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mUserRepository.putValue(SharePrefer.userToken, "");
                        mUserRepository.putValue(SharePrefer.userAccount, "");
                        CommonVariableCacheUtils.getInstance().token = "";
                        mldToastMsg.setValue("登录失败，请检查网络状态");
                        mldUserPwdLoginState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqImageCaptchaLogin() {
        ImageCaptchaReq imageCaptchaReq = new ImageCaptchaReq("1");
        reqImageCaptcha(imageCaptchaReq);
    }

    public void reqImageCaptchaRegister() {
        ImageCaptchaReq imageCaptchaReq = new ImageCaptchaReq("3");
        reqImageCaptcha(imageCaptchaReq);
    }

    public void reqImageCaptcha(ImageCaptchaReq imageCaptchaReq) {
        mldImgCodeReqState.setValue(LoadState.Loading);
        imageCaptchaResp = null;
        mUserRepository.imageCaptchaReq(imageCaptchaReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ImageCaptchaResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ImageCaptchaResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            imageCaptchaResp = resp.data;
                            mldImgCodeReqState.setValue(LoadState.Success);
                        } else {
                            mldImgCodeReqState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldImgCodeReqState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("网络异常，请检查网络状态！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
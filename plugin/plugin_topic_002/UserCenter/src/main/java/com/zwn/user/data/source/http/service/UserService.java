package com.zwn.user.data.source.http.service;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zwn.user.data.protocol.request.AkSkReq;
import com.zwn.user.data.protocol.request.DelFavoritesReq;
import com.zwn.user.data.protocol.request.DelHistoryReq;
import com.zwn.user.data.protocol.request.FavoritePagedReq;
import com.zwn.user.data.protocol.request.HistoryReq;
import com.zwn.user.data.protocol.request.ImageCaptchaReq;
import com.zwn.user.data.protocol.request.MsgCodeReq;
import com.zwn.user.data.protocol.request.MsgLoginReq;
import com.zwn.user.data.protocol.request.PwdLoginReq;
import com.zwn.user.data.protocol.request.ResetPasswordReq;
import com.zwn.user.data.protocol.request.UserActivateReq;
import com.zwn.user.data.protocol.request.UserPwdLoginReq;
import com.zwn.user.data.protocol.response.AboutUsInfoResp;
import com.zwn.user.data.protocol.response.AkSkResp;
import com.zwn.user.data.protocol.response.DelFavoritesResp;
import com.zwn.user.data.protocol.response.FavoritePagedResp;
import com.zwn.user.data.protocol.response.FavoriteStateResp;
import com.zwn.user.data.protocol.response.FavoritesResp;
import com.zwn.user.data.protocol.response.HistoryResp;
import com.zwn.user.data.protocol.response.ImageCaptchaResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.protocol.response.MsgCodeResp;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {

    @POST(BaseConstants.basePath + "/auth/client/get-ak-sk")
    Observable<BaseResp<AkSkResp>> getAkSkInfo(@Body AkSkReq akSkReq);

    @POST(BaseConstants.basePath + "/captcha/captcha/sms")
    Observable<BaseResp<MsgCodeResp>> getMsgCode(@Body MsgCodeReq msgCodeReq);

    @POST(BaseConstants.basePath + "/sso/sso/login")
    Observable<BaseResp<LoginResp>> msgLogin(@Body MsgLoginReq msgLoginReq);

    @POST(BaseConstants.basePath + "/sso/sso/login")
    Observable<BaseResp<LoginResp>> pwdLogin(@Body PwdLoginReq pwdLoginReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.USER_FAVORITES_PAGE_LIST)
    Observable<BaseResp<FavoritePagedResp>> getUserFavorites(@Body FavoritePagedReq favoritesPagedReq);

    @POST(BaseConstants.basePath + "/ums/favorite/courseware/del")
    Observable<BaseResp<DelFavoritesResp>> delFavorites(@Body DelFavoritesReq delFavoritesReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.USER_PLAY_RECORD_LIST)
    Observable<BaseResp<HistoryResp>> getUserHistory();

    @POST(BaseConstants.basePath + "/ums/playHistory/del")
    Observable<BaseResp<String>> delUserHistory(@Body DelHistoryReq delHistoryReq);

    @POST(BaseConstants.basePath + "/ums/playHistory/clean")
    Observable<BaseResp<String>> clearUserHistory();

    @POST(BaseConstants.basePath + "/manager/aboutUs/info")
    Observable<BaseResp<AboutUsInfoResp>> getAboutUsInfo();

    @POST(BaseConstants.basePath + "/user/user/find-password")
    Observable<BaseResp<String>> resetPassword(@Body ResetPasswordReq resetPasswordReq);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.USER_FAVORITES_ITEM_INFO)
    Observable<BaseResp<FavoriteStateResp>> getFavoriteState(@Query("objId") String objId);

    @POST(BaseConstants.basePath + "/dmsmgr/purchase/device/activate")
    Observable<BaseResp<String>> userActivateReq(@Body UserActivateReq userActivateReq);

    @POST(BaseConstants.basePath + "/operation/sso/doLogin")
    Observable<BaseResp<LoginResp>> userPwdLoginReq(@Body UserPwdLoginReq userPwdLoginReq);

    @POST(BaseConstants.basePath + "/captcha/captcha/image")
    Observable<BaseResp<ImageCaptchaResp>> imageCaptchaReq(@Body ImageCaptchaReq imageCaptchaReq);
}

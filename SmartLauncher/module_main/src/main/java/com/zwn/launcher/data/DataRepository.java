package com.zwn.launcher.data;


import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zwn.launcher.data.model.MainCategoryMo;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.data.protocol.request.CoursewareStartReq;
import com.zwn.launcher.data.protocol.request.MainCategoryReq;
import com.zwn.launcher.data.protocol.request.ProductListBySkuIdsReq;
import com.zwn.launcher.data.protocol.request.ProductListReq;
import com.zwn.launcher.data.protocol.request.ProductModuleListReq;
import com.zwn.launcher.data.protocol.request.ProductRecommendReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zwn.launcher.data.protocol.response.FavoriteStateResp;
import com.zwn.launcher.data.protocol.response.ServicePkgInfoResp;
import com.zwn.launcher.data.protocol.response.ThemeInfoResp;
import com.zwn.launcher.data.source.http.service.ApiService;
import com.zwn.launcher.data.protocol.request.ProDetailReq;
import com.zwn.launcher.data.protocol.request.PublishReq;
import com.zwn.launcher.data.protocol.request.UpgradeReq;
import com.zwn.launcher.data.protocol.response.ProDetailResp;
import com.zwn.launcher.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.UpgradeResp;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;

public class DataRepository implements ApiService{

    private static volatile DataRepository instance;
    private final ApiService apiService;

    private DataRepository(ApiService apiService){
        this.apiService = apiService;
    }

    public static DataRepository getInstance(){
        if(instance == null){
            synchronized (DataRepository.class){
                if (instance == null){
                    instance = new DataRepository(RetrofitClient.getInstance().create(ApiService.class));
                }
            }
        }
        return instance;
    }

    public Observable<BaseResp<List<MainCategoryMo>>> getMainCategoryList(MainCategoryReq mainCategoryReq){
        return apiService.getMainCategoryList(mainCategoryReq);
    }

    public Observable<BaseResp<ProductListMo>> getProductList(ProductListReq productListReq){
        return apiService.getProductList(productListReq);
    }

    @Override
    public Observable<BaseResp<List<ProductListMo.Record>>> getProductListBySkuIds(ProductListBySkuIdsReq skuIds) {
        return apiService.getProductListBySkuIds(skuIds);
    }

    public Observable<BaseResp<ProductListMo>> getProductModuleList(ProductModuleListReq productModuleListReq){
        return apiService.getProductModuleList(productModuleListReq);
    }

    public Observable<BaseResp<ProDetailResp>> getProDetailInfo(@Body ProDetailReq proDetailReq){
        return apiService.getProDetailInfo(proDetailReq);
    }

    public Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq){
        return apiService.getPublishedVersionInfo(publishReq);
    }

    public Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq){
        return apiService.getUpgradeVersionInfo(upgradeReq);
    }

    public Observable<BaseResp<FavoriteStateResp>> getFavoriteState(String objId) {
        return  apiService.getFavoriteState(objId);
    }

    public Observable<BaseResp<String>> uploadLog(UploadLogReq uploadLogReq) {
        return apiService.uploadLog(uploadLogReq);
    }

    public Observable<BaseResp<String>> startCourseware(CoursewareStartReq coursewareStartReq){
        return apiService.startCourseware(coursewareStartReq);
    }

    @Override
    public Observable<BaseResp<ServicePkgInfoResp>> getServicePackInfo(String deviceSn) {
        return apiService.getServicePackInfo(deviceSn);
    }

    @Override
    public Observable<BaseResp<ThemeInfoResp>> getThemeInfo(String deviceSn) {
        return apiService.getThemeInfo(deviceSn);
    }

    @Override
    public Observable<BaseResp<List<ProductListMo.Record>>> getProductRecommend(ProductRecommendReq productRecommendReq) {
        return apiService.getProductRecommend(productRecommendReq);
    }

    @Override
    public Observable<BaseResp<String>> getDeviceHealth(String deviceSn) {
        return apiService.getDeviceHealth(deviceSn);
    }

    @Override
    public Observable<BaseResp<String>> getDeviceOffline(String deviceSn) {
        return apiService.getDeviceOffline(deviceSn);
    }

    public Observable<ResponseBody> downloadSmallFile(String url){
        return apiService.downloadSmallFile(url);
    }
}

package com.zwn.launcher.utils;

import android.content.Context;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.CommonUtils;
import com.zwn.launcher.data.protocol.response.ModelInfoResp;
import com.zwn.launcher.data.protocol.response.ProDetailResp;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.launcher.data.protocol.response.AlgorithmInfoResp;
import com.zwn.launcher.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.UpgradeResp;

import java.util.List;

public class DownloadHelper {

    public static DownloadInfo buildDownloadInfo(Context context, ProDetailResp proDetailResp, PublishResp publishResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = publishResp.getSoftwareInfo().getSoftwareCode();
        downloadInfo.fileName = proDetailResp.getProductTitle();
        downloadInfo.fileImgUrl = proDetailResp.getUseImgUrl();
        downloadInfo.mainClassPath = publishResp.getSoftwareInfo().getSoftwareExtendInfo().getPackageName();
        downloadInfo.url = publishResp.getPackageUrl();
        downloadInfo.version = publishResp.getSoftwareVersion();
        downloadInfo.type = BaseConstants.DownloadFileType.PLUGIN_APP;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);
        downloadInfo.packageMd5 = publishResp.getPackageMd5();
        downloadInfo.extraId = proDetailResp.getSkuId();
        downloadInfo.relyIds = getRelayLibIds(publishResp.getRelevancyAlgorithmVersions());
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static String getRelayLibIds(List<AlgorithmInfoResp> algorithmInfoList){
        StringBuilder relyIdsBuilder = new StringBuilder();
        if(algorithmInfoList != null) {
            for (int i = 0; i < algorithmInfoList.size(); i++) {
                relyIdsBuilder.append(",");
                relyIdsBuilder.append(algorithmInfoList.get(i).versionId);
            }
            if (relyIdsBuilder.length() > 0) {
                relyIdsBuilder.deleteCharAt(0);
            }
        }
        return relyIdsBuilder.toString();
    }

    public static DownloadInfo buildUpgradeDownloadInfo(Context context, ProDetailResp proDetailResp, PublishResp publishResp, UpgradeResp upgradeResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = publishResp.getSoftwareInfo().getSoftwareCode();
        downloadInfo.fileName = proDetailResp.getProductTitle();
        downloadInfo.fileImgUrl = proDetailResp.getUseImgUrl();
        downloadInfo.mainClassPath = publishResp.getSoftwareInfo().getSoftwareExtendInfo().getPackageName();
        downloadInfo.url = upgradeResp.getPackageUrl();
        downloadInfo.version = upgradeResp.getSoftwareVersion();
        downloadInfo.type = BaseConstants.DownloadFileType.PLUGIN_APP;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);
        downloadInfo.packageMd5 = upgradeResp.getPackageMd5();
        downloadInfo.extraId = proDetailResp.getSkuId();
        downloadInfo.relyIds = getRelayLibIds(upgradeResp.getRelevancyAlgorithmVersions());
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static DownloadInfo buildAlgorithmDownloadInfo(Context context, AlgorithmInfoResp algorithmInfoResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = algorithmInfoResp.versionId;
        downloadInfo.fileName = "";
        downloadInfo.fileImgUrl = "";
        downloadInfo.mainClassPath = "";
        downloadInfo.url = algorithmInfoResp.packageUrl;
        downloadInfo.version = algorithmInfoResp.softwareVersion;
        downloadInfo.type = BaseConstants.DownloadFileType.SHARE_LIB;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);
        downloadInfo.packageMd5 = algorithmInfoResp.packageMd5;
        downloadInfo.extraId = "";
        downloadInfo.relyIds = getRelayModelIds(algorithmInfoResp.relevancyModelVersions);
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static String getRelayModelIds(List<ModelInfoResp> modelVersionsList){
        StringBuilder relyIdsBuilder = new StringBuilder();
        if(modelVersionsList != null) {
            for (int i = 0; i < modelVersionsList.size(); i++) {
                relyIdsBuilder.append(",");
                relyIdsBuilder.append(modelVersionsList.get(i).versionId);
            }
            if (relyIdsBuilder.length() > 0) {
                relyIdsBuilder.deleteCharAt(0);
            }
        }
        return relyIdsBuilder.toString();
    }

    public static DownloadInfo buildModelDownloadInfo(Context context, ModelInfoResp modelInfoResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = modelInfoResp.versionId;
        downloadInfo.fileName = modelInfoResp.modelFileName;
        downloadInfo.fileImgUrl = "";
        downloadInfo.mainClassPath = "";
        downloadInfo.url = modelInfoResp.packageUrl;
        downloadInfo.version = modelInfoResp.softwareVersion;
        downloadInfo.type = BaseConstants.DownloadFileType.MODEL_BIN;
        downloadInfo.filePath = CommonUtils.getModelStorePath(modelInfoResp.modelFileName, context);
        downloadInfo.packageMd5 = modelInfoResp.packageMd5;
        downloadInfo.extraId = "";
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static DownloadInfo buildHostUpgradeDownloadInfo(Context context, UpgradeResp upgradeResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = BaseConstants.HOST_APP_SOFTWARE_CODE;
        downloadInfo.fileName = "ZeeLauncher";
        downloadInfo.fileImgUrl = "";
        downloadInfo.mainClassPath = "com.zwn.launcher.MainActivity";
        downloadInfo.url = upgradeResp.getPackageUrl();
        downloadInfo.version = upgradeResp.getSoftwareVersion();
        downloadInfo.type = BaseConstants.DownloadFileType.HOST_APP;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);;
        downloadInfo.packageMd5 = upgradeResp.getPackageMd5();
        downloadInfo.extraId = "";
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static DownloadInfo buildManagerUpgradeDownloadInfo(Context context, UpgradeResp upgradeResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = BaseConstants.MANAGER_APP_SOFTWARE_CODE;
        downloadInfo.fileName = "ZeeManager";
        downloadInfo.fileImgUrl = "";
        downloadInfo.mainClassPath = BaseConstants.MANAGER_PACKAGE_NAME;
        downloadInfo.url = upgradeResp.getPackageUrl();
        downloadInfo.version = upgradeResp.getSoftwareVersion();
        downloadInfo.type = BaseConstants.DownloadFileType.MANAGER_APP;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);;
        downloadInfo.packageMd5 = upgradeResp.getPackageMd5();
        downloadInfo.extraId = "";
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static DownloadInfo buildCommonAppDownloadInfo(Context context, UpgradeResp upgradeResp, String softwareCode){
        DownloadInfo downloadInfo = new DownloadInfo();
        if(BaseConstants.SETTINGS_APP_SOFTWARE_CODE.equals(softwareCode)){
            downloadInfo.fileId = BaseConstants.SETTINGS_APP_SOFTWARE_CODE;
            downloadInfo.fileName = "ZeeSettings";
            downloadInfo.mainClassPath = BaseConstants.SETTINGS_APP_PACKAGE_NAME;
        }else if(BaseConstants.ZEE_GESTURE_AI_APP_SOFTWARE_CODE.equals(softwareCode)){
            downloadInfo.fileId = BaseConstants.ZEE_GESTURE_AI_APP_SOFTWARE_CODE;
            downloadInfo.fileName = "ZeeGestureAI";
            downloadInfo.mainClassPath = BaseConstants.ZEE_GESTURE_AI_APP_PACKAGE_NAME;
        }
        downloadInfo.fileImgUrl = "";
        downloadInfo.url = upgradeResp.getPackageUrl();
        downloadInfo.version = upgradeResp.getSoftwareVersion();
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);;
        downloadInfo.packageMd5 = upgradeResp.getPackageMd5();
        downloadInfo.extraId = "";
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static DownloadInfo buildHostPluginDownloadInfo(Context context, PublishResp publishResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = publishResp.getSoftwareInfo().getSoftwareCode();
        downloadInfo.fileName = "";
        downloadInfo.fileImgUrl = "";
        downloadInfo.mainClassPath = publishResp.getSoftwareInfo().getSoftwareExtendInfo().getPackageName();
        downloadInfo.url = publishResp.getPackageUrl();
        downloadInfo.version = publishResp.getSoftwareVersion();
        downloadInfo.type = BaseConstants.DownloadFileType.HOST_PLUGIN_PKG;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);
        downloadInfo.packageMd5 = publishResp.getPackageMd5();
        downloadInfo.extraId = "";
        downloadInfo.relyIds = "";
        downloadInfo.describe = "";
        return downloadInfo;
    }

    public static DownloadInfo buildHostPluginUpgradeDownloadInfo(Context context, String fileId, String mainClassPath, UpgradeResp upgradeResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = fileId;
        downloadInfo.fileName = "";
        downloadInfo.fileImgUrl = "";
        downloadInfo.mainClassPath = mainClassPath;
        downloadInfo.url = upgradeResp.getPackageUrl();
        downloadInfo.version = upgradeResp.getSoftwareVersion();
        downloadInfo.type = BaseConstants.DownloadFileType.HOST_PLUGIN_PKG;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);;
        downloadInfo.packageMd5 = upgradeResp.getPackageMd5();
        downloadInfo.extraId = "";
        downloadInfo.describe = "";
        return downloadInfo;
    }

}

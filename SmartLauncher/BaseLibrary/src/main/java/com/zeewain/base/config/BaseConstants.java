package com.zeewain.base.config;

import android.os.Environment;

public class BaseConstants {

    public static final String PACKAGE_INSTALLED_ACTION = "plugin.install.SESSION_API_PACKAGE_INSTALLED";
    public static final String MANAGER_INSTALLED_ACTION = "manager.launcher.SESSION_API_PACKAGE_INSTALLED";
    public static final String EXTRA_REGISTER = "Register";
    public static final String EXTRA_UPGRADE_INFO = "UpgradeInfo";
    public static final String EXTRA_PLUGIN_NAME = "PluginName";
    public static final String EXTRA_PLUGIN_FILE_PATH = "PluginFilePath";
    public static final String EXTRA_AUTH_AK_CODE = "AuthAkCode";
    public static final String EXTRA_AUTH_SK_CODE = "AuthSkCode";
    public static final String EXTRA_SKU_NAME = "SkuName";
    public static final String EXTRA_SKU_URL = "SkuUrl";
    public static final String EXTRA_HOST_PKG = "HostPkg";
    public static final String EXTRA_AUTH_URI = "AuthUri";
    public static final String EXTRA_AUTH_TOKEN = "AuthToken";
    public static final String EXTRA_MODELS_DIR_PATH = "ModelsDirPath";
    public static final String EXTRA_LICENSE_PATH = "LicensePath";
    public static final String EXTRA_LICENSE_CONTENT = "LicenseContent";
    public static final String EXTRA_DONE_TO_PACKAGE_NAME = "DoneToPackageName";
    public static final String EXTRA_DONE_TO_CLASS_NAME = "DoneToClassName";
    public static final String DONE_TO_CLASS_NAME = "com.zwn.launcher.MainActivity";

    public static final String EXTRA_APK_PATH = "ApkPath";
    public static final String EXTRA_APK_INSTALL_RESULT = "ApkInstallResult";

    public static final String AUTH_SYSTEM_CODE = "ZWN_AIIP_003";
    public static final String HOST_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_005";
    public static final String MANAGER_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_090";
    public static final String SETTINGS_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_091";
    public static final String SETTINGS_APP_PACKAGE_NAME = "com.zee.setting";

    //used for third party app default enable all Permission
    public static final String PERSIST_SYS_PERMISSION_PKG = "persist.sys.zeewain.pkgs";

    public static final String PLUGIN_MODEL_PATH = Environment.getExternalStorageDirectory() + "/.system/models";
    public static final String PRIVATE_DATA_PATH = Environment.getExternalStorageDirectory() + "/.system";
    public static final String LICENSE_FILE_PATH = PRIVATE_DATA_PATH + "/zeewain";
    public static final String LICENSE_V2_FILE_PATH = PRIVATE_DATA_PATH + "/zeewainV2";

    public static final String MANAGER_PACKAGE_NAME = "com.zee.manager";
    public static final String MANAGER_INSTALL_ACTIVITY = "com.zee.manager.InstallActivity";
    public static final String MANAGER_SERVICE_ACTION = "com.zee.manager.service";

    public static final String ZEE_SETTINGS_ACTIVITY_ACTION = "com.zee.setting.START_SETTINGS_ACTION";
    public static final String ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION = "com.zee.setting.SHOW_AGREEMENT_ACTION";
    public static final String EXTRA_ZEE_SETTINGS_AGREEMENT_CODE = "agreementCode";

    public static class AgreementCode{
        public static final String CODE_PRIVACY_AGREEMENT = "AISPACE_APP_LAW_PRIVACY_POLOICY";
        public static final String CODE_USER_AGREEMENT = "AISPACE_APP_NONAGE_PRIVACY_POLOICY";
    }

    /**
     * use for unity Courseware
     */
    public static final String EXTRA_SHOW_ACTION = "ShowAction";
    public static class ShowCode{
        public static int CODE_CAMERA_ERROR = 1;
        public static int CODE_CAMERA_INVALID = 2;
    }

    public static class DownloadFileType{
        public static final int MODEL_BIN = -2;
        public static final int SHARE_LIB = -1;
        public static final int HOST_APP = 0;
        public static final int PLUGIN_APP = 1;
        public static final int HOST_PLUGIN_PKG = 2;
        public static final int MANAGER_APP = 10;
        public static final int SETTINGS_APP = 11;
    }

    public static class ApiPath{
        public static final String SERVICE_PACKAGE_INFO = "/dmsmgr/purchase/device/servicePackInfo";
        public static final String PRODUCT_RECOMMEND_LIST = "/dmsmgr/purchase/device/recommend";
        public static final String PRODUCT_ONLINE_QUERY_LIST = "/product/online/query/list";
        public static final String PRODUCT_DETAIL = "/product/online/detail";
        public static final String SW_VERSION_LATEST = "/software/version/latest-published";
        public static final String SW_VERSION_NEWER = "/software/version/newer-published";

        public static final String USER_FAVORITES_PAGE_LIST = "/ums/favorite/courseware/page";
        public static final String USER_FAVORITES_ITEM_INFO = "/ums/favorite/courseware/info";
        public static final String USER_PLAY_RECORD_LIST = "/ums/playHistory/record";
    }

    public static final int API_HANDLE_SUCCESS = 0;

    //正式环境&测试环境使用
    public static final boolean buildRelease = true;
    public static final String baseUrl = buildRelease ? "https://aiip.zeewain.com" : "https://www.zeewain.com";
    public static final String basePath = buildRelease ? "/api" : "/aiip-test/api";

    //开发环境使用
    /*public static final String baseUrl = "https://www.zeewain.com";
    public static final String basePath = "/aiip-debug/api";*/

}

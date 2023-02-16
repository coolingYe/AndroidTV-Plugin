package com.zwn.launcher;

import android.content.Context;
import android.content.res.Configuration;

import com.danikula.videocache.HttpProxyCacheServer;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginConfig;
import com.zeewain.base.BaseApplication;
import com.zwn.launcher.host.HostManager;
import com.zwn.launcher.service.ZeeServiceManager;
import com.zwn.lib_download.db.CareController;


public class ZeeApplication extends BaseApplication {

    private HttpProxyCacheServer proxy;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        RePluginConfig rePluginConfig = new RePluginConfig();
        rePluginConfig.setUseHostClassIfNotFound(true);
        // FIXME RePlugin默认会对安装的外置插件进行签名校验，这里先关掉，避免调试时出现签名错误
        rePluginConfig.setVerifySign(!BuildConfig.DEBUG);

        // FIXME 若宿主为Release，则此处应加上您认为"合法"的插件的签名，例如，可以写上"宿主"自己的。
        if(!BuildConfig.DEBUG)
            RePlugin.addCertSignature("8DDB342F2DA5408402D7568AF21E29F9");
        RePlugin.App.attachBaseContext(this, rePluginConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;

        RePlugin.App.onCreate();

        CareController.init(this);
        ZeeServiceManager.init(this);

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        RePlugin.registerGlobalBinder("HostManager", new HostManager());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        RePlugin.App.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        RePlugin.App.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        RePlugin.App.onConfigurationChanged(config);
    }


    public static HttpProxyCacheServer getProxy(Context context) {
        ZeeApplication app = (ZeeApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }
}

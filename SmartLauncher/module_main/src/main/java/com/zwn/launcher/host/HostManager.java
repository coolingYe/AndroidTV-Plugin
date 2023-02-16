package com.zwn.launcher.host;


import android.content.Intent;
import android.os.RemoteException;

import com.zeewain.base.utils.CommonUtils;
import com.zwn.launcher.service.ZeeServiceManager;

public class HostManager extends IHostManager.Stub {

    @Override
    public void installPlugin(String fileId) throws RemoteException {
        if(!ZeeServiceManager.isInQueue(fileId)) {
            ZeeServiceManager.addToQueueNextCheckInstall(fileId);
        }
    }

    @Override
    public String getInstallingFileId() throws RemoteException {
        return ZeeServiceManager.installingFileId;
    }

    @Override
    public String getLastPluginPackageName() throws RemoteException {
        return ZeeServiceManager.lastPluginPackageName;
    }

    @Override
    public void setLastPluginPackageName(String packageName) throws RemoteException {
        ZeeServiceManager.lastPluginPackageName = packageName;
    }

    @Override
    public String getLastUnzipDonePlugin() throws RemoteException {
        return ZeeServiceManager.lastUnzipDonePlugin;
    }

    @Override
    public void setLastUnzipDonePlugin(String fileId) throws RemoteException {
        ZeeServiceManager.lastUnzipDonePlugin = fileId;
    }

    @Override
    public void logoutClear() throws RemoteException {
        CommonUtils.logoutClear();
    }

    @Override
    public void gotoLoginPage() throws RemoteException {

    }
}

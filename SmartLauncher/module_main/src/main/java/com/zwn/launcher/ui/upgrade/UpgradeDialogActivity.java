package com.zwn.launcher.ui.upgrade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.widgets.GradientProgressView;
import com.zwn.launcher.R;
import com.zwn.launcher.data.protocol.response.UpgradeResp;
import com.zwn.launcher.dialog.UpgradeTipDialog;
import com.zwn.launcher.service.ZeeServiceManager;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;

public class UpgradeDialogActivity extends BaseActivity {

    private TextView titleTextView;
    private TextView progressTextView;
    private TextView positiveView;
    private TextView cancelView;
    private LinearLayout positiveCancelLayout;
    private GradientProgressView gradientProgressView;

    public DownloadService.DownloadBinder downloadBinder = null;
    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            if (fileId.equals(BaseConstants.HOST_APP_SOFTWARE_CODE)) {
                runOnUiThread(() -> {
                    if(positiveCancelLayout != null && positiveCancelLayout.getVisibility() == View.VISIBLE){
                        positiveCancelLayout.setVisibility(View.GONE);
                    }
                    if (progress < 100) {
                        setTitleText("正在更新");
                        setLoadingProgress(progress);
                    } else if (progress == 100) {
                        finish();
                    }
                });
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {

        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(BaseConstants.HOST_APP_SOFTWARE_CODE)) {
                runOnUiThread(() -> {
                    setTitleText("网络异常");
                    if(positiveCancelLayout != null){
                        positiveCancelLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        @Override
        public void onPaused(String fileId) {}

        @Override
        public void onCancelled(String fileId) {}

        @Override
        public void onUpdate(String fileId) {}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_upgrade_dialog);
        setFinishOnTouchOutside(false);
        titleTextView = findViewById(R.id.txt_title_progress);
        progressTextView = findViewById(R.id.txt_value_progress);

        positiveView = findViewById(R.id.txt_positive_progress);
        positiveView.setOnClickListener(v -> {
            if(downloadBinder != null){
                setTitleText("正在更新");
                downloadBinder.startDownload(BaseConstants.HOST_APP_SOFTWARE_CODE);
            }
        });

        cancelView = findViewById(R.id.txt_cancel_progress);
        cancelView.setOnClickListener(v -> {
            CommonUtils.startSettingsActivity(v.getContext());
        });

        positiveCancelLayout = findViewById(R.id.ll_positive_cancel_progress);

        gradientProgressView = findViewById(R.id.gradient_progress_view);

        setTitleText("正在更新");

        downloadBinder = ZeeServiceManager.getInstance().getDownloadBinder();
        if(downloadBinder == null){
            finish();
            return;
        }
        ZeeServiceManager.getInstance().registerDownloadListener(downloadListener);

        Bundle bundle = getIntent().getBundleExtra(BaseConstants.EXTRA_UPGRADE_INFO);
        UpgradeResp upgradeResp = (UpgradeResp) bundle.getSerializable(BaseConstants.EXTRA_UPGRADE_INFO);
        handleUpgrade(upgradeResp);
    }

    @SuppressLint("DefaultLocale")
    public void setLoadingProgress(int progress){
        if(gradientProgressView != null) {
            gradientProgressView.setProgress(progress);
            progressTextView.setText(String.format("%d/100", progress));
        }
    }

    public void setTitleText(String titleText){
        if(titleTextView != null)
            titleTextView.setText(titleText);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        ZeeServiceManager.getInstance().unRegisterDownloadListener(downloadListener);
        super.onDestroy();
    }

    public void handleUpgrade(final UpgradeResp upgradeResp) {
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
        if(downloadInfo != null){
            if(downloadInfo.version.equals(upgradeResp.getSoftwareVersion())){//mean already add
                if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()){
                        if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(file))) {
                            ZeeServiceManager.getInstance().handleHostInstall(downloadInfo.filePath, downloadInfo.fileId);
                            finish();
                        }else{
                            if(file.delete() && CareController.instance.deleteDownloadInfo(downloadInfo.fileId) > 0){
                                downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
                            }else{
                                showToast("删除损坏的升级包失败！");
                                finish();
                            }
                        }
                    }else{//something wrong?
                        downloadBinder.startDownload(downloadInfo);
                    }
                }else if(downloadInfo.status == DownloadInfo.STATUS_STOPPED){
                    downloadBinder.startDownload(downloadInfo);
                }
            }else{//old version in db
                downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
            }
        }else{
            downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
        }
    }

    public static void showUpgradeTipDialog(final Context context, final UpgradeResp upgradeResp) {
        final UpgradeTipDialog customNormalDialog = new UpgradeTipDialog(context);
        customNormalDialog.setTitleText("检测到新版本");
        customNormalDialog.setMessageText("V" + upgradeResp.getSoftwareVersion());
        customNormalDialog.setPositiveText("升级");
        customNormalDialog.setCancelText("取消");

        if (upgradeResp.isForcible()) {
            customNormalDialog.setConfirmText("升级");
        }
        customNormalDialog.setOnClickListener(new UpgradeTipDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {
                customNormalDialog.cancel();
                showUpgradeDialog(context, upgradeResp);
            }

            @Override
            public void onPositive(View v) {
                customNormalDialog.cancel();
                showUpgradeDialog(context, upgradeResp);
            }

            @Override
            public void onCancel(View v) {
                customNormalDialog.cancel();
            }
        });
        customNormalDialog.show();
    }

    public static void showUpgradeDialog(Context context, final UpgradeResp upgradeResp){
        Intent intent = new Intent();
        intent.setClass(context, UpgradeDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BaseConstants.EXTRA_UPGRADE_INFO, upgradeResp);
        intent.putExtra(BaseConstants.EXTRA_UPGRADE_INFO, bundle);
        context.startActivity(intent);
    }
}
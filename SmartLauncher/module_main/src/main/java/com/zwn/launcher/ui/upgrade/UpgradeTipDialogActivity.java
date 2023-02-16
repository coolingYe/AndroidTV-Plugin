package com.zwn.launcher.ui.upgrade;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.LoadingView;
import com.zwn.launcher.R;
import com.zwn.launcher.data.DataRepository;


public class UpgradeTipDialogActivity extends BaseActivity implements View.OnFocusChangeListener {
    private TextView titleView;
    private TextView messageView;
    private MaterialCardView positiveView;
    private MaterialCardView cancelView;
    private TextView confirmView;
    private ConstraintLayout positiveCancelLayout;
    private ConstraintLayout clUpgradeTipRoot;
    private LoadingView loadingViewUpgradeTip;
    private UpgradeTipViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_upgrade_tip);
        setFinishOnTouchOutside(false);

        UpgradeTipViewModelFactory factory = new UpgradeTipViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(UpgradeTipViewModel.class);

        titleView = findViewById(R.id.txt_title_dialog);
        messageView = findViewById(R.id.txt_message_dialog);
        positiveView = findViewById(R.id.card_positive_dialog);
        cancelView = findViewById(R.id.card_cancel_dialog);
        confirmView = findViewById(R.id.txt_confirm_dialog);

        positiveCancelLayout = findViewById(R.id.layout_positive_cancel_dialog);

        clUpgradeTipRoot = findViewById(R.id.cl_upgrade_tip_root);
        loadingViewUpgradeTip = findViewById(R.id.loadingView_upgrade_tip);

        positiveView.setOnFocusChangeListener(this);
        cancelView.setOnFocusChangeListener(this);

        positiveView.setOnClickListener(v -> {
            UpgradeDialogActivity.showUpgradeDialog(this, viewModel.hostAppUpgradeResp);
            finish();
        });

        cancelView.setOnClickListener(v -> {
            finish();
        });

        confirmView.setOnClickListener(v -> {
            UpgradeDialogActivity.showUpgradeDialog(this, viewModel.hostAppUpgradeResp);
            finish();
        });

        initViewObservable();

        viewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
    }

    private void initViewObservable() {
        viewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                loadingViewUpgradeTip.stopAnim();
                loadingViewUpgradeTip.setVisibility(View.GONE);
                if(viewModel.hostAppUpgradeResp != null){
                    clUpgradeTipRoot.setVisibility(View.VISIBLE);
                    if(viewModel.hostAppUpgradeResp.isForcible()){
                        positiveCancelLayout.setVisibility(View.GONE);
                        confirmView.setVisibility(View.VISIBLE);
                    }else{
                        positiveCancelLayout.setVisibility(View.VISIBLE);
                        confirmView.setVisibility(View.GONE);
                    }
                }else{
                    showToast("已是最新版本了！");
                    delayFinish();
                }
            }else if(LoadState.Failed == loadState){
                loadingViewUpgradeTip.stopAnim();
                loadingViewUpgradeTip.setVisibility(View.GONE);
                showToast("网络异常！");
                delayFinish();
            }else{
                loadingViewUpgradeTip.setVisibility(View.VISIBLE);
                loadingViewUpgradeTip.startAnim();
            }
        });
    }

    private void delayFinish(){
        loadingViewUpgradeTip.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 600);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorYellow));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(view, 1.1f);
        } else {
            cardView.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
            cardView.setStrokeWidth(0);
            view.clearAnimation();
            CommonUtils.scaleView(view, 1f);
        }
    }
}
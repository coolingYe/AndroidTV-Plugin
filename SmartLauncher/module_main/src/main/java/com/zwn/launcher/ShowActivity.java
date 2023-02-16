package com.zwn.launcher;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;

public class ShowActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        int showActionCode = getIntent().getIntExtra(BaseConstants.EXTRA_SHOW_ACTION, -1);
        if(showActionCode > 0){
            setContentView(R.layout.activity_show);
            TextView textView = findViewById(R.id.txt_show_tip);
            if(showActionCode == BaseConstants.ShowCode.CODE_CAMERA_ERROR) {
                textView.setText("摄像头异常，请重新插拔USB摄像头或者重启设备！");
            }else if(showActionCode == BaseConstants.ShowCode.CODE_CAMERA_INVALID){
                textView.setText("未找到可使用的摄像头！");
            }else{
                textView.setText("未知错误！");
            }
            ConstraintLayout constraintLayout = findViewById(R.id.scl_confirm);
            constraintLayout.requestFocus();
            constraintLayout.setOnClickListener(v -> finish());
        }else{
            finish();
        }
    }
}
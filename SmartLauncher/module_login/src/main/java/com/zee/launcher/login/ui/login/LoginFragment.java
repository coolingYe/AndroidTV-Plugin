package com.zee.launcher.login.ui.login;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.login.BuildConfig;
import com.zee.launcher.login.R;
import com.zee.launcher.login.ui.LoginViewModel;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.ImageUtil;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.views.BaseDialog;


public class LoginFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
    private AppCompatEditText editLoginAccount;
    private AppCompatEditText editLoginPassword;
    private AppCompatEditText editLoginImgCode;
    private TextView txtLoginAccountTip;
    private TextView txtLoginPasswordTip;
    private ImageView imgLoginRefresh;
    private ImageView imgLoginImgCode;
    private LoginViewModel mViewModel;
    private Animation animation;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener(view);
        initObserve();
        mViewModel.reqImageCaptchaLogin();
    }

    private void initView(View view) {
        editLoginAccount = view.findViewById(R.id.edit_login_account);
        editLoginPassword = view.findViewById(R.id.edit_login_password);
        editLoginImgCode = view.findViewById(R.id.edit_login_img_code);

        txtLoginAccountTip = view.findViewById(R.id.txt_login_account_tip);
        txtLoginPasswordTip = view.findViewById(R.id.txt_login_password_tip);

        imgLoginRefresh = view.findViewById(R.id.img_login_refresh);
        imgLoginImgCode = view.findViewById(R.id.img_login_img_code);

        animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_loading_anim);
        LinearInterpolator interpolator = new LinearInterpolator();
        animation.setInterpolator(interpolator);

        if(BuildConfig.DEBUG){
            editLoginAccount.setText("zwy_test_001");
            editLoginPassword.setText("123456Aa");
        }

        if(mViewModel.usedUserActivateReq != null){
            editLoginAccount.setText(mViewModel.usedUserActivateReq.userCode);
            editLoginPassword.setText(mViewModel.usedUserActivateReq.userPwd);
        }
    }

    private void initListener(View view) {
        MaterialCardView cardLoginImgMain = view.findViewById(R.id.card_login_img_code_refresh);
        cardLoginImgMain.setOnClickListener(this);
        cardLoginImgMain.setOnFocusChangeListener(this);

        MaterialCardView cardLogin = view.findViewById(R.id.card_login);
        cardLogin.setOnClickListener(this);
        cardLogin.setOnFocusChangeListener(this);

        MaterialCardView cardCheckboxLoginPassword = view.findViewById(R.id.card_checkbox_login_password);
        cardCheckboxLoginPassword.setOnFocusChangeListener(this);
        CheckBox checkboxLoginPassword = view.findViewById(R.id.checkbox_login_password);
        cardCheckboxLoginPassword.setOnClickListener(v -> checkboxLoginPassword.setChecked(!checkboxLoginPassword.isChecked()));
        checkboxLoginPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editLoginPassword.setTransformationMethod(isChecked
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance());
            }
        });

        CardView cardLoginImgCode = view.findViewById(R.id.card_login_img_code);
        cardLoginImgCode.setOnClickListener(this);

        TextView txtLoginImgCodeRefresh = view.findViewById(R.id.txt_login_img_code_refresh);
        txtLoginImgCodeRefresh.setOnClickListener(this);

        editLoginAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    txtLoginAccountTip.setText("");
                }
            }
        });

        editLoginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtLoginPasswordTip.setText("");
            }
        });
    }

    private void handleUserPwdLogin(){
        if(mViewModel.imageCaptchaResp == null){
            mViewModel.reqImageCaptchaLogin();
            return;
        }

        String userCode = editLoginAccount.getText().toString();
        if(userCode.trim().isEmpty()){
            txtLoginAccountTip.setText("账号不能为空");
            editLoginAccount.requestFocus();
            return;
        }

        String password = editLoginPassword.getText().toString();
        if(password.trim().isEmpty()){
            txtLoginPasswordTip.setText("密码不能为空");
            editLoginPassword.requestFocus();
            return;
        }
        String errTip = mViewModel.checkPassword(password);
        if(errTip.length() > 0){
            txtLoginPasswordTip.setText("密码输入错误");
            editLoginPassword.requestFocus();
            return;
        }

        String imgCode = editLoginImgCode.getText().toString();
        if(imgCode.trim().isEmpty()){
            showToast("验证码不能为空");
            editLoginImgCode.requestFocus();
            return;
        }

        mViewModel.reqUserPwdLogin(userCode, password, CommonUtils.getDeviceSn(), mViewModel.imageCaptchaResp.uuid, imgCode);
    }

    public void startAnim(){
        imgLoginRefresh.clearAnimation();
        imgLoginRefresh.setAnimation(animation);
        animation.start();
    }

    public void stopAnim(){
        imgLoginRefresh.clearAnimation();
    }

    private void initObserve() {
        mViewModel.mldImgCodeReqState.observe(getViewLifecycleOwner(), loadState -> {
            if(LoadState.Success == loadState){
                stopAnim();
                imgLoginRefresh.setVisibility(View.GONE);
                imgLoginImgCode.setVisibility(View.VISIBLE);
                imgLoginImgCode.setImageBitmap(ImageUtil.stringToBitmap(mViewModel.imageCaptchaResp.img));
            }else if(LoadState.Loading == loadState){
                startAnim();
                imgLoginImgCode.setVisibility(View.GONE);
                imgLoginRefresh.setVisibility(View.VISIBLE);
            }else{
                stopAnim();
                imgLoginImgCode.setVisibility(View.GONE);
                imgLoginRefresh.setVisibility(View.VISIBLE);
            }
        });

    }

    private void showNetErrorDialog(Context context){
        final BaseDialog normalDialog = new BaseDialog(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_net_error, null, false);

        TextView txtTitle = view.findViewById(R.id.txt_net_error_title);
        txtTitle.setText("网络异常");

        MaterialCardView cardCancel = view.findViewById(R.id.card_net_error_cancel);
        cardCancel.setOnFocusChangeListener(this);
        cardCancel.setOnClickListener(v -> normalDialog.dismiss());

        MaterialCardView cardSure = view.findViewById(R.id.card_net_error_set);
        cardSure.setOnFocusChangeListener(this);
        TextView txtSure = view.findViewById(R.id.txt_net_error_set);
        txtSure.setText("设置");
        cardSure.setOnClickListener(v -> {
            normalDialog.dismiss();
            CommonUtils.startSettingsActivity(v.getContext());
        });
        normalDialog.setContentView(view);
        normalDialog.show();
    }

    @Override
    public void onDestroyView() {
        stopAnim();
        super.onDestroyView();
    }

    private void showToast(String msg){
        mViewModel.mldToastMsg.setValue(msg);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.card_login_img_code_refresh || v.getId() == R.id.card_login_img_code || v.getId() == R.id.txt_login_img_code_refresh) {
            if (!NetworkUtil.isNetworkAvailable(v.getContext())) {
                showNetErrorDialog(v.getContext());
            } else {
                mViewModel.reqImageCaptchaLogin();
            }
        } else if (v.getId() == R.id.card_login) {
            if (!NetworkUtil.isNetworkAvailable(v.getContext())) {
                showNetErrorDialog(v.getContext());
            } else {
                handleUserPwdLogin();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorBlue));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(v, 1.1f);
        } else {
            cardView.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }
}

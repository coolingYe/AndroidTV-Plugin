package com.zee.launcher.login.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.login.R;
import com.zee.launcher.login.data.UserRepository;
import com.zee.launcher.login.ui.login.LoginFragment;
import com.zee.launcher.login.ui.login.RegisterFragment;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.SPUtils;


public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private LoginFragment mLoginFragment;
    private RegisterFragment mRegisterFragment;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginViewModelProvider factory = new LoginViewModelProvider(UserRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_login);

        boolean isRegister = getIntent().getBooleanExtra(BaseConstants.EXTRA_REGISTER, false);

        if (isRegister) {
            mRegisterFragment = RegisterFragment.newInstance();
            replaceFragment(mRegisterFragment);
        } else {
            mLoginFragment = LoginFragment.newInstance();
            replaceFragment(mLoginFragment);
        }

        initViewObservable();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_login_root, fragment);
        transaction.commit();
    }

    private void initViewObservable() {
        viewModel.mldToastMsg.observe(this, msg -> showToast(msg));

        viewModel.mldUserActivateState.observe(this, loadState -> {
            if(LoadState.Success == loadState){
                hideLoadingDialog();
                mLoginFragment = LoginFragment.newInstance();
                replaceFragment(mLoginFragment);
            }else if(LoadState.Loading == loadState){
                showLoadingDialog();
            }else{
                hideLoadingDialog();
                viewModel.reqImageCaptchaRegister();
            }
        });

        viewModel.mldUserPwdLoginState.observe(this, new Observer<LoadState>() {
            @Override
            public void onChanged(LoadState loadState) {
                if(LoadState.Success == loadState){
                    hideLoadingDialog();
                    toMainActivity();
                }else if(LoadState.Loading == loadState){
                    showLoadingDialog();
                }else{
                    hideLoadingDialog();
                    viewModel.reqImageCaptchaLogin();
                }
            }
        });
    }

    private void toMainActivity(){
        try {
            SPUtils.getInstance().put(SharePrefer.GuideDone, true);
            Intent intent = new Intent(this, Class.forName("com.zwn.launcher.MainActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean isGuideDone = SPUtils.getInstance().getBoolean(SharePrefer.GuideDone);
            if(isGuideDone && mLoginFragment != null){
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
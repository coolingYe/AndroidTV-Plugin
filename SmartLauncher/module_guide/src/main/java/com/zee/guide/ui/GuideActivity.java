package com.zee.guide.ui;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;

import com.zee.guide.R;
import com.zee.guide.data.GuideRepository;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zee.guide.ui.start.StartFragment;

public class GuideActivity extends BaseActivity {

    private final int REQUEST_CODE_PERMISSIONS = 1;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
    };

    private GuideViewModel viewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GuideViewModelFactory factory = new GuideViewModelFactory(GuideRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(GuideViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_guide);

        initViewObservable();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);

        requestPermission();
    }

    private void initViewObservable() {
        viewModel.mldToastMsg.observe(this, msg -> showToast(msg));

        viewModel.mldDeviceInfoLoadState.observe(this, loadState -> {
            if(LoadState.Success == loadState){
                hideLoadingDialog();
                NavOptions navOptions = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .build();
                navController.navigate(R.id.nav_guide_activation_check, null, navOptions);
            }else if(LoadState.Loading == loadState){
                showLoadingDialog();
            }else{
                hideLoadingDialog();
            }
        });
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // ????????????????????????
            if (allPermissionsGranted()) {

            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {
                showToast("??????????????????");
            }
        }
    }

    @Override
    public void onBackPressed() {
        NavDestination currentDestination = navController.getCurrentDestination();
        if(currentDestination instanceof FragmentNavigator.Destination){
            FragmentNavigator.Destination destination = (FragmentNavigator.Destination) currentDestination;
            if(destination.getClassName().equals(StartFragment.class.getName())){
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            NavDestination currentDestination = navController.getCurrentDestination();
            if(currentDestination instanceof FragmentNavigator.Destination){
                FragmentNavigator.Destination destination = (FragmentNavigator.Destination) currentDestination;
                if(destination.getClassName().equals(StartFragment.class.getName())){
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
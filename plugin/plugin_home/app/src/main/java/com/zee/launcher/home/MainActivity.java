package com.zee.launcher.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.zee.launcher.home.adapter.CenterLayoutManager;
import com.zee.launcher.home.adapter.HMainTabItemDecoration;
import com.zee.launcher.home.adapter.MainTabAdapter;
import com.zee.launcher.home.adapter.MainViewPager2Adapter;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zeewain.base.widgets.TopBarView;
import com.zwn.launcher.host.HostManager;

public class MainActivity extends BaseActivity{

    private MainViewModel viewModel;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private LinearLayout llMainContent;
    private ViewPager2 viewPageMain;
    private RecyclerView recyclerViewMainTab;
    private MainTabAdapter mainTabAdapter;
    private MainViewPager2Adapter mainViewPager2Adapter;
    private CenterLayoutManager centerLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);

        loadingViewHomeClassic = findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = findViewById(R.id.networkErrView_home_classic);
        llMainContent = findViewById(R.id.ll_main_content);
        viewPageMain = findViewById(R.id.view_page_main);
        viewPageMain.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        viewPageMain.setFocusable(true);
        viewPageMain.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    RecyclerView recyclerView = (RecyclerView)viewPageMain.getChildAt(0);
                    if(recyclerView.getLayoutManager() != null) {
                        int position = viewPageMain.getCurrentItem();
                        View view = recyclerView.getLayoutManager().findViewByPosition(position);
                        boolean canFocus = view.requestFocus();
                        if(!canFocus){
                            recyclerViewMainTab.requestFocus();
                        }
                    }
                }
            }
        });

        View mainTabView = LayoutInflater.from(this).inflate(R.layout.layout_main_tab, null, false);
        recyclerViewMainTab = mainTabView.findViewById(R.id.recycler_view_main_tab);
        recyclerViewMainTab.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        recyclerViewMainTab.setFocusable(true);
        recyclerViewMainTab.requestFocus();
        recyclerViewMainTab.setNextFocusDownId(R.id.view_page_main);
        //recyclerViewMainTab = findViewById(R.id.recycler_view_main_tab);
        TopBarView topBarView = findViewById(R.id.top_bar_view);
        topBarView.addCenterView(mainTabView);
        topBarView.updateUserImg(HostManager.getHostSpString(SharePrefer.userAccount, "用户ID"));

        initListener();
        initViewObservable();

        HomeApplication.initHostData();

        viewModel.reqServicePackInfo();
    }

    private void initView(GlobalLayout globalLayout){
        if("vertical".equals(globalLayout.layout.basic.config.direction)){
            initVerticalLayout();
        }else{
            initHorizontalLayout();
        }

        mainTabAdapter = new MainTabAdapter(globalLayout.layout.pages);
        mainTabAdapter.setHasStableIds(true);
        mainTabAdapter.setOnSelectedListener(position -> {
            mainTabAdapter.setSelectedPosition(position);
            centerLayoutManager.smoothScrollToPosition(recyclerViewMainTab, new RecyclerView.State(), position);
            viewPageMain.setCurrentItem(position);
        });
        recyclerViewMainTab.setAdapter(mainTabAdapter);
        recyclerViewMainTab.requestFocus();

        mainViewPager2Adapter = new MainViewPager2Adapter(globalLayout.layout.pages, getSupportFragmentManager(), getLifecycle());
        viewPageMain.setAdapter(mainViewPager2Adapter);
    }

    private void initVerticalLayout(){
        llMainContent.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams layoutParams = recyclerViewMainTab.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        recyclerViewMainTab.setLayoutParams(layoutParams);
        //binding.recyclerViewMainTab.setVisibility(View.GONE);

        layoutParams = viewPageMain.getLayoutParams();
        layoutParams.height = 0;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        viewPageMain.setLayoutParams(layoutParams);

        centerLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMainTab.setLayoutManager(centerLayoutManager);
        recyclerViewMainTab.addItemDecoration(new HMainTabItemDecoration(DisplayUtil.dip2px(this, 20)));
    }

    private void initHorizontalLayout(){
        llMainContent.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams layoutParams = recyclerViewMainTab.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        recyclerViewMainTab.setLayoutParams(layoutParams);
        //binding.recyclerViewTab.setVisibility(View.GONE);

        layoutParams = viewPageMain.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        viewPageMain.setLayoutParams(layoutParams);
        viewPageMain.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPageMain.setUserInputEnabled(false);

        centerLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewMainTab.setLayoutManager(centerLayoutManager);
    }

    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> {
            viewModel.reqServicePackInfo();
        });

        viewPageMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(mainTabAdapter != null){
                    mainTabAdapter.setSelectedPosition(position);
                    centerLayoutManager.smoothScrollToPosition(recyclerViewMainTab, new RecyclerView.State(), position);
                    centerLayoutManager.findViewByPosition(position).requestFocus();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewObservable() {
        viewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                loadingViewHomeClassic.setVisibility(View.VISIBLE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                llMainContent.setVisibility(View.GONE);
                loadingViewHomeClassic.startAnim();
            }else if(LoadState.Success == loadState){
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                llMainContent.setVisibility(View.VISIBLE);
                initView(viewModel.globalLayout);
            }else{
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
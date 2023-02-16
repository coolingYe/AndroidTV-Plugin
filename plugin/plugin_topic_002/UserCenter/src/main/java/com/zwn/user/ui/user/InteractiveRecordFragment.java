package com.zwn.user.ui.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.model.LoadState;
import com.zeewain.base.views.CustomAlertDialog;
import com.zeewain.base.widgets.CenterGridLayoutManager;
import com.zwn.user.R;
import com.zwn.user.adapter.UserCommonAdapter;
import com.zwn.user.adapter.UserRecordListAdapter;
import com.zwn.user.data.model.UserPageCommonItem;

public class InteractiveRecordFragment extends BaseUserCenterFragment implements UserCommonAdapter.OnItemClickListener, UserCommonAdapter.OnRemoveItemListener {
    protected UserRecordListAdapter mAdapter;

    public static InteractiveRecordFragment newInstance() {
        return new InteractiveRecordFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initObserve();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.reqUserHistory();
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        tvUserCommEmpty.setText("暂无内容，快去体验课件叭~");

        mAdapter = new UserRecordListAdapter(mViewModel.historyResp, this, this);
        centerGridLayoutManager = new CenterGridLayoutManager(requireContext(),1, LinearLayoutManager.VERTICAL, false);
        rvUserCommPage.setLayoutManager(centerGridLayoutManager);
        rvUserCommPage.setAdapter(mAdapter);
    }

    @Override
    public void initListener() {
        super.initListener();
        ivUserCommPageDel.setOnClickListener(v -> {
            mDelMode = !mDelMode;
            mAdapter.setDelMode(mDelMode);
            ivUserCommPageDel.setImageResource(mDelMode ? R.mipmap.icon_delete_btn_selected : R.mipmap.icon_delete_btn);
        });

        ivUserCommPageDelAll.setOnClickListener(v -> {
            mDelAllMode = !mDelAllMode;
            mAdapter.setDelAllMode(mDelAllMode);
            ivUserCommPageDelAll.setImageResource(mDelAllMode ? R.mipmap.icon_delete_all_selected : R.mipmap.icon_delete_all);
            showClearAllDialog(v.getContext());
        });

        nevUserComm.setRetryClickListener(() -> {
            nevUserComm.setVisibility(View.INVISIBLE);
            lvUserCommLoading.setVisibility(View.VISIBLE);
            lvUserCommLoading.startAnim();
            mViewModel.reqUserHistory();
        });
    }

    protected void checkState() {
        if (mAdapter.getItemCount() == 0) {
            mDelMode = true;
            mDelAllMode = true;
            ivUserCommPageDel.performClick();
            ivUserCommPageDel.setVisibility(View.INVISIBLE);
            ivUserCommPageDelAll.setVisibility(View.INVISIBLE);
            ivUserCommSaturn.setVisibility(View.VISIBLE);
            tvUserCommEmpty.setVisibility(View.VISIBLE);
        } else {
            ivUserCommSaturn.setVisibility(View.INVISIBLE);
            tvUserCommEmpty.setVisibility(View.INVISIBLE);
            ivUserCommPageDel.setVisibility(View.VISIBLE);
            ivUserCommPageDelAll.setVisibility(View.VISIBLE);
        }
        if (mViewModel.historyResp != null) {
            tvUserCommonTab.setText(String.format("全部(%d)", mViewModel.historyResp.getTotal()));
        } else {
            tvUserCommonTab.setText("全部");
        }
    }

    private void initObserve() {
        mViewModel.pHistoryReqState.observe(getViewLifecycleOwner(), state -> {
            lvUserCommLoading.stopAnim();
            lvUserCommLoading.setVisibility(View.INVISIBLE);
            if (state == LoadState.Success) {
                rvUserCommPage.setVisibility(View.VISIBLE);
                mAdapter.updateData(mViewModel.historyResp);
                checkState();
            } else {
                nevUserComm.setVisibility(View.VISIBLE);
                rvUserCommPage.setVisibility(View.INVISIBLE);
            }
        });

        mViewModel.mldDelHistoryReqState.observe(getViewLifecycleOwner(), state -> {
            if (LoadState.Success == state.loadState) {
                mAdapter.updateData(mViewModel.historyResp);//TODO
                checkState();
            }
        });

        mViewModel.pClearHistoryReqState.observe(getViewLifecycleOwner(), state -> {
            if (state == LoadState.Success) {
                mAdapter.updateData(mViewModel.historyResp);
                checkState();
            }
        });
    }

    public void onBackFromDetails() {
        mViewModel.reqUserHistory();
    }


    @Override
    public void onItemClick(View view, int position, UserPageCommonItem commonItem) {
        if (position == RecyclerView.NO_POSITION || mDelMode) {
            return;
        }
        try {
            Intent intent = new Intent(getActivity(), Class.forName("com.zee.launcher.home.ui.detail.DetailActivity"));
            intent.putExtra("skuId", commonItem.skuId);
            startActivityForResult(intent, DETAIL_BACK_CODE);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRemove(View view, int position, UserPageCommonItem commonItem) {
        mViewModel.reqDelHistory(commonItem.skuId);
    }

    private void showClearAllDialog(Context context) {
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
        customAlertDialog.setMessageText("您确定要删除所有记录吗？");
        customAlertDialog.setMessageSummaryText("删除后将无法恢复");
        customAlertDialog.setOnClickListener(new CustomAlertDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {

            }

            @Override
            public void onPositive(View v) {
                customAlertDialog.dismiss();
                mViewModel.reqClearHistory();
            }

            @Override
            public void onCancel(View v) {
                customAlertDialog.dismiss();
                mDelAllMode = false;
                mAdapter.setDelAllMode(false);
                ivUserCommPageDelAll.setImageResource(R.mipmap.icon_delete_all);
            }
        });
        customAlertDialog.show();

    }
}
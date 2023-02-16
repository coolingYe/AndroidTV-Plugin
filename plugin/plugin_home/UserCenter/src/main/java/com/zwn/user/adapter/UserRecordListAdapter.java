package com.zwn.user.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zwn.user.R;
import com.zwn.user.data.model.UserPageCommonItem;
import com.zwn.user.data.protocol.response.HistoryResp;

import java.util.ArrayList;
import java.util.List;

public class UserRecordListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_RECORD_TODAY = 1;
    private static final int TYPE_RECORD_IN_WEEK = 2;
    private static final int TYPE_RECORD_THIS_MONTH = 3;
    private static final int TYPE_RECORD_EARLIER = 4;

    private final UserCommonAdapter.OnItemClickListener mOnItemClickListener;
    private final UserCommonAdapter.OnRemoveItemListener mOnRemoveItemListener;

    private boolean mDelMode = false;
    private HistoryResp historyResp;
    private List<Integer> recordTypeList;

    public UserRecordListAdapter(HistoryResp historyResp, UserCommonAdapter.OnItemClickListener onItemClickListener,
                                 UserCommonAdapter.OnRemoveItemListener onRemoveItemListener) {
        this.historyResp = historyResp;
        this.mOnItemClickListener = onItemClickListener;
        this.mOnRemoveItemListener = onRemoveItemListener;

        recordTypeList = new ArrayList<>();
        if(historyResp != null) {
            if (historyResp.today.size() > 0) {
                recordTypeList.add(TYPE_RECORD_TODAY);
            }
            if (historyResp.inAWeek.size() > 0) {
                recordTypeList.add(TYPE_RECORD_IN_WEEK);
            }
            if (historyResp.thisMonth.size() > 0) {
                recordTypeList.add(TYPE_RECORD_THIS_MONTH);
            }
            if (historyResp.earlier.size() > 0) {
                recordTypeList.add(TYPE_RECORD_EARLIER);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(HistoryResp historyResp){
        this.historyResp = historyResp;
        recordTypeList = new ArrayList<>();
        if(historyResp != null) {
            if (historyResp.today.size() > 0) {
                recordTypeList.add(TYPE_RECORD_TODAY);
            }
            if (historyResp.inAWeek.size() > 0) {
                recordTypeList.add(TYPE_RECORD_IN_WEEK);
            }
            if (historyResp.thisMonth.size() > 0) {
                recordTypeList.add(TYPE_RECORD_THIS_MONTH);
            }
            if (historyResp.earlier.size() > 0) {
                recordTypeList.add(TYPE_RECORD_EARLIER);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_record, parent, false);
        return new CommonGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(recordTypeList.get(position) == TYPE_RECORD_TODAY){
            ((CommonGridViewHolder)holder).bind(historyResp.today, "今天", mDelMode);
        }else if(recordTypeList.get(position) == TYPE_RECORD_IN_WEEK){
            ((CommonGridViewHolder)holder).bind(historyResp.inAWeek, "七天内", mDelMode);
        }else if(recordTypeList.get(position) == TYPE_RECORD_THIS_MONTH){
            ((CommonGridViewHolder)holder).bind(historyResp.thisMonth, "本月", mDelMode);
        }else {
            ((CommonGridViewHolder)holder).bind(historyResp.earlier, "更早", mDelMode);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return recordTypeList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return recordTypeList.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDelMode(boolean delMode) {
        mDelMode = delMode;
        notifyDataSetChanged();
    }

    private class CommonGridViewHolder extends RecyclerView.ViewHolder{
        private final RecyclerView recyclerViewRecordType;
        private final TextView txtTitleRecordType;
        private final UserCommonAdapter userCommonAdapter;

        public void bind(List<HistoryResp.Record> recordList, String title, boolean deleteMode){
            userCommonAdapter.clearData();
            for (HistoryResp.Record record: recordList) {
                UserPageCommonItem commonItem = new UserPageCommonItem(record.skuUrl,
                        record.skuName, String.valueOf(record.skuId));
                userCommonAdapter.addItem(commonItem);
            }
            recyclerViewRecordType.setAdapter(userCommonAdapter);
            txtTitleRecordType.setText(title);
            userCommonAdapter.setDelMode(deleteMode);
        }

        public CommonGridViewHolder(View view) {
            super(view);
            userCommonAdapter = new UserCommonAdapter();
            userCommonAdapter.setOnItemClickListener(mOnItemClickListener);
            userCommonAdapter.setOnRemoveItemListener(mOnRemoveItemListener);

            txtTitleRecordType = view.findViewById(R.id.txt_title_record_type);

            recyclerViewRecordType = view.findViewById(R.id.recycler_view_record_type);
            recyclerViewRecordType.setLayoutManager(new GridLayoutManager(view.getContext(), 5));
        }
    }
}
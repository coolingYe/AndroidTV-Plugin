package com.zwn.user.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.utils.DisplayUtil;
import com.zwn.user.R;
import com.zwn.user.data.model.UserCenterCategory;

import java.util.List;

public class UserCenterCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SELECTED = 1;
    private final List<UserCenterCategory> categoryList;
    public int selectedIndex = 0;
    private OnCategorySelectedListener onCategorySelectedListener;
    private OnSelectedListener onSelectedListener;
    private RecyclerView recyclerView;

    public UserCenterCategoryAdapter(List<UserCenterCategory> categoryList) {
        this.categoryList = categoryList;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SELECTED) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_center_category, parent, false);
            return new CategorySelectedViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_center_category, parent, false);
            return new CategoryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CategorySelectedViewHolder) {
            ((CategorySelectedViewHolder) holder).bind(categoryList.get(position), position);
        } else {
            ((CategoryViewHolder) holder).bind(categoryList.get(position), position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(selectedIndex == position){
            return TYPE_SELECTED;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return categoryList == null ? 0 : categoryList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener onCategorySelectedListener) {
        this.onCategorySelectedListener = onCategorySelectedListener;
    }

    public int getSelectedPosition() {
        return selectedIndex;
    }

    public void setFocusedPosition(int focusedPosition) {
        if(onSelectedListener != null){
            onSelectedListener.onSelected(focusedPosition);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedPosition(int selectedPosition) {
        this.selectedIndex = selectedPosition;
        notifyDataSetChanged();
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final MaterialCardView cardUserCenterCategory;
        public final LinearLayout llUserCenterCategory;
        public final ImageView imgUserCenterCategory;
        public final TextView txtUserCenterCategory;

        public void bind(UserCenterCategory userCenterCategory, int position) {
            llUserCenterCategory.setBackgroundColor(Color.TRANSPARENT);
            txtUserCenterCategory.setTextColor(0xFF333333);
            imgUserCenterCategory.setImageResource(userCenterCategory.iconResId);

            txtUserCenterCategory.setText(userCenterCategory.title);
            cardUserCenterCategory.setTag(position);
            cardUserCenterCategory.setOnClickListener(this);
        }

        public CategoryViewHolder(@NonNull View view) {
            super(view);
            cardUserCenterCategory = view.findViewById(R.id.card_user_center_category);
            llUserCenterCategory = view.findViewById(R.id.ll_user_center_category);
            imgUserCenterCategory = view.findViewById(R.id.img_user_center_category);
            txtUserCenterCategory = view.findViewById(R.id.txt_user_center_category);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onClick(View view) {
            if(view.getTag() != null){
                Integer selectedPosition = (Integer)view.getTag();
                selectedIndex = selectedPosition;
                if(onCategorySelectedListener != null ){
                    onCategorySelectedListener.onCategorySelected(selectedPosition);
                }
                notifyDataSetChanged();
            }
        }
    }

    class CategorySelectedViewHolder extends CategoryViewHolder {
        private int strokeWidth = 0;

        public CategorySelectedViewHolder(@NonNull View view) {
            super(view);
            strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
        }

        public void bind(UserCenterCategory userCenterCategory, int position) {
            txtUserCenterCategory.setTextColor(0xFFFA8219);
            imgUserCenterCategory.setImageResource(userCenterCategory.iconSelectedResId);
            if (recyclerView.hasFocus()) {
                llUserCenterCategory.setBackgroundResource(R.drawable.shape_radius5_color19fa8219);
                cardUserCenterCategory.setStrokeColor(0xFFFA701F);
                cardUserCenterCategory.setStrokeWidth(strokeWidth);
            } else {
                llUserCenterCategory.setBackgroundColor(Color.TRANSPARENT);
                cardUserCenterCategory.setStrokeColor(0x00FFFFFF);
                cardUserCenterCategory.setStrokeWidth(0);
            }
            txtUserCenterCategory.setText(userCenterCategory.title);
            cardUserCenterCategory.setTag(position);
            cardUserCenterCategory.setOnClickListener(this);
        }
    }

    public interface OnCategorySelectedListener{
        void onCategorySelected(int position);
    }

    public interface OnSelectedListener{
        void onSelected(int position);
    }
}

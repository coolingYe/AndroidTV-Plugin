package com.zwn.user.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.utils.GlideApp;
import com.zwn.user.R;
import com.zwn.user.data.model.UserPageCommonItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserCommonAdapter extends RecyclerView.Adapter<UserCommonAdapter.UserProductViewHolder> {
    private List<UserPageCommonItem> mItemList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private OnRemoveItemListener mOnRemoveItemListener;

    private boolean mDelMode = false;
    private boolean mDelAllMode = false;
    private Consumer<Integer> mOnItemFocusedListener;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private Runnable mOnItemKeyEventUp;
    private Runnable mOnItemKeyEventDown;

    @NonNull
    @Override
    public UserCommonAdapter.UserProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_product, parent, false);
        return new UserCommonAdapter.UserProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserCommonAdapter.UserProductViewHolder holder, int position) {
        holder.bind(mItemList.get(position), position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void addItem(UserPageCommonItem item) {
        mItemList.add(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItemList(List<UserPageCommonItem> itemList) {
        this.mItemList = itemList;
        notifyDataSetChanged();

    }

    public void setOnItemFocusedListener(Consumer<Integer> mOnItemFocusedListener) {
        this.mOnItemFocusedListener = mOnItemFocusedListener;
    }

    public void delItem(int position) {
        mItemList.remove(position);
    }

    public void delItemBySkuId(String skuId) {
        int index = -1;
        for (int i = 0; i < mItemList.size(); i++) {
            if (skuId.equals(mItemList.get(i).skuId)) {
                mItemList.remove(i);
                index = i;
                break;
            }
        }
        if (index >= 0) {
            notifyItemRemoved(index);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDelMode(boolean delMode) {
        mDelMode = delMode;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDelAllMode(boolean delAllMode) {
        mDelAllMode = delAllMode;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        mItemList.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, UserPageCommonItem commonItem);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnRemoveItemListener {
        void onRemove(View view, int position, UserPageCommonItem commonItem);
    }

    public void setOnRemoveItemListener(OnRemoveItemListener onRemoveItemListener) {
        mOnRemoveItemListener = onRemoveItemListener;
    }

    public void setOnItemKeyEventUpListener(Runnable mOnItemKeyEventUp) {
        this.mOnItemKeyEventUp = mOnItemKeyEventUp;
    }

    public void setOnItemKeyEventDownListener(Runnable mOnItemKeyEventDown) {
        this.mOnItemKeyEventDown = mOnItemKeyEventDown;
    }

    public Object getItem(int position) {
        return mItemList.get(position);
    }

    public class UserProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivItemUserProductImg;
        private final TextView tvItemUserProductTitle;
        private final ImageView ivItemUserProductDel;
        private final LinearLayout cardItemUserProduct;
        private final ImageView ivItemUserProductDelSwitch;

        public void bind(UserPageCommonItem commonItem, int position) {
            GlideApp.with(ivItemUserProductImg.getContext())
                    .load(commonItem.url)
                    .into(ivItemUserProductImg);
            ivItemUserProductDelSwitch.setVisibility(mDelAllMode ? View.VISIBLE : View.INVISIBLE);
            tvItemUserProductTitle.setText(commonItem.title);
            if (cardItemUserProduct.getNextFocusLeftId() == -1) {
                cardItemUserProduct.setNextFocusLeftId(R.id.recycler_view_user_center_category);
            }
            if (mOnItemClickListener != null) {
                cardItemUserProduct.setOnClickListener(v -> {
                    if (ivItemUserProductDel.getVisibility() == View.VISIBLE) {
                        ivItemUserProductDel.setImageResource(R.mipmap.icon_delete_selected);
                        if (mOnRemoveItemListener != null) {
                            mOnRemoveItemListener.onRemove(v, position, commonItem);
                        }
                    }
                    mOnItemClickListener.onItemClick(v, position, commonItem);
                });

            }
//            ivItemUserProductDel.setOnClickListener(v -> {
//                if (mOnRemoveItemListener != null) {
//                    mOnRemoveItemListener.onRemove(v, position, commonItem);
//                }
//            });
            cardItemUserProduct.setOnFocusChangeListener((v, hasFocus) -> {
                if (mDelMode) {
                    if (hasFocus) {
                        ivItemUserProductDel.setVisibility(View.VISIBLE);
                    } else {
                        ivItemUserProductDel.setVisibility(View.INVISIBLE);
                    }
                }
                if (hasFocus) {
                    if (mOnItemFocusedListener != null) {
                        mOnItemFocusedListener.accept(position);
                    }
                }
            });

            cardItemUserProduct.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (position <= 9) {
                        sHandler.post(mOnItemKeyEventUp);
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN){
                    if (position >= getItemCount() - 10) {
                        sHandler.post(mOnItemKeyEventDown);
                    }
                }
                return false;
            });
        }

        public UserProductViewHolder(View view) {
            super(view);
            ivItemUserProductDelSwitch = view.findViewById(R.id.iv_item_user_product_del_switch);
            ivItemUserProductImg = view.findViewById(R.id.iv_item_user_product_img);
            tvItemUserProductTitle = view.findViewById(R.id.tv_item_user_product_title);
            ivItemUserProductDel = view.findViewById(R.id.iv_item_user_product_del);
            cardItemUserProduct = view.findViewById(R.id.card_item_user_product);
        }
    }
}
package com.zee.launcher.home.ui.home.holder;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.GlideApp;


public class ClassicItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public final TextView txtTitle;
    public final TextView txtSummary;
    public final ImageView imageView;
    public final MaterialCardView cardRootLayout;
    public final CardView cardView;
    public OnItemFocusChange onItemFocusChange;

    public void bind(ProductListMo.Record record){
        GlideApp.with(imageView.getContext())
                .load(record.getProductImg())
                .into(imageView);
        if (record.getProductTitle() != null) {
            txtTitle.setText(record.getProductTitle());
        }
        if (record.getSimplerIntroduce()!= null) {
            txtSummary.setText(record.getSimplerIntroduce());
        }
        cardRootLayout.setTag(record);
    }

    public ClassicItemViewHolder(@NonNull View view) {
        super(view);
        final int strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
        txtTitle = view.findViewById(R.id.txt_type_classic_title);
        txtSummary = view.findViewById(R.id.txt_type_classic_summary);
        imageView = view.findViewById(R.id.img_type_classic);
        cardView = view.findViewById(R.id.cardView_type_classic);
        cardRootLayout = view.findViewById(R.id.card_type_classic_root);
        cardRootLayout.setOnClickListener(this);
        cardRootLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(onItemFocusChange != null){
                    onItemFocusChange.onFocusChange(v, hasFocus);
                }
                if(hasFocus){
                    cardRootLayout.setStrokeColor(0xFFFA701F);
                    cardRootLayout.setStrokeWidth(strokeWidth);
                    CommonUtils.scaleView(v, 1.1f);
                }else{
                    cardRootLayout.setStrokeColor(0x00FFFFFF);
                    cardRootLayout.setStrokeWidth(0);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            }
        });
    }

    public void setImageViewHeight(int height){
        ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
        layoutParams.height = height;
    }

    public void setTitleSummaryMargin(int titleTopMargin, int summaryTopMargin){
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)txtTitle.getLayoutParams();
        layoutParams.topMargin = titleTopMargin;

        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams)txtSummary.getLayoutParams();
        layoutParams2.topMargin = summaryTopMargin;
    }

    @Override
    public void onClick(View view) {
        if(view.getTag() != null){
            ProductListMo.Record record = (ProductListMo.Record)view.getTag();
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra("skuId",record.getSkuId());
            view.getContext().startActivity(intent);
        }
    }

    public void setOnItemFocusChange(OnItemFocusChange onItemFocusChange) {
        this.onItemFocusChange = onItemFocusChange;
    }

    public interface OnItemFocusChange{
        void onFocusChange(View v, boolean hasFocus);
    }
}
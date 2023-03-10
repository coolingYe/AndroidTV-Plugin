package com.zeewain.base.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.R;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;

public class NetworkErrView extends ConstraintLayout implements View.OnFocusChangeListener {
    private MaterialCardView cardRetryBtn;
    private MaterialCardView cardNetworkSetBtn;
    private RetryClickListener retryClickListener = null;

    public NetworkErrView(@NonNull Context context) {
        this(context, null);
    }

    public NetworkErrView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkErrView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_network_err, this);
        cardRetryBtn = findViewById(R.id.card_network_retry);
        cardRetryBtn.setOnFocusChangeListener(this);
        cardRetryBtn.setOnClickListener(v -> {
            if(retryClickListener != null){
                retryClickListener.onRetryClick();
            }
        });

        cardNetworkSetBtn = findViewById(R.id.card_network_set);
        cardNetworkSetBtn.setOnFocusChangeListener(this);
        cardNetworkSetBtn.setOnClickListener(v -> {
            CommonUtils.startSettingsActivity(v.getContext());
        });
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){
            cardRetryBtn.requestFocus();
        }
    }

    public void setRetryClickListener(RetryClickListener retryClickListener) {
        this.retryClickListener = retryClickListener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(0xFFFFFFFF);
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(v, 1.1f);
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    public interface RetryClickListener{
        void onRetryClick();
    }
}

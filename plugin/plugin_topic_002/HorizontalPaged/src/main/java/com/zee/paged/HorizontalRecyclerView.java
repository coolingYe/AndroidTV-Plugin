package com.zee.paged;

import android.content.Context;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class HorizontalRecyclerView extends RecyclerView {
    private float initialX = 0;
    private float initialY = 0;
    private OnHandleKeyEventListener onHandleKeyEventListener;
    private ViewPager2 parentViewPager = null;
    private RecyclerView parentRecyclerView = null;
    private boolean keepFlag = false;
    private int mlastFocusPosition = 0;

    public HorizontalRecyclerView(@NonNull Context context) {
        super(context);
    }

    public HorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();

    }

    private void initView() {
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);

        setClipChildren(false);
        setClipToPadding(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        executeKeyEvent(event);
        if (onHandleKeyEventListener != null) {
            boolean isHandled = onHandleKeyEventListener.handleKeyEvent(event);
            if (isHandled) return true;
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                initialY = ev.getY();
                keepFlag = false;
                getParentView();
                requestParentDisallow(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float dealtX = ev.getX() - initialX;
                float dealtY = ev.getY() - initialY;

                if (Math.abs(dealtX) >= Math.abs(dealtY)) {
                    if (!keepFlag) {
                        keepFlag = true;
                        if (canScrollHorizontally(-(int) dealtX)) {
                            requestParentDisallow(true);
                        } else {
                            requestParentDisallow(false);
                        }
                    }
                } else {
                    requestParentDisallow(false);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;

        }

        return super.dispatchTouchEvent(ev);
    }

    public void requestParentDisallow(boolean enable) {
        if (parentViewPager != null) {
            parentViewPager.requestDisallowInterceptTouchEvent(enable);
        }

        if (parentRecyclerView != null) {
            parentRecyclerView.requestDisallowInterceptTouchEvent(enable);
        }
    }

    public void getParentView() {
        ViewParent parent = getParent();
        while (parent != null && !(parent instanceof ViewPager2)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            parentViewPager = (ViewPager2) parent;
        }

        parent = getParent();
        while (parent != null && !(parent instanceof RecyclerView)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            parentRecyclerView = (RecyclerView) parent;
        }
    }

    public void setOnHandleKeyEventListener(OnHandleKeyEventListener onHandleKeyEventListener) {
        this.onHandleKeyEventListener = onHandleKeyEventListener;
    }

    public interface OnHandleKeyEventListener {
        boolean handleKeyEvent(KeyEvent event);
    }

    public boolean executeKeyEvent(@NonNull KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = arrowScroll(FOCUS_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = arrowScroll(FOCUS_RIGHT);
                    break;
            }
        }
        return handled;
    }

    public boolean arrowScroll(int direction) {

        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        } else if (currentFocused != null) {
            boolean isChild = false;
            for (ViewParent parent = currentFocused.getParent(); parent instanceof ViewGroup;
                 parent = parent.getParent()) {
                if (parent == this) {
                    isChild = true;
                    break;
                }
            }
            if (!isChild) {
                // This would cause the focus search down below to fail in fun ways.
                final StringBuilder sb = new StringBuilder();
                sb.append(currentFocused.getClass().getSimpleName());
                for (ViewParent parent = currentFocused.getParent(); parent instanceof ViewGroup;
                     parent = parent.getParent()) {
                    sb.append(" => ").append(parent.getClass().getSimpleName());
                }
                currentFocused = null;
            }
        }

        boolean handled = false;

        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused,
                direction);
        if (nextFocused == null || nextFocused == currentFocused) {
            if (direction == FOCUS_LEFT || direction == FOCUS_RIGHT) {
                shakeX(currentFocused);
                handled = true;
            }
        }
        return handled;
    }

    private void shakeX(View currentFocused) {
        if (currentFocused != null && getScrollState() == SCROLL_STATE_IDLE) {
            currentFocused.clearAnimation();
            currentFocused.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake));
        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            mlastFocusPosition = getChildViewHolder(child).getAdapterPosition();
        }
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        View view = null;
        if (this.hasFocus() || mlastFocusPosition < 0 || (view = getLayoutManager().findViewByPosition(mlastFocusPosition)) == null) {
            super.addFocusables(views, direction, focusableMode);
        } else if (view.isFocusable()) {
            views.add(view);
        } else {
            super.addFocusables(views, direction, focusableMode);
        }
    }
}

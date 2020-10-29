package com.renj.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2020-10-29   09:51
 * <p>
 * 描述：
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class PullFlowLayout extends ViewGroup {
    private int viewContentHeight = 0;
    private int viewReallyHeight = 0;
    private Scroller mScroller;

    private int totalRowCount; // 总行数
    private List<ChildViewInfo> childViewList = new ArrayList<>(); // 所有子控件集合
    private PullFlowLayoutAdapter pullFlowLayoutAdapter;

    public PullFlowLayout(Context context) {
        this(context, null);
    }

    public PullFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PullFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context);
    }

    public void setAdapter(PullFlowLayoutAdapter pullFlowLayoutAdapter) {
        if (pullFlowLayoutAdapter != null) {
            this.pullFlowLayoutAdapter = pullFlowLayoutAdapter;
            pullFlowLayoutAdapter.setPullFlowLayout(this);
            requestLayout();
        }
    }

    public int getTotalRowCount() {
        return totalRowCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (pullFlowLayoutAdapter == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        viewContentHeight = heightSize - getPaddingTop() - getPaddingBottom();

        // 所有孩子控件都完全显示需要的高度，默认加上顶部的 padding 值
        int flowLayoutReallyHeight = getPaddingTop();
        int childCount = pullFlowLayoutAdapter.getViewCount();
        if (childCount > 0) {
            childViewList.clear();
            // 父控件可以存放子控件的内容，父控件的宽度，减去左右两边的 padding 值
            int flowLayoutContentWidth = widthSize - getPaddingLeft() - getPaddingRight();
            // 当前行已使用的宽度
            int currentRowWidth = getPaddingLeft();
            // 当前行的高度，以一行中最大高度的子控件高度为行高
            int currentRowMaxHeight = 0;
            // 总行数
            totalRowCount = 1;

            for (int i = 0; i < childCount; i++) {
                View childView = pullFlowLayoutAdapter.createView(getContext(), i, this);
                addView(childView);

                measureChild(childView, widthMeasureSpec, heightMeasureSpec);

                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) childView.getLayoutParams();
                int childViewLeftMargin = 0;
                int childViewTopMargin = 0;
                int childViewRightMargin = 0;
                int childViewBottomMargin = 0;
                if (marginLayoutParams != null) {
                    childViewLeftMargin = marginLayoutParams.leftMargin;
                    childViewTopMargin = marginLayoutParams.topMargin;
                    childViewRightMargin = marginLayoutParams.rightMargin;
                    childViewBottomMargin = marginLayoutParams.bottomMargin;
                }

                int measuredWidth = childView.getMeasuredWidth();
                int measuredHeight = childView.getMeasuredHeight();
                // 计算当前行已使用的宽度
                currentRowWidth += measuredWidth + childViewLeftMargin + childViewRightMargin;
                // 取一行最大高度为行高
                currentRowMaxHeight = Math.max(measuredHeight + childViewTopMargin + childViewBottomMargin, currentRowMaxHeight);
                // 换行
                if (currentRowWidth > flowLayoutContentWidth) {
                    currentRowWidth = getPaddingLeft() + measuredWidth + childViewLeftMargin + childViewRightMargin;
                    flowLayoutReallyHeight += currentRowMaxHeight;
                    currentRowMaxHeight = 0;
                    totalRowCount += 1;
                }

                // 确定当前子控件所在的位置
                ChildViewInfo childViewInfo = new ChildViewInfo(childView);
                childViewInfo.left = currentRowWidth - measuredWidth - childViewRightMargin;
                childViewInfo.top = flowLayoutReallyHeight + childViewTopMargin;
                childViewInfo.right = currentRowWidth - childViewRightMargin;
                childViewInfo.bottom = flowLayoutReallyHeight + childViewTopMargin + measuredHeight;
                childViewInfo.rowNumber = totalRowCount;
                childViewList.add(childViewInfo);
            }
            // 加上最后一行高度
            if (currentRowWidth != 0) {
                flowLayoutReallyHeight += currentRowMaxHeight;
            }
        }
        // 加上底部 padding 值
        flowLayoutReallyHeight += getPaddingBottom();
        viewReallyHeight = flowLayoutReallyHeight;

        // 确定高度
        if (heightMode == MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(flowLayoutReallyHeight, MeasureSpec.EXACTLY);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (childViewList.isEmpty()) return;

        for (ChildViewInfo childViewInfo : childViewList) {
            childViewInfo.onLayout();
        }
    }

    int lastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (viewReallyHeight > viewContentHeight) {
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                    int dy = lastY - y;
                    if (dy < 0) {
                        if (getScrollY() + dy < 0) {
                            dy = -getScrollY();
                        }
                    } else {
                        if (getScrollY() + dy + viewContentHeight > viewReallyHeight) {
                            dy = viewReallyHeight - viewContentHeight - getScrollY() - getPaddingBottom();
                        }
                    }

                    Log.i("Renj", "------ " + getScrollY() + " " + getPaddingTop() + "  " + getPaddingBottom());
                    Log.i("Renj", "viewReallyHeight " + viewReallyHeight + " viewContentHeight: " + viewContentHeight);

                    scrollBy(0, dy);
                    lastY = y;
                    break;
                case MotionEvent.ACTION_UP:
//                    if (getScrollY() > viewReallyHeight - viewContentHeight) {
//                        mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - (viewReallyHeight - viewContentHeight) - getPaddingTop()));
//                    } else if (getScrollY() < 0) {
//                        mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
//                    }
                    break;
            }
            postInvalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 子控件信息
     */
    private static class ChildViewInfo {
        View childView;
        int left;
        int top;
        int right;
        int bottom;

        int rowNumber; // 所在行

        public ChildViewInfo(View childView) {
            this.childView = childView;
        }

        public void onLayout() {
            childView.layout(left, top, right, bottom);
        }
    }
}

package com.renj.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
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
    private int mViewContentHeight = 0;
    private int mViewReallyHeight = 0;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private int mTotalRowCount; // 总行数
    private List<ChildViewInfo> mChildViewList = new ArrayList<>(); // 所有子控件集合
    private PullFlowLayoutAdapter mPullFlowLayoutAdapter;

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
        mVelocityTracker = VelocityTracker.obtain();
    }

    public void setAdapter(PullFlowLayoutAdapter pullFlowLayoutAdapter) {
        if (pullFlowLayoutAdapter != null) {
            this.mPullFlowLayoutAdapter = pullFlowLayoutAdapter;
            pullFlowLayoutAdapter.setPullFlowLayout(this);
            requestLayout();
        }
    }

    public int getTotalRowCount() {
        return mTotalRowCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mPullFlowLayoutAdapter == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        mViewContentHeight = heightSize - getPaddingTop() - getPaddingBottom();

        // 所有孩子控件都完全显示需要的高度，默认加上顶部的 padding 值
        int flowLayoutReallyHeight = getPaddingTop();
        int childCount = mPullFlowLayoutAdapter.getViewCount();
        if (childCount > 0) {
            mChildViewList.clear();
            // 父控件可以存放子控件的内容，父控件的宽度，减去左右两边的 padding 值
            int flowLayoutContentWidth = widthSize - getPaddingLeft() - getPaddingRight();
            // 当前行已使用的宽度
            int currentRowWidth = 0;
            // 当前行的高度，以一行中最大高度的子控件高度为行高
            int currentRowMaxHeight = 0;
            // 总行数
            mTotalRowCount = 1;

            for (int i = 0; i < childCount; i++) {
                View childView = mPullFlowLayoutAdapter.createView(getContext(), i, this);
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
                    // 增加上一行高度
                    flowLayoutReallyHeight += currentRowMaxHeight;
                    // 计算新行已使用宽度
                    currentRowWidth = measuredWidth + childViewLeftMargin + childViewRightMargin;
                    // 新行高度
                    currentRowMaxHeight = measuredHeight + childViewTopMargin + childViewBottomMargin;
                    // 总行数加1
                    mTotalRowCount += 1;
                }

                // 确定当前子控件所在的位置
                ChildViewInfo childViewInfo = new ChildViewInfo(childView);
                childViewInfo.left = currentRowWidth - measuredWidth - childViewRightMargin + getPaddingLeft();
                childViewInfo.top = flowLayoutReallyHeight + childViewTopMargin;
                childViewInfo.right = currentRowWidth - childViewRightMargin + getPaddingLeft();
                childViewInfo.bottom = flowLayoutReallyHeight + childViewTopMargin + measuredHeight;
                childViewInfo.rowNumber = mTotalRowCount;
                mChildViewList.add(childViewInfo);
            }
            // 加上最后一行高度
            flowLayoutReallyHeight += currentRowMaxHeight;
        }
        // 加上底部 padding 值
        flowLayoutReallyHeight += getPaddingBottom();
        mViewReallyHeight = flowLayoutReallyHeight;

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
        if (mChildViewList.isEmpty()) return;

        for (ChildViewInfo childViewInfo : mChildViewList) {
            childViewInfo.onLayout();
        }
    }

    private int mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mViewReallyHeight > mViewContentHeight + getPaddingTop() + getPaddingBottom()) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                    int dy = mLastY - y;
                    if (dy < 0) {
                        if (getScrollY() == 0) {
                            return super.onTouchEvent(event);
                        }
                        if (getScrollY() + dy < 0) {
                            scrollTo(0, 0);
                            return true;
                        }
                    } else {
                        if (getScrollY() == mViewReallyHeight - mViewContentHeight - getPaddingBottom() - getPaddingTop()) {
                            return super.onTouchEvent(event);
                        }

                        if (getScrollY() + dy + mViewContentHeight > mViewReallyHeight - getPaddingBottom() - getPaddingTop()) {
                            scrollTo(0, mViewReallyHeight - mViewContentHeight - getPaddingBottom() - getPaddingTop());
                            return true;
                        }
                    }

                    scrollBy(0, dy);
                    mLastY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int initialVelocity = (int) mVelocityTracker.getYVelocity();
                    if (Math.abs(initialVelocity) > 200) {
                        // 由于坐标轴正方向问题，要加负号。
                        mScroller.fling(0, getScrollY(), 0, -initialVelocity, 0, 0, 0, 10000);
                    }
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
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            // 快速滑动边界判断
            if (currY > mViewReallyHeight - mViewContentHeight - getPaddingBottom() - getPaddingTop()) {
                currY = mViewReallyHeight - mViewContentHeight - getPaddingBottom() - getPaddingTop();
                scrollTo(0, currY);
                mScroller.abortAnimation();

                if (mVelocityTracker != null) {
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            scrollTo(0, currY);
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
        private View childView;
        private int left;
        private int top;
        private int right;
        private int bottom;

        private int rowNumber; // 所在行

        public ChildViewInfo(View childView) {
            this.childView = childView;
        }

        public void onLayout() {
            childView.layout(left, top, right, bottom);
        }
    }
}

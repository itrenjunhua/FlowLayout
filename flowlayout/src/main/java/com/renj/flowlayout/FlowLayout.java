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
 * 描述：流式布局控件  需要继续实现功能：
 * 行内对齐方式；
 * 增加属性控制：最大行数，对齐方式，水平和垂直方向间距；
 * Adapter控件回收机制
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class FlowLayout extends ViewGroup {
    private int mViewContentHeight = 0; // 内容显示高度
    private int mViewReallyHeight = 0;  // 控件实际高度(所有子控件的高度和+paddingTop+paddingBottom)
    private int mMaxScrollY; // 滑动时，最大滑动偏移量
    private Scroller mScroller; // 支持滑动
    private VelocityTracker mVelocityTracker; // ACTION_UP 时测速

    private boolean childViewShowFinish = true; // 子控件是否已经全部显示完成了
    private int mTotalRowCount; // 总行数
    private int mMaxRowCount = Integer.MAX_VALUE; // 显示最大行数
    private List<ChildViewInfo> mChildViewList = new ArrayList<>(); // 所有子控件集合
    private FlowLayoutAdapter mFlowLayoutAdapter;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context);
        mVelocityTracker = VelocityTracker.obtain();
    }

    /**
     * 设置适配器
     *
     * @param flowLayoutAdapter
     */
    public void setAdapter(FlowLayoutAdapter flowLayoutAdapter) {
        if (flowLayoutAdapter != null) {
            this.mFlowLayoutAdapter = flowLayoutAdapter;
            flowLayoutAdapter.setFlowLayout(this);
            requestLayout();
        }
    }

    /**
     * 设置最大显示行数
     *
     * @param maxRowCount 最大显示行数  小于0表示全部显示
     */
    public void setMaxRowCount(int maxRowCount) {
        if ((maxRowCount == mMaxRowCount)
                || (maxRowCount < 0 && mMaxRowCount < 0)
                || (childViewShowFinish && maxRowCount > mTotalRowCount)
                || (childViewShowFinish && maxRowCount < 0)) {
            return;
        }

        if (maxRowCount < 0) {
            maxRowCount = Integer.MAX_VALUE;
        }
        this.mMaxRowCount = maxRowCount;
        // 先滑动到顶部
        if (getScrollY() > 0) {
            scrollTo(0, 0);
        }
        requestLayout();
    }

    /**
     * 获取所有行数，当设置了最大行数时，可能小于(最大行数大于实际全部显示完成行数)等于最大行数
     *
     * @return 所有行数
     */
    public int getTotalRowCount() {
        return mTotalRowCount;
    }

    /**
     * 是否所有的子控件都显示完成了，当设置了最大行数时 {@link #setMaxRowCount(int)}，可能没有显示完全
     *
     * @return
     */
    public boolean isChildViewShowFinish() {
        return childViewShowFinish;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        removeAllViews();
        mChildViewList.clear();
        mTotalRowCount = 0;

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mFlowLayoutAdapter == null || mMaxRowCount == 0) {
            // 确定高度
            if (heightMode == MeasureSpec.EXACTLY) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            } else {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // 内容显示高度
        mViewContentHeight = heightSize - getPaddingTop() - getPaddingBottom();

        // 所有孩子控件都完全显示需要的高度，默认加上顶部的 padding 值
        int flowLayoutReallyHeight = getPaddingTop();
        int childCount = mFlowLayoutAdapter.getItemCount();
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
                View childView = mFlowLayoutAdapter.createView(getContext(), this, i);
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

                int childViewWidth = childView.getMeasuredWidth() + childViewLeftMargin + childViewRightMargin;
                int childViewHeight = childView.getMeasuredHeight() + childViewTopMargin + childViewBottomMargin;
                // 计算当前行已使用的宽度
                currentRowWidth += childViewWidth;
                // 取一行最大高度为行高
                currentRowMaxHeight = Math.max(childViewHeight, currentRowMaxHeight);
                // 换行
                if (currentRowWidth > flowLayoutContentWidth) {
                    // 增加上一行高度
                    flowLayoutReallyHeight += currentRowMaxHeight;
                    // 计算新行已使用宽度
                    currentRowWidth = childViewWidth;
                    // 新行高度
                    currentRowMaxHeight = childViewHeight;
                    // 总行数加1
                    mTotalRowCount += 1;
                    // 显示最大行数控制
                    if (mTotalRowCount > mMaxRowCount) {
                        // 超过最大行数的部分，减掉
                        mTotalRowCount -= 1;
                        currentRowMaxHeight = 0;
                        break;
                    }
                }

                // 确定当前子控件所在的位置
                ChildViewInfo childViewInfo = new ChildViewInfo(childView);
                childViewInfo.left = currentRowWidth - childViewWidth - childViewRightMargin + getPaddingLeft();
                childViewInfo.top = flowLayoutReallyHeight + childViewTopMargin;
                childViewInfo.right = currentRowWidth - childViewRightMargin + getPaddingLeft();
                childViewInfo.bottom = flowLayoutReallyHeight + childViewTopMargin + childViewHeight;
                childViewInfo.rowNumber = mTotalRowCount;
                childViewInfo.position = i;
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

        // 滑动时，最大滑动偏移量
        mMaxScrollY = mViewReallyHeight - mViewContentHeight - getPaddingBottom() - getPaddingTop();
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mFlowLayoutAdapter != null) {
            childViewShowFinish = mFlowLayoutAdapter.getItemCount() == mChildViewList.size();
        } else {
            childViewShowFinish = true;
        }

        if (mChildViewList.isEmpty()) {
            if (onLayoutFinishListener != null)
                onLayoutFinishListener.onLayoutFinish(this, mChildViewList.size());
            return;
        }

        for (final ChildViewInfo childViewInfo : mChildViewList) {
            childViewInfo.onLayout();
            childViewInfo.childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(mFlowLayoutAdapter, childViewInfo.position);
                    }
                }
            });
        }
        if (onLayoutFinishListener != null)
            onLayoutFinishListener.onLayoutFinish(this, mChildViewList.size());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                intercepted = true;
                break;
        }
        return intercepted;
    }

    private boolean isActionDown;
    private int mTouchEventLastY;

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
                    // 不处理按下事件，防止和子控件的点击事件冲突。
                    // 所以将滑动的第一个坐标作为起始坐标
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isActionDown) {
                        // 将滑动的第一个坐标作为起始坐标
                        mTouchEventLastY = y;
                        isActionDown = true;
                    } else {
                        if (!mScroller.isFinished()) {
                            mScroller.abortAnimation();
                        }
                        int dy = mTouchEventLastY - y;
                        // 向上滑动
                        if (dy < 0) {
                            if (getScrollY() == 0) {
                                return super.onTouchEvent(event);
                            }
                            if (getScrollY() + dy < 0) {
                                scrollTo(0, 0);
                                return true;
                            }
                        } else {
                            // 向下滑动
                            if (getScrollY() == mMaxScrollY) {
                                return super.onTouchEvent(event);
                            }
                            if (getScrollY() + dy > mMaxScrollY) {
                                scrollTo(0, mMaxScrollY);
                                return true;
                            }
                        }
                        scrollBy(0, dy);
                        mTouchEventLastY = y;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isActionDown = false;
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int initialVelocity = (int) mVelocityTracker.getYVelocity();
                    if (Math.abs(initialVelocity) > 200) {
                        // 由于坐标轴正方向问题，要加负号。
                        mScroller.fling(0, getScrollY(), 0, -initialVelocity, 0, 0, 0, 10000);
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.clear();
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
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
            if (currY > mMaxScrollY) {
                currY = mMaxScrollY;
                scrollTo(0, currY);
                mScroller.abortAnimation();
            }
            scrollTo(0, currY);
            postInvalidate();
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
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

        private int position;  // 在父控件中的位置
        private int rowNumber; // 所在行

        public ChildViewInfo(View childView) {
            this.childView = childView;
        }

        public void onLayout() {
            childView.layout(left, top, right, bottom);
        }
    }

    private OnItemClickListener onItemClickListener;
    private OnLayoutFinishListener onLayoutFinishListener;

    /**
     * 设置孩子控件布局完成监听
     *
     * @param onLayoutFinishListener
     */
    public void setOnLayoutFinishListener(OnLayoutFinishListener onLayoutFinishListener) {
        this.onLayoutFinishListener = onLayoutFinishListener;
    }

    /**
     * 移除孩子控件布局完成监听
     */
    public void removeOnLayoutFinishListener() {
        this.onLayoutFinishListener = null;
    }

    /**
     * 设置孩子控件点击监听
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 孩子控件点击监听
     */
    public interface OnItemClickListener {
        void onItemClick(FlowLayoutAdapter adapter, int position);
    }

    /**
     * 孩子控件布局完成监听
     */
    public interface OnLayoutFinishListener {
        /**
         * @param flowLayout
         * @param childCount 当前完成布局的孩子数（显示的孩子数）
         */
        void onLayoutFinish(FlowLayout flowLayout, int childCount);
    }
}

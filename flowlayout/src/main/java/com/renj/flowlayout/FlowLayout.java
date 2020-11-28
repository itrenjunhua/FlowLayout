package com.renj.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
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
 * 描述：流式布局控件
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class FlowLayout extends ViewGroup {
    /**
     * 居左对齐，默认
     */
    public static final int HORIZONTAL_GRAVITY_LEFT = 0;
    /**
     * 居右对齐
     */
    public static final int HORIZONTAL_GRAVITY_RIGHT = 1;
    /**
     * 左右对齐
     */
    public static final int HORIZONTAL_GRAVITY_LEFT_RIGHT = 2;
    /**
     * 居中对齐
     */
    public static final int HORIZONTAL_GRAVITY_CENTER = 3;

    private int mViewContentWidth; // 内容显示宽度
    private int mViewContentHeight; // 内容显示高度
    private int mViewOldContentHeight; // 老的高度，当高度发生变化时，用来做高度变化动画
    private int mViewReallyHeight;  // 控件实际高度(所有子控件的高度和+paddingTop+paddingBottom)

    private int mShowChildViewCount; // 显示的子控件数
    private boolean mChildViewAllShow = true; // 子控件是否已经全部显示了
    private int mTotalShowRowCount; // 总显示行数
    private int mMaxRowCount = Integer.MAX_VALUE; // 最大显示行数
    private List<RowChildViewInfo> mRowChildViewList = new ArrayList<>(); // 所有子控件行信息集合

    private int mMaxScrollY; // 滑动时，最大滑动偏移量
    private Scroller mScroller; // 支持滑动
    private VelocityTracker mVelocityTracker; // ACTION_UP 时测速

    // 每一行的水平方向对齐方式
    private int mHorizontalGravity = HORIZONTAL_GRAVITY_LEFT;

    // 适配器对象
    private FlowLayoutAdapter mFlowLayoutAdapter;
    // 子控件点击监听
    private OnItemClickListener mOnItemClickListener;
    // 子控件布局完成监听
    private OnChildLayoutFinishListener mOnChildLayoutFinishListener;

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

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        mMaxRowCount = typedArray.getInteger(R.styleable.FlowLayout_flow_max_row_count, Integer.MAX_VALUE);
        mHorizontalGravity = typedArray.getInteger(R.styleable.FlowLayout_flow_horizontal_gravity, HORIZONTAL_GRAVITY_LEFT);
        typedArray.recycle();
    }

    /**
     * 设置适配器
     *
     * @param flowLayoutAdapter {@link FlowLayoutAdapter} 子类对象
     */
    public void setAdapter(FlowLayoutAdapter flowLayoutAdapter) {
        if (flowLayoutAdapter != null) {
            this.mFlowLayoutAdapter = flowLayoutAdapter;
            flowLayoutAdapter.setFlowLayout(this);
            requestLayout();
        }
    }

    /**
     * 获取适配器，由方法 {@link #setAdapter(FlowLayoutAdapter)} 设置的
     *
     * @return 返回设置的适配器
     */
    public FlowLayoutAdapter getFlowLayoutAdapter() {
        return mFlowLayoutAdapter;
    }

    /**
     * 设置子控件布局完成监听
     *
     * @param onChildLayoutFinishListener {@link OnChildLayoutFinishListener}
     */
    public void setOnChildLayoutFinishListener(OnChildLayoutFinishListener onChildLayoutFinishListener) {
        this.mOnChildLayoutFinishListener = onChildLayoutFinishListener;
    }

    /**
     * 移除子控件布局完成监听
     */
    public void removeOnLayoutFinishListener() {
        this.mOnChildLayoutFinishListener = null;
    }

    /**
     * 设置子控件点击监听
     *
     * @param onItemClickListener {@link OnItemClickListener}
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * 设置最大显示行数
     *
     * @param maxRowCount 最大显示行数  小于0表示全部显示
     */
    public void setMaxRowCount(int maxRowCount) {
        if ((maxRowCount == mMaxRowCount)
                || (maxRowCount < 0 && mMaxRowCount < 0)
                || (mChildViewAllShow && maxRowCount > mTotalShowRowCount)
                || (mChildViewAllShow && maxRowCount < 0)) {
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
     * 设置水平方向控件对齐方式，默认居左对齐（{@link #HORIZONTAL_GRAVITY_LEFT}）
     *
     * @param horizontalGravity {@link #HORIZONTAL_GRAVITY_LEFT}、
     *                          {@link #HORIZONTAL_GRAVITY_RIGHT}、
     *                          {@link #HORIZONTAL_GRAVITY_LEFT_RIGHT}、
     *                          {@link #HORIZONTAL_GRAVITY_CENTER}
     */
    public void setHorizontalGravity(int horizontalGravity) {
        if (this.mHorizontalGravity != horizontalGravity) {
            this.mHorizontalGravity = horizontalGravity;
            requestLayout();
        }
    }

    /**
     * 滚动到顶部
     *
     * @param animation true：使用动画滚动  false：不使用动画
     */
    public void scrollToTop(boolean animation) {
        int scrollY = getScrollY();
        if (scrollY > 0) {
            if (animation) {
                smallScrollToPosition(scrollY, -scrollY);
            } else {
                scrollTo(0, 0);
            }
        }
    }

    /**
     * 滚动到底部
     *
     * @param animation true：使用动画滚动  false：不使用动画
     */
    public void scrollToBottom(boolean animation) {
        int scrollY = getScrollY();
        if (mMaxScrollY > scrollY) {
            if (animation) {
                smallScrollToPosition(scrollY, mMaxScrollY - scrollY);
            } else {
                scrollTo(0, mMaxScrollY);
            }
        }
    }

    /**
     * 滚动到指定位置
     *
     * @param animation position：需要滚动到的位置
     * @param animation true：使用动画滚动  false：不使用动画
     */
    public void scrollToPosition(int position, boolean animation) {
        if (position <= 0) {
            scrollToTop(animation);
        } else if (position >= mMaxScrollY) {
            scrollToBottom(animation);
        } else {
            if (animation) {
                int scrollY = getScrollY();
                smallScrollToPosition(scrollY, position - scrollY);
            } else {
                scrollTo(0, position);
            }
        }
    }

    /**
     * 滚动到指定行数
     *
     * @param animation rowNumber：需要滚动到的行数值
     * @param animation true：使用动画滚动  false：不使用动画
     */
    public void scrollToRowNumber(int rowNumber, boolean animation) {
        if (rowNumber <= 0) {
            scrollToTop(animation);
        } else if (rowNumber >= mTotalShowRowCount) {
            scrollToBottom(animation);
        } else {
            int position = 0;
            // 根据行数计算 position
            for (RowChildViewInfo rowChildViewInfo : mRowChildViewList) {
                position += rowChildViewInfo.rowHeight;
                if (rowChildViewInfo.rowNumber == rowNumber) {
                    break;
                }
            }
            scrollToPosition(position, animation);
        }
    }

    /**
     * 垂直方向平滑滚动方法
     *
     * @param startY 开始位置
     * @param dy     移动距离
     */
    private void smallScrollToPosition(int startY, int dy) {
        mScroller.startScroll(0, startY, 0, dy, Math.min(600, Math.max(300, Math.abs(dy))));
        postInvalidate();
    }

    /**
     * 获取显示的行数。<br/>
     * <b>重点注意：不要直接调用，而要在 {@link #setOnChildLayoutFinishListener(OnChildLayoutFinishListener)}
     * 回调中调用才能保证结果的正确性。</b><br/><br/>
     * 注意：当调用 {@link #setMaxRowCount(int)} 方法设置了最大行数时，<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * 可能小于最大行数：当设置的最大行数 > 全部显示完成所需的行数时；<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * 或者等于最大行数：当设置的最大行数 <= 全部显示完成所需的行数时。
     *
     * @return 显示的行数
     */
    public int getShowRowCount() {
        return mTotalShowRowCount;
    }

    /**
     * 是否所有的子控件都显示了。<br/>
     * <b>重点注意：不要直接调用，而要在 {@link #setOnChildLayoutFinishListener(OnChildLayoutFinishListener)}
     * 回调中调用才能保证结果的正确性。</b><br/><br/>
     * 注意：当调用 {@link #setMaxRowCount(int)} 方法设置了最大行数时，可能并非所有子控件都显示完全了。
     *
     * @return true：所有子控件都显示出来了   false：还有子控件没有显示出来
     */
    public boolean isChildViewAllShow() {
        return mChildViewAllShow;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        removeAllViews();
        mRowChildViewList.clear();
        mShowChildViewCount = 0;
        mTotalShowRowCount = 0;

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
        // 内容显示宽度和高度
        mViewContentWidth = widthSize - getPaddingLeft() - getPaddingRight();
        mViewContentHeight = heightSize - getPaddingTop() - getPaddingBottom();

        // 所有孩子控件都完全显示需要的高度，默认加上顶部的 padding 值
        int flowLayoutReallyHeight = getPaddingTop();
        int childCount = mFlowLayoutAdapter.getItemCount();
        if (childCount > 0) {
            // 当前行已使用的宽度
            int currentRowWidth = 0;
            // 当前行的高度，以一行中最大高度的子控件高度为行高
            int currentRowMaxHeight = 0;
            // 总行数
            mTotalShowRowCount = 1;
            // 显示的子控件数
            mShowChildViewCount = 0;

            List<ChildViewInfo> mChildViewList = new ArrayList<>();
            for (int i = 0; i < childCount; i++) {
                View childView = mFlowLayoutAdapter.createView(getContext(), this, i);
                if (childView.getVisibility() == View.GONE) {
                    continue;
                }
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

                int childViewWidth = childView.getMeasuredWidth();
                int childViewHeight = childView.getMeasuredHeight();
                // 计算当前行已使用的宽度
                currentRowWidth += childViewWidth + childViewLeftMargin + childViewRightMargin;
                // 取一行最大高度为行高
                currentRowMaxHeight = Math.max(childViewHeight + childViewTopMargin + childViewBottomMargin, currentRowMaxHeight);
                // 换行
                if (currentRowWidth > mViewContentWidth) {
                    // 增加上一行高度
                    flowLayoutReallyHeight += currentRowMaxHeight;
                    // 组合成新的行对象信息
                    RowChildViewInfo rowChildViewInfo = new RowChildViewInfo();
                    rowChildViewInfo.rowChildViews = mChildViewList;
                    rowChildViewInfo.rowNumber = mTotalShowRowCount;
                    rowChildViewInfo.rowHeight = currentRowMaxHeight;
                    rowChildViewInfo.currentRowUsedWidth = currentRowWidth - (childViewWidth + childViewLeftMargin + childViewRightMargin);
                    mRowChildViewList.add(rowChildViewInfo);

                    // 计算新行已使用宽度
                    currentRowWidth = childViewWidth + childViewLeftMargin + childViewRightMargin;
                    // 新行高度
                    currentRowMaxHeight = childViewHeight + childViewTopMargin + childViewBottomMargin;
                    // 新行子控件集合
                    mChildViewList = new ArrayList<>();
                    // 总行数加1
                    mTotalShowRowCount += 1;
                    // 显示最大行数控制
                    if (mTotalShowRowCount > mMaxRowCount) {
                        // 超过最大行数的部分，减掉
                        mTotalShowRowCount -= 1;
                        currentRowMaxHeight = 0;
                        break;
                    }
                }

                // 确定当前子控件所在的位置
                ChildViewInfo childViewInfo = new ChildViewInfo(childView, mTotalShowRowCount, i);
                childViewInfo.right = currentRowWidth - childViewRightMargin + getPaddingLeft();
                childViewInfo.left = childViewInfo.right - childViewWidth;
                childViewInfo.top = flowLayoutReallyHeight + childViewTopMargin;
                childViewInfo.bottom = childViewInfo.top + childViewHeight;
                mChildViewList.add(childViewInfo);
                mShowChildViewCount += 1;
            }
            // 加上最后一行高度
            flowLayoutReallyHeight += currentRowMaxHeight;
            // 加上最后一行的行对象信息
            RowChildViewInfo rowChildViewInfo = new RowChildViewInfo();
            rowChildViewInfo.rowChildViews = mChildViewList;
            rowChildViewInfo.rowNumber = mTotalShowRowCount;
            rowChildViewInfo.rowHeight = currentRowMaxHeight;
            rowChildViewInfo.currentRowUsedWidth = currentRowWidth;
            mRowChildViewList.add(rowChildViewInfo);
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
        // 确定子控件是否已经全部显示了
        mChildViewAllShow = mFlowLayoutAdapter == null ? true : mFlowLayoutAdapter.getItemCount() == mShowChildViewCount;
        if (mRowChildViewList.isEmpty()) {
            if (mOnChildLayoutFinishListener != null)
                mOnChildLayoutFinishListener.onLayoutFinish(this, mShowChildViewCount);
            return;
        }

        // 水平方向不同对齐方式偏移量，默认居左对齐，不偏移
        int offsetX = 0;
        for (final RowChildViewInfo rowChildViewInfo : mRowChildViewList) {
            List<ChildViewInfo> rowChildViews = rowChildViewInfo.rowChildViews;
            if (mHorizontalGravity == HORIZONTAL_GRAVITY_RIGHT) {
                // 居右对齐
                offsetX = mViewContentWidth - rowChildViewInfo.currentRowUsedWidth;
            } else if (mHorizontalGravity == HORIZONTAL_GRAVITY_LEFT_RIGHT) {
                // 左右两端对齐
                if (rowChildViews.size() > 1) {
                    offsetX = (mViewContentWidth - rowChildViewInfo.currentRowUsedWidth) / (rowChildViews.size() - 1);
                } else {
                    offsetX = 0;
                }
            } else if (mHorizontalGravity == HORIZONTAL_GRAVITY_CENTER) {
                // 居中对齐
                offsetX = (mViewContentWidth - rowChildViewInfo.currentRowUsedWidth) / 2;
            }

            for (int i = 0; i < rowChildViews.size(); i++) {
                final ChildViewInfo childViewInfo = rowChildViews.get(i);
                if (mHorizontalGravity == HORIZONTAL_GRAVITY_LEFT_RIGHT) {
                    // 左右两端对齐，需要特殊处理
                    childViewInfo.onLayout(offsetX * i);
                } else {
                    childViewInfo.onLayout(offsetX);
                }
                childViewInfo.addClickListener(mOnItemClickListener, this, mFlowLayoutAdapter);
            }
        }
        if (mOnChildLayoutFinishListener != null)
            mOnChildLayoutFinishListener.onLayoutFinish(this, mShowChildViewCount);
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
     * 每一行信息
     */
    private static class RowChildViewInfo {
        private int currentRowUsedWidth; // 行使用宽度
        private int rowNumber; // 行号
        private int rowHeight; // 行高
        private List<ChildViewInfo> rowChildViews; // 行内子控件列表
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

        private int rowNumber; // 所在行位置
        private int position;  // 在父控件中的位置

        private ChildViewInfo(View childView, int rowNumber, int position) {
            this.childView = childView;
            this.rowNumber = rowNumber;
            this.position = position;
        }

        private void onLayout(int offsetX) {
            childView.layout(left + offsetX, top, right + offsetX, bottom);
        }

        private void addClickListener(final OnItemClickListener onItemClickListener,
                                      final FlowLayout flowLayout,
                                      final FlowLayoutAdapter flowLayoutAdapter) {
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(flowLayout, flowLayoutAdapter,
                                rowNumber, position);
                    }
                }
            });
        }
    }

    /**
     * 子控件点击监听
     */
    public interface OnItemClickListener {
        /**
         * 点击子控件回调方法
         *
         * @param flowLayout {@link FlowLayout} 控件对象
         * @param adapter    {@link FlowLayoutAdapter} 对象
         * @param rowNumber  所在行，行号从 1 开始
         * @param position   在父控件的位置，位置从 0 开始
         */
        void onItemClick(FlowLayout flowLayout, FlowLayoutAdapter adapter, int rowNumber, int position);
    }

    /**
     * 孩子控件布局完成监听
     */
    public interface OnChildLayoutFinishListener {
        /**
         * @param flowLayout {@link FlowLayout} 控件对象
         * @param childCount 当前完成布局的孩子数（显示的孩子数）
         */
        void onLayoutFinish(FlowLayout flowLayout, int childCount);
    }
}

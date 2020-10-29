package com.renj.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

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

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

//        if (widthMode == MeasureSpec.EXACTLY) {
//            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
//        } else {
//            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
//        }

        if (heightMode == MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        } else {
            int childCount = getChildCount();
            int currentRowWidth = 0;
            int rowMaxHeight = 0;
            int reallyHeight = 0;

            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                int measuredWidth = childView.getMeasuredWidth();
                int measuredHeight = childView.getMeasuredHeight();
                currentRowWidth += measuredWidth;
                // 取一行最大高度为行高
                rowMaxHeight = Math.max(measuredHeight, rowMaxHeight);
                // 换行
                if (currentRowWidth > widthSize) {
                    currentRowWidth = measuredWidth;
                    reallyHeight += rowMaxHeight;
                    rowMaxHeight = 0;
                }
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(reallyHeight, MeasureSpec.EXACTLY);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}

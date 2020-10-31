package com.renj.flowlayout;

import android.content.Context;
import android.view.View;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2020-10-29   14:29
 * <p>
 * 描述：流式布局控件适配器
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public abstract class FlowLayoutAdapter {
    private FlowLayout flowLayout;

    protected abstract View createView(Context context, FlowLayout flowLayout, int position);

    public abstract int getItemCount();

    public abstract Object getItem(int position);

    protected void setFlowLayout(FlowLayout flowLayout) {
        this.flowLayout = flowLayout;
    }

    public void notifyChange() {
        flowLayout.requestLayout();
    }

}

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
 * 描述：
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public abstract class PullFlowLayoutAdapter {
    private PullFlowLayout pullFlowLayout;

    protected abstract View createView(Context context, int position, PullFlowLayout pullFlowLayout);

    public abstract int getViewCount();

    protected void setPullFlowLayout(PullFlowLayout pullFlowLayout) {
        this.pullFlowLayout = pullFlowLayout;
    }

    public void notifyChange() {
        pullFlowLayout.requestLayout();
    }
}

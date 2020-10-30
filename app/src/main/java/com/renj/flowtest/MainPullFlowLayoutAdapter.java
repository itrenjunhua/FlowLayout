package com.renj.flowtest;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.renj.flowlayout.PullFlowLayout;
import com.renj.flowlayout.PullFlowLayoutAdapter;

import java.util.List;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2020-10-29   15:45
 * <p>
 * 描述：
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class MainPullFlowLayoutAdapter extends PullFlowLayoutAdapter {
    private List<String> dataList;

    public MainPullFlowLayoutAdapter(List<String> datas) {
        this.dataList = datas;
    }

    public void addData(List<String> datas) {
        if (datas != null && dataList != null) {
            this.dataList.addAll(datas);
            notifyChange();
        }
    }

    @Override
    protected View createView(Context context, int position, PullFlowLayout pullFlowLayout) {
        TextView textView = new TextView(context);
        textView.setText(dataList.get(position));
        textView.setTextSize(16);
        if (position % 4 == 0) {
            textView.setTextColor(context.getResources().getColor(R.color.color_white));
            textView.setBackgroundResource(R.drawable.shape_text_bg2);
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.color_text_grey));
            textView.setBackgroundResource(R.drawable.shape_text_bg);
        }
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 12;
        params.leftMargin = 6;
        params.rightMargin = 6;
        textView.setLayoutParams(params);
        textView.setPadding(16, 6, 16, 6);
        return textView;
    }

    @Override
    public int getViewCount() {
        return dataList == null ? 0 : dataList.size();
    }
}

package com.renj.flowtest;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.renj.flowlayout.FlowLayout;
import com.renj.flowlayout.FlowLayoutAdapter;
import com.renj.flowtest.utils.ListUtils;

import java.util.ArrayList;
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
public class MainFlowLayoutAdapter extends FlowLayoutAdapter {
    private List<String> dataList;
    private int mCheckedPosition = -1;

    public MainFlowLayoutAdapter(List<String> datas) {
        this.dataList = datas;
    }

    public void setCheckedPosition(int checkedPosition) {
        if (this.mCheckedPosition == checkedPosition) {
            this.mCheckedPosition = -1;
        } else {
            this.mCheckedPosition = checkedPosition;
        }
        notifyChange();
    }

    public void setNewData(List<String> datas) {
        if (datas != null) {
            if (this.dataList == null) {
                this.dataList = new ArrayList<>();
            } else {
                this.dataList.clear();
            }
            this.dataList.addAll(datas);
            notifyChange();
        }
    }

    public void addData(List<String> datas) {
        if (datas != null && dataList != null) {
            this.dataList.addAll(datas);
            notifyChange();
        }
    }

    @Override
    protected View createView(Context context, FlowLayout flowLayout, int position) {
        TextView textView = new TextView(context);
        textView.setText(dataList.get(position));
        textView.setTextSize(16);
        if (position == mCheckedPosition) {
            textView.setTextColor(context.getResources().getColor(R.color.color_white));
            textView.setBackgroundResource(R.drawable.shape_text_bg2);
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.color_text_grey));
            textView.setBackgroundResource(R.drawable.shape_text_bg);
        }
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dip2px(context, 6);
        params.leftMargin = dip2px(context, 3);
        params.rightMargin = dip2px(context, 3);
        textView.setLayoutParams(params);
        textView.setPadding(16, 6, 16, 6);
        return textView;
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        if (ListUtils.isEmpty(dataList)) return null;
        return dataList.get(position);
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

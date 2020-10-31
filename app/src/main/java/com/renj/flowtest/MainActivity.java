package com.renj.flowtest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.renj.flowlayout.FlowLayout;
import com.renj.flowtest.utils.DataUtils;
import com.renj.flowtest.utils.Logger;
import com.renj.flowtest.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {
    private FlowLayout flowLayout;
    private Button btAddData;
    private EditText etInputRows;
    private Button btApplyRows;

    private MainFlowLayoutAdapter flowLayoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flowLayout = findViewById(R.id.flow_layout);
        btAddData = findViewById(R.id.bt_add_data);
        etInputRows = findViewById(R.id.et_input_rows);
        btApplyRows = findViewById(R.id.bt_apply_rows);

        flowLayoutAdapter = new MainFlowLayoutAdapter(DataUtils.getDataList(30));
        flowLayout.setAdapter(flowLayoutAdapter);

        setFlowLayoutFinishListener();

        // 设置点击监听
        flowLayout.setOnItemClickListener((adapter, position) -> {
            // 移除孩子控件布局完成监听
            flowLayout.removeOnLayoutFinishListener();
            flowLayoutAdapter.setCheckedPosition(position);
            Object item = flowLayoutAdapter.getItem(position);
            ToastUtils.showToast(item + "");
        });

        // 增加数据
        btAddData.setOnClickListener(v -> {
            setFlowLayoutFinishListener();
            flowLayoutAdapter.addData(DataUtils.getDataList(10));
        });

        // 设置行数
        btApplyRows.setOnClickListener(v -> {
            setFlowLayoutFinishListener();
            flowLayout.setMaxRowCount(getNumberFormText(etInputRows));
        });
    }

    // 设置孩子控件布局完成监听
    private void setFlowLayoutFinishListener() {
        flowLayout.setOnLayoutFinishListener((flowLayout, childCount) -> {
            Logger.i("总行数: " + flowLayout.getTotalRowCount() + " ；子控件总数："
                    + flowLayoutAdapter.getItemCount() + " ；显示子控件数： " + childCount
                    + " ；全部子控件是否显示完成： " + flowLayout.isChildViewShowFinish());
            ToastUtils.showToast("总行数: " + flowLayout.getTotalRowCount() + " 子控件显示完成： " + flowLayout.isChildViewShowFinish());
        });
    }

    private int getNumberFormText(TextView textView) {
        String trim = textView.getText().toString().trim();
        try {
            return Integer.parseInt(trim);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
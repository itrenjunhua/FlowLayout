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

    private MainFlowLayoutAdapter pullFlowLayoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flowLayout = findViewById(R.id.flow_layout);
        btAddData = findViewById(R.id.bt_add_data);
        etInputRows = findViewById(R.id.et_input_rows);
        btApplyRows = findViewById(R.id.bt_apply_rows);

        pullFlowLayoutAdapter = new MainFlowLayoutAdapter(DataUtils.getDataList(30));
        flowLayout.setAdapter(pullFlowLayoutAdapter);

        // 设置点击监听
        flowLayout.setOnItemClickListener((adapter, position) -> {
            pullFlowLayoutAdapter.setCheckedPosition(position);
            Object item = pullFlowLayoutAdapter.getItem(position);
            ToastUtils.showToast(item + "");
        });

        // 增加数据
        btAddData.setOnClickListener(v -> {
            pullFlowLayoutAdapter.addData(DataUtils.getDataList(10));
            Logger.i("总行数: " + flowLayout.getTotalRowCount());
        });

        // 设置行数
        btApplyRows.setOnClickListener(v -> {
            flowLayout.setMaxRowCount(getNumberFormText(etInputRows));
            Logger.i("总行数: " + flowLayout.getTotalRowCount());
            ToastUtils.showToast(flowLayout.isShowFinish() + "");
        });
    }

    private int getNumberFormText(TextView textView) {
        String trim = textView.getText().toString().trim();
        try {
            return Integer.parseInt(trim);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
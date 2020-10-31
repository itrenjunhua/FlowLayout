package com.renj.flowtest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.renj.flowlayout.FlowLayout;
import com.renj.flowtest.utils.DataUtils;
import com.renj.flowtest.utils.Logger;
import com.renj.flowtest.utils.StringUtils;
import com.renj.flowtest.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {
    private FlowLayout flowLayout;
    private Button btDefault;
    private Button btAddData;
    private Button btGravityLeft;
    private Button btGravityRight;
    private Button btGravityLeftRight;
    private Button btGravityCenter;

    private EditText etInputRows;
    private Button btApplyRows;

    private MainFlowLayoutAdapter flowLayoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flowLayout = findViewById(R.id.flow_layout);
        btDefault = findViewById(R.id.bt_default);
        btAddData = findViewById(R.id.bt_add_data);
        btGravityLeft = findViewById(R.id.bt_gravity_left);
        btGravityRight = findViewById(R.id.bt_gravity_right);
        btGravityLeftRight = findViewById(R.id.bt_gravity_leftRight);
        btGravityCenter = findViewById(R.id.bt_gravity_center);
        etInputRows = findViewById(R.id.et_input_rows);
        btApplyRows = findViewById(R.id.bt_apply_rows);

        flowLayoutAdapter = new MainFlowLayoutAdapter(DataUtils.getDataList(30));
        flowLayout.setAdapter(flowLayoutAdapter);

        setFlowLayoutFinishListener();

        // 设置点击监听
        flowLayout.setOnItemClickListener((flowLayout, adapter, rowNumber, position) -> {
            // 移除孩子控件布局完成监听
            flowLayout.removeOnLayoutFinishListener();

            flowLayoutAdapter.setCheckedPosition(position);
            Object item = flowLayoutAdapter.getItem(position);
            ToastUtils.showToast(item + "");
            Logger.i("点击子控件： 所在行： " + rowNumber + " ；位置： " + position + " ；内容： " + item);
        });

        // 默认
        btDefault.setOnClickListener(v -> {
            setFlowLayoutFinishListener();
            flowLayout.scrollToTop(false);
            flowLayout.setHorizontalGravity(FlowLayout.HORIZONTAL_GRAVITY_LEFT);
            flowLayout.setMaxRowCount(-1);
            etInputRows.setText("");
            flowLayoutAdapter.setCheckedPosition(-1);
            flowLayoutAdapter.setNewData(DataUtils.getDataList(30));
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

        // 左对齐，默认
        btGravityLeft.setOnClickListener(v -> flowLayout.setHorizontalGravity(FlowLayout.HORIZONTAL_GRAVITY_LEFT));
        // 右对齐
        btGravityRight.setOnClickListener(v -> flowLayout.setHorizontalGravity(FlowLayout.HORIZONTAL_GRAVITY_RIGHT));
        // 两端对齐
        btGravityLeftRight.setOnClickListener(v -> flowLayout.setHorizontalGravity(FlowLayout.HORIZONTAL_GRAVITY_LEFT_RIGHT));
        // 居中对齐
        btGravityCenter.setOnClickListener(v -> flowLayout.setHorizontalGravity(FlowLayout.HORIZONTAL_GRAVITY_CENTER));
    }

    // 设置子控件布局完成监听
    private void setFlowLayoutFinishListener() {
        flowLayout.setOnChildLayoutFinishListener((flowLayout, childCount) -> {
            Logger.i("子控件布局完成： 显示行数: " + flowLayout.getShowRowCount() + " ；子控件总数："
                    + flowLayoutAdapter.getItemCount() + " ；显示子控件数： " + childCount
                    + " ；全部子控件是否显示完成： " + flowLayout.isChildViewAllShow());
        });
    }

    private int getNumberFormText(TextView textView) {
        String trim = textView.getText().toString().trim();
        if (StringUtils.isEmpty(trim)) return -1;

        try {
            return Integer.parseInt(trim);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
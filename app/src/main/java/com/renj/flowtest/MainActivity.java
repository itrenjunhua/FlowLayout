package com.renj.flowtest;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.renj.flowlayout.PullFlowLayout;

public class MainActivity extends AppCompatActivity {
    private PullFlowLayout flowLayout;
    private Button btAddData;


    private MainPullFlowLayoutAdapter pullFlowLayoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flowLayout = findViewById(R.id.flow_layout);
        btAddData = findViewById(R.id.bt_add_data);

        pullFlowLayoutAdapter = new MainPullFlowLayoutAdapter(DataUtils.getDataList(30));
        flowLayout.setAdapter(pullFlowLayoutAdapter);

        btAddData.setOnClickListener(v -> {
            pullFlowLayoutAdapter.addData(DataUtils.getDataList(10));
        });
    }
}
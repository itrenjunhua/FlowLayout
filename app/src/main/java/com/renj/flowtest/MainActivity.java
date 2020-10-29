package com.renj.flowtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.renj.flowlayout.PullFlowLayout;
import com.renj.flowlayout.PullFlowLayoutAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullFlowLayout flowLayout;

    private List<String> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flowLayout = findViewById(R.id.flow_layout);

        datas = DataUtils.getDataList(30);
        flowLayout.setAdapter(new PullFlowLayoutAdapter() {
            @Override
            protected View createView(Context context, int position, PullFlowLayout pullFlowLayout) {
                TextView textView = new TextView(context);
                textView.setText(datas.get(position));
                textView.setTextSize(16);
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundResource(R.drawable.shape_text_bg);
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = 8;
                params.rightMargin = 12;
                textView.setLayoutParams(params);
                textView.setPadding(16, 6, 16, 6);
                return textView;
            }

            @Override
            public int getViewCount() {
                return datas.size();
            }
        });
    }
}
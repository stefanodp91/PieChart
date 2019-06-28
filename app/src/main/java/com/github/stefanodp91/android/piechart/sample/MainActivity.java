package com.github.stefanodp91.android.piechart.sample;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.github.stefanodp91.android.piechart.OnArcClickListener;
import com.github.stefanodp91.android.piechart.PieChart;

public class MainActivity extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        final PieChart pieChart = findViewById(R.id.pie_chart_view);
        pieChart.setOnArcCLickListener(new OnArcClickListener() {
            @Override
            public void onArcClicked(PieChart.Arc arc) {
                Log.d("arcId", arc.getId());
                ((AppCompatTextView) findViewById(R.id.value)).setText(arc.getId());
            }
        });

        pieChart.addArc("red", 270, 45, getResources().getIntArray(R.array.dummy_red_arc));
        pieChart.addArc("green", 315, 45, getResources().getIntArray(R.array.dummy_green_arc));
        pieChart.addArc("blue", 360, 90, getResources().getIntArray(R.array.dummy_blue_arc));
        pieChart.addArc("yellow", 90, 180, getResources().getIntArray(R.array.dummy_yellow_arc));
    }
}

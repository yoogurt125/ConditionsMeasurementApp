package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;

public class HistoryChartActivity extends AppCompatActivity {

    int[] temperature;
    int[] humidity;
    LineChart mLinechartTemp;
    LineChart mLinechartHumid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_chart_viewer);

        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String data = intent.getStringExtra("data");
        Log.d("dupa", date);
        Log.d("dupa", data);
        data = data.replace(",", "")
                .replace("[", "")
                .replace("]", "")
                .replace(" ","")
                .trim();
        Log.d("dupa", data);


        mLinechartTemp = findViewById(R.id.history_linechart_temp);
        mLinechartHumid = findViewById(R.id.history_linechart_humid);
        mLinechartTemp.setDragEnabled(true);
        mLinechartTemp.setScaleEnabled(true);
        mLinechartHumid.setDragEnabled(true);
        mLinechartHumid.setScaleEnabled(true);
        ArrayList<Entry> temperatureValues = new ArrayList<>();
        ArrayList<Entry> humidityValues = new ArrayList<>();

        temperature = new int[data.length() / 4];
        humidity = new int[data.length() / 4];
        for (int i = 0, j = 0; i < data.length() / 2 && j < data.length() / 4; i += 2, j++) {
            String subTemperature = data.substring(i, i + 2);
            temperature[j] = Integer.parseInt(subTemperature);
        }
        for (int i = data.length() / 2, j = 0; i < data.length() && j < data.length() / 4; i += 2, j++) {
            String subHumidity = data.substring(i, i + 2);
            humidity[j] = Integer.parseInt(subHumidity);
        }
        for (int i = 0; i < temperature.length; i++) {
            temperatureValues.add(new Entry(i, temperature[i]));
        }
        for (int i = 0; i < humidity.length; i++) {
            humidityValues.add(new Entry(i, humidity[i]));
        }

        LineDataSet temperatureSet = new LineDataSet(temperatureValues, "Temperature °C");
        temperatureSet.setLineWidth(3f);
        temperatureSet.setLineWidth(3f);
        temperatureSet.setColor(RED);
        temperatureSet.setValueTextSize(15f);
        ArrayList<ILineDataSet> temperatureDataSet = new ArrayList<>();
        temperatureDataSet.add(temperatureSet);
        LineData temperatureData = new LineData(temperatureDataSet);
        mLinechartTemp.zoom(3f, 1f, 1, 1);
        mLinechartTemp.setData(temperatureData);
        mLinechartTemp.highlightValue(null);
        mLinechartTemp.invalidate();
        mLinechartTemp.animateY(1500, Easing.EaseOutBack);
        YAxis leftAxisTemp = mLinechartTemp.getAxisLeft();
        leftAxisTemp.removeAllLimitLines();

        LineDataSet humiditySet = new LineDataSet(humidityValues, "Humidity %");
        humiditySet.setLineWidth(3f);
        humiditySet.setColor(Color.BLUE);
        humiditySet.setValueTextSize(15f);
        ArrayList<ILineDataSet> humidityDataSet = new ArrayList<>();
        humidityDataSet.add(humiditySet);
        LineData humidityData = new LineData(humidityDataSet);
        mLinechartHumid.zoom(3f, 1f, 1, 1);
        mLinechartHumid.setData(humidityData);
        mLinechartHumid.highlightValue(null);
        mLinechartHumid.invalidate();
        mLinechartHumid.animateY(1500, Easing.EaseOutBack);
        YAxis leftAxisHumid = mLinechartHumid.getAxisLeft();
        leftAxisHumid.removeAllLimitLines();
//        LimitLine maxHumidLimitLine = createLimitLine(maxHumidThreshold, 2f, BLACK, "Max allowed humiditiy");
//        LimitLine minHumidLimitLine = createLimitLine(minHumidThreshold, 2f, BLACK, "Min allowed humiditiy");
//        leftAxis.addLimitLine(maxHumidLimitLine);
//        leftAxis.addLimitLine(minHumidLimitLine);
//        LimitLine maxTempLimitLine = createLimitLine(maxTempThreshold, 2f, BLACK, "Max allowed temeperature");
//        LimitLine minTempLimitLine = createLimitLine(minTempThreshold, 2f, BLACK, "Min allowed temeperature");
//        leftAxis.addLimitLine(maxTempLimitLine);
//        leftAxis.addLimitLine(minTempLimitLine);
    }
}

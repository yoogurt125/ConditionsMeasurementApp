package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class StatisticsActivity extends AppCompatActivity {

    Bundle bundle;
    TextView avgTempTextView;
    TextView avgHumidTextView;
    TextView highestTempTextView;
    TextView highestHumidTextView;
    TextView lowestTempTextView;
    TextView lowestHumidTextView;
    TextView aboveHumidTextView;
    TextView belowHumidTextView;
    TextView aboveTempTextView;
    TextView belowTempTextView;
    TextView amplitudeTempTextView;
    TextView amplitudeHumidTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        bundle = getIntent().getExtras();
        avgTempTextView = findViewById(R.id.average_temp);
        avgHumidTextView = findViewById(R.id.average_humid);
        highestTempTextView = findViewById(R.id.highest_temp);
        highestHumidTextView = findViewById(R.id.highest_humid);
        lowestTempTextView = findViewById(R.id.lowest_temp);
        lowestHumidTextView = findViewById(R.id.lowest_humid);
        aboveHumidTextView = findViewById(R.id.above_limit_humid);
        belowHumidTextView = findViewById(R.id.below_limit_humid);
        aboveTempTextView = findViewById(R.id.above_limit_temp);
        belowTempTextView = findViewById(R.id.below_limit_temp);
        amplitudeTempTextView = findViewById(R.id.amplitude_temp);
        amplitudeHumidTextView = findViewById(R.id.amplitude_humid);

        calculateStatistics();

    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateStatistics();
    }

     void calculateStatistics(){
        int[] temperature;
        int[] humidity;
        float avgTemp = 0;
        float avgHumid = 0;
        int highestTemp,highestHumid,lowestTemp,lowestHumid,aboveHumid=0,belowHumid=0,
                aboveTemp=0,belowTemp=0;
        float maxTempThreshold, minTempThreshold, maxHumidThreshold, minHumidThreshold;
        temperature = bundle.getIntArray("TEMPERATURE");
        humidity = bundle.getIntArray("HUMIDITY");
        maxTempThreshold = bundle.getFloat("MAXTEMP");
        minTempThreshold = bundle.getFloat("MINTEMP");
        maxHumidThreshold = bundle.getFloat("MAXHUMID");
        minHumidThreshold = bundle.getFloat("MINHUMID");
        for(int i = 0; i<temperature.length; i++){
            avgTemp += (float) temperature[i];
        }
        avgTemp = avgTemp / temperature.length;
        avgTempTextView.setText("Average temperature: " + String.valueOf(avgTemp));

        for(int i = 0; i<humidity.length; i++){
            avgHumid += (float) humidity[i];
        }
        avgHumid = avgHumid/ humidity.length;
        avgHumidTextView.setText("Average humidity: " + String.valueOf(avgHumid));

         highestTemp = temperature[0];
         for(int i = 1; i<temperature.length; i++){
             if(highestTemp<temperature[i])
                 highestTemp = temperature[i];
         }
         highestTempTextView.setText("Highest temperature: " + String.valueOf(highestTemp));

         lowestTemp = temperature[0];
         for(int i = 1; i<temperature.length; i++){
             if(lowestTemp>temperature[i])
                 lowestTemp = temperature[i];
         }
         lowestTempTextView.setText("Lowest temperature: " + String.valueOf(lowestTemp));

         highestHumid = humidity[0];
         for(int i = 1; i<humidity.length; i++){
             if(highestHumid<humidity[i])
                 highestHumid = humidity[i];
         }
         highestHumidTextView.setText("Highest humidity: " + String.valueOf(highestHumid));

         lowestHumid = humidity[0];
         for(int i = 1; i<humidity.length; i++){
             if(lowestHumid>humidity[i])
                 lowestHumid = humidity[i];
         }
         lowestHumidTextView.setText("Lowest humidity: " + String.valueOf(lowestHumid));

         for(int i = 0; i<temperature.length; i++){
             if(temperature[i]>maxTempThreshold)
                 aboveTemp++;
         }
         aboveTempTextView.setText("# above max temperature limit: " + String.valueOf(aboveTemp));

         for(int i = 0; i<temperature.length; i++){
             if(temperature[i]<minTempThreshold)
                 belowTemp++;
         }
         belowTempTextView.setText("# below min temperature limit: " + String.valueOf(belowTemp));

         for(int i = 0; i<humidity.length; i++){
             if(humidity[i]>maxHumidThreshold)
                 aboveHumid++;
         }
         aboveHumidTextView.setText("# above max humidity limit: " + String.valueOf(aboveHumid));

         for(int i = 0; i<humidity.length; i++){
             if(humidity[i]<minHumidThreshold)
                 belowHumid++;
         }
         belowHumidTextView.setText("# below min temperature limit: " + String.valueOf(belowHumid));
         amplitudeTempTextView.setText("Amplitude: " + String.valueOf(highestTemp-lowestTemp));
         amplitudeHumidTextView.setText("Amplitude: " + String.valueOf(highestHumid-lowestHumid));



    }
}
package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;


public class MainActivity extends AppCompatActivity {

    DatabaseHelper mDatabaseHelper;


    TextView myLabel;
    LineChart mLinechart;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    int numBytes;
    String dataAggregated;
    int[] temperature;
    int[] humidity;
    String[] xAxisHours;
    float maxTempThreshold, minTempThreshold, maxHumidThreshold, minHumidThreshold;
    boolean isTemperature;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.app_bar));
        mDatabaseHelper = new DatabaseHelper(this);

        Button openButton = (Button) findViewById(R.id.open);
        Button temperatureButton = (Button) findViewById(R.id.temperature_button);
        Button humidityButton = (Button) findViewById(R.id.humidity_button);
        Button sendButton = (Button) findViewById(R.id.send);
        Button saveButton = findViewById(R.id.save);
        myLabel = (TextView) findViewById(R.id.label);


        mLinechart = findViewById(R.id.linechart);
        mLinechart.setDragEnabled(true);
        mLinechart.setScaleEnabled(true);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = Arrays.toString(temperature) + Arrays.toString(humidity);

                String date = java.text.DateFormat.getDateTimeInstance().format(new Date());
                Log.d("dupa", date);
                saveChartData(data, date);
            }
        });
        //Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myLabel.setText("Opening Bluetooth connection. Please wait.");
                try {
                    findBT();
                    openBT();
                    sendButton.setEnabled(true);
                } catch (IOException ex) {
                }
            }
        });

        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();
                    temperatureButton.setEnabled(true);
                    humidityButton.setEnabled(true);
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    setXAxisOrder(hour);
                    Log.d("dupa", Arrays.toString(xAxisHours));
                } catch (IOException ex) {
                }
            }
        });

        temperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseTemperatureData(dataAggregated);
                isTemperature = true;
            }
        });
        humidityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseHumidityData(dataAggregated);
                isTemperature = false;
            }
        });
    }


    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
    }

    void beginListenForData() {
        final Handler handler = new Handler();

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            numBytes = mmInputStream.read(packetBytes);
                            StringBuilder sb = new StringBuilder(numBytes * 2);
                            for (byte b : packetBytes)
                                sb.append(String.format("%02x", b));
                            String data = sb.toString();

                            handler.post(new Runnable() {
                                public void run() {


                                    dataAggregated += data;
                                    myLabel.setText("Data Received.");
                                    Log.d("data_temp", dataAggregated);

                                }
                            });
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException {
        dataAggregated = "";
        String msg = "1";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Receiving data");
    }

    void parseHumidityData(String s) {
        if (myLabel.getText() == "Data Received.") {


            ArrayList<Entry> humidityValues = new ArrayList<>();

            humidity = new int[24];
            for (int i = 48, j = 0; i < 96 && j < 24; i += 2, j++) {
                String subHumidity = s.substring(i, i + 2);
                humidity[j] = Integer.parseInt(subHumidity, 16);
            }

            for (int i = 0; i < humidity.length; i++) {
                humidityValues.add(new Entry(i, humidity[i]));
            }


            LineDataSet humiditySet = new LineDataSet(humidityValues, "Humidity %");
            humiditySet.setLineWidth(3f);
            humiditySet.setColor(Color.BLUE);
            humiditySet.setValueTextSize(15f);
            ArrayList<ILineDataSet> humidityDataSet = new ArrayList<>();
            humidityDataSet.add(humiditySet);
            LineData humidityData = new LineData(humidityDataSet);
            mLinechart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisHours));
            mLinechart.getXAxis().setGranularity(1f);
            mLinechart.zoom(3f, 1f, 1, 1);
            mLinechart.setData(humidityData);
            mLinechart.highlightValue(null);
            mLinechart.invalidate();
            mLinechart.animateY(3000, Easing.EaseOutBack);
            YAxis leftAxis = mLinechart.getAxisLeft();
            leftAxis.removeAllLimitLines();
            LimitLine maxHumidLimitLine = createLimitLine(maxHumidThreshold, 2f, BLACK, "Max allowed humiditiy");
            LimitLine minHumidLimitLine = createLimitLine(minHumidThreshold, 2f, BLACK, "Min allowed humiditiy");
            leftAxis.addLimitLine(maxHumidLimitLine);
            leftAxis.addLimitLine(minHumidLimitLine);
        } else {
            Toast.makeText(this, "Device is still receiving data", Toast.LENGTH_SHORT).show();
        }
    }

    void parseTemperatureData(String s) {
        if (myLabel.getText() == "Data Received.") {
            mLinechart = findViewById(R.id.linechart);
            mLinechart.setDragEnabled(true);
            mLinechart.setScaleEnabled(true);
            ArrayList<Entry> temperatureValues = new ArrayList<>();

            temperature = new int[24];
            for (int i = 0, j = 0; i < 48 && j < 24; i += 2, j++) {
                String subTemperature = s.substring(i, i + 2);
                temperature[j] = Integer.parseInt(subTemperature, 16);
            }
            for (int i = 0; i < temperature.length; i++) {
                temperatureValues.add(new Entry(i, temperature[i]));
            }


            LineDataSet temperatureSet = new LineDataSet(temperatureValues, "Temperature °C");
            temperatureSet.setLineWidth(3f);
            temperatureSet.setColor(RED);
            temperatureSet.setValueTextSize(15f);
            ArrayList<ILineDataSet> temperatureDataSet = new ArrayList<>();
            temperatureDataSet.add(temperatureSet);
            LineData temperatureData = new LineData(temperatureDataSet);

            mLinechart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisHours));
            mLinechart.getXAxis().setGranularity(1f);
            mLinechart.zoom(3f, 1f, 1, 1);
            mLinechart.setData(temperatureData);
            mLinechart.highlightValue(null);
            mLinechart.invalidate();
            mLinechart.animateY(3000, Easing.EaseOutBack);
            YAxis leftAxis = mLinechart.getAxisLeft();
            leftAxis.removeAllLimitLines();
            LimitLine maxTempLimitLine = createLimitLine(maxTempThreshold, 2f, BLACK, "Max allowed temeperature");
            LimitLine minTempLimitLine = createLimitLine(minTempThreshold, 2f, BLACK, "Min allowed temeperature");
            leftAxis.addLimitLine(maxTempLimitLine);
            leftAxis.addLimitLine(minTempLimitLine);
        } else {
            Toast.makeText(this, "Device is still receiving data", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferencesIntent);
                break;
            case R.id.statistics:

                Intent statisticsIntent = new Intent(this, StatisticsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putIntArray("TEMPERATURE", temperature);
                bundle.putIntArray("HUMIDITY", humidity);
                bundle.putFloat("MAXTEMP", maxTempThreshold);
                bundle.putFloat("MINTEMP", minTempThreshold);
                bundle.putFloat("MAXHUMID", maxHumidThreshold);
                bundle.putFloat("MINHUMID", minHumidThreshold);
                statisticsIntent.putExtras(bundle);
                startActivity(statisticsIntent);
                break;
            case R.id.saved:
                Intent savedChartsIntent = new Intent(this, SavedChartsActivity.class);
                startActivity(savedChartsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        YAxis leftAxis = mLinechart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String tempString;
        tempString = sharedPreferences.getString("max_limit_temperature", "25");
        maxTempThreshold = Float.parseFloat(tempString);
        tempString = sharedPreferences.getString("min_limit_temperature", "18");
        minTempThreshold = Float.parseFloat(tempString);
        tempString = sharedPreferences.getString("max_limit_humidity", "70");
        maxHumidThreshold = Float.parseFloat(tempString);
        tempString = sharedPreferences.getString("min_limit_humidity", "35");
        minHumidThreshold = Float.parseFloat(tempString);
        if (isTemperature) {
            LimitLine maxTempLimitLine = createLimitLine(maxTempThreshold, 2f, BLACK, "Max allowed temeperature");
            LimitLine minTempLimitLine = createLimitLine(minTempThreshold, 2f, BLACK, "Min allowed temeperature");
            leftAxis.addLimitLine(maxTempLimitLine);
            leftAxis.addLimitLine(minTempLimitLine);
        } else {

            LimitLine maxHumidLimitLine = createLimitLine(maxHumidThreshold, 2f, BLACK, "Max allowed humiditiy");
            LimitLine minHumidLimitLine = createLimitLine(minHumidThreshold, 2f, BLACK, "Min allowed humiditiy");
            leftAxis.addLimitLine(maxHumidLimitLine);
            leftAxis.addLimitLine(minHumidLimitLine);
        }
        mLinechart.invalidate();
    }

    public LimitLine createLimitLine(float threshold, float width, int color, String label) {
        LimitLine limitLine = new LimitLine(threshold, label);
        limitLine.setLineWidth(width);
        limitLine.setLineColor(color);
        limitLine.enableDashedLine(10f, 20f, 0f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        limitLine.setTextSize(10f);
        return limitLine;

    }

    public void saveChartData(String data, String date) {
        boolean insertData = mDatabaseHelper.addData(data, date);


        if (insertData) {
            Toast.makeText(this, "data saved", Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(this, "not saved", Toast.LENGTH_SHORT);
        }

    }

    void setXAxisOrder(int x) {
        xAxisHours = new String[24];
        for (int i = 23; i >= 0; i--) {
            xAxisHours[i] = String.valueOf(x);
            x = x - 1;
            if (x <= 0) {
                x = x + 24;
            }
        }
    }
}




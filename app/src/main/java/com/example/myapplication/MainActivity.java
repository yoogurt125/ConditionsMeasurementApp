package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
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
    String dataAgregated;
    int[] temperature;
    int[] humidity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button openButton = (Button) findViewById(R.id.open);
        Button temperatureButton = (Button) findViewById(R.id.temperature_button);
        Button humidityButton = (Button) findViewById(R.id.humidity_button);
        Button sendButton = (Button) findViewById(R.id.send);
        myLabel = (TextView) findViewById(R.id.label);

        mLinechart = findViewById(R.id.linechart);
        mLinechart.setDragEnabled(true);
        mLinechart.setScaleEnabled(true);
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
                } catch (IOException ex) {
                }
            }
        });

        temperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseTemperatureData(dataAgregated);
            }
        });
        humidityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseHumidityData(dataAgregated);
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
        final byte delimiter = 10; //This is the ASCII code for a newline character

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

//                            for (int i = 0; i < bytesAvailable; i++) {
//                                byte b = packetBytes[i];
//                                if (b == delimiter) {
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String data = new String(encodedBytes);
//                                    readBufferPosition = 0;
//
                            handler.post(new Runnable() {
                                public void run() {


                                    dataAgregated += data;
                                    myLabel.setText("Data Received.");
                                    Log.d("data_temp", dataAgregated);

                                }
                            });
//                                } else {
//                                    readBuffer[readBufferPosition++] = b;
//                                }
//                            }
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
        dataAgregated = "";
        String msg = "1";
//        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Receiving data");
    }

    void parseHumidityData(String s) {
        if (myLabel.getText() == "Data Received.") {


            ArrayList<Entry> humidityValues = new ArrayList<>();

            humidity = new int[30];
            for (int i = 60, j = 0; i < 120 && j < 30; i += 2, j++) {
                String subTemperature = s.substring(i, i + 2);
                humidity[j] = Integer.parseInt(subTemperature, 16);
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
            mLinechart.zoom(3f, 1f, 1, 1);
            mLinechart.setData(humidityData);
            mLinechart.invalidate();
            mLinechart.animateY(3000, Easing.EaseOutBack);
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


            temperature = new int[30];
            for (int i = 0, j = 0; i < 60 && j < 30; i += 2, j++) {
                String subTemperature = s.substring(i, i + 2);
                temperature[j] = Integer.parseInt(subTemperature, 16);
            }
            for (int i = 0; i < temperature.length; i++) {
                temperatureValues.add(new Entry(i, temperature[i]));
            }

            LineDataSet temperatureSet = new LineDataSet(temperatureValues, "Temperature °C");
            temperatureSet.setLineWidth(3f);
            temperatureSet.setLineWidth(3f);
            temperatureSet.setColor(Color.RED);
            temperatureSet.setValueTextSize(15f);
            ArrayList<ILineDataSet> temperatureDataSet = new ArrayList<>();
            temperatureDataSet.add(temperatureSet);
            LineData temperatureData = new LineData(temperatureDataSet);
            mLinechart.zoom(3f, 1f, 1, 1);
            mLinechart.setData(temperatureData);
            mLinechart.invalidate();
            mLinechart.animateY(3000, Easing.EaseOutBack);
        } else {
            Toast.makeText(this, "Device is still receiving data", Toast.LENGTH_SHORT).show();
        }

    }
}



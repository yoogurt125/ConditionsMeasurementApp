package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SavedChartsActivity extends AppCompatActivity {

    ListView mlistView;
    DatabaseHelper mDatabaseHelper;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_charts);
        mlistView = findViewById(R.id.list_view_saved);
        mDatabaseHelper = new DatabaseHelper(this);

        populateListView();
    }

    private void populateListView() {
        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            listData.add(data.getString(0));
        }

        ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                listData);
        mlistView.setAdapter(listAdapter);

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String date = adapterView.getItemAtPosition(position).toString();
                Log.d("dupa", date);
                String data="";
                Cursor cursor = mDatabaseHelper.getDataFromDate(date);
                while(cursor.moveToNext()) {
                    data = cursor.getString(cursor.getColumnIndex("data"));
                }
                Intent historyChartIntent = new Intent(SavedChartsActivity.this, HistoryChartActivity.class);
                historyChartIntent.putExtra("date", date);
                historyChartIntent.putExtra("data", data);
                startActivity(historyChartIntent);


            }
        });
        mlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                new AlertDialog.Builder( SavedChartsActivity.this )
                        .setTitle( "Delete Entry" )
                        .setMessage( "Do you want to delete?" )
                        .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String date = adapterView.getItemAtPosition(position).toString();
                                String data="";
                                Cursor cursor = mDatabaseHelper.getDataFromDate(date);
                                while(cursor.moveToNext()) {
                                    data = cursor.getString(cursor.getColumnIndex("data"));
                                }
                                mDatabaseHelper.deleteData(date,data);
                                populateListView();
                                mlistView.invalidate();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        } )
                        .show();
                return true;
            }
        });
    }
}

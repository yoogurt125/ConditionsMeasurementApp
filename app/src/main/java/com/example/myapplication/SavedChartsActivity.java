package com.example.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
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
    }

    private void populateListView() {
        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            listData.add(data.getString(1));
        }

        ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                listData);
        mlistView.setAdapter(listAdapter);
    }
}

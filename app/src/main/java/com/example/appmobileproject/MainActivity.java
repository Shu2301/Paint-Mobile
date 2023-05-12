package com.example.appmobileproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.appmobileproject.Interface.ToolsListener;
import com.example.appmobileproject.Model.ToolsItem;

import java.util.ArrayList;
import java.util.List;

import Adapters.ToolsAdapters;

public class MainActivity extends AppCompatActivity implements ToolsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTools();

    }

    private void initTools() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tools);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL, false));
        ToolsAdapters toolsAdapters = new ToolsAdapters(loadTools(), this);
        recyclerView.setAdapter(toolsAdapters);
    }

    private List<ToolsItem> loadTools() {
        List<ToolsItem> result = new ArrayList<>();

        result.add(new ToolsItem(R.drawable.baseline_brush_24, "brush"));
        result.add(new ToolsItem(R.drawable.eraser, "eraser"));
        result.add(new ToolsItem(R.drawable.baseline_palette_24, "color"));
        result.add(new ToolsItem(R.drawable.palleter, "background"));
        result.add(new ToolsItem(R.drawable.baseline_undo_24, "return"));

        return result;
    }

    public void finishPaint(View view) {
        finish();
    }

    public void shareApp(View view) {
    }

    public void showFiles(View view) {
    }

    public void saveFiles(View view) {
    }

    @Override
    public void onSeclected(String name) {

    }
}
package com.example.appmobileproject;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmobileproject.Interface.ToolsListener;
import com.example.appmobileproject.Width.PaintView;
import com.example.appmobileproject.adapters.ToolsAdapter;
import com.example.appmobileproject.common.common;
import com.example.appmobileproject.model.ToolsItem;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ToolsListener {

    PaintView mPaintView;
    int colorBackground, colorBrush;
    int brushSize,eraserSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTools();
    }

    private void initTools() {

        colorBackground = Color.WHITE;
        colorBrush = Color.BLACK;

        mPaintView = findViewById(R.id.paint_view);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_tools);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL, false));
        ToolsAdapter toolsAdapters = new ToolsAdapter(loadTools(), this);
        recyclerView.setAdapter(toolsAdapters);
    }

    private List<ToolsItem> loadTools() {

        List<ToolsItem> result = new ArrayList<>();

        result.add(new ToolsItem(R.drawable.baseline_brush_24, "common.BRUSH"));
        result.add(new ToolsItem(R.drawable.eraser_white, "common.ERASER"));
        result.add(new ToolsItem(R.drawable.baseline_palette_24, "common.COLORS"));
        result.add(new ToolsItem(R.drawable.paint, "common.BACKGROUND"));
        result.add(new ToolsItem(R.drawable.baseline_undo_24, "common.RETURN"));

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

        switch (name){
            case common.BRUSH:
                mPaintView.desableEraser();
                showDialogSize(false);
                break;
            case common.ERASER:
                mPaintView.enableEraser();
                showDialogSize(true);
                break;
            case common.RETURN:
                mPaintView.returnLastAction();
                break;
            case common.BACKGROUND:
                updateColor(name);
                break;
            case common.COLORS:
                updateColor(name);
                break;

        }
    }

    private void updateColor(String name) {
        int color;
        if(name.equals(common.BACKGROUND)){
            color = colorBackground;
        }else {
            color = colorBrush;
        }

        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose colors")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("OK", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        if(name.equals(common.BACKGROUND)){
                            colorBackground = lastSelectedColor;
                            mPaintView.setBackgroundColor(colorBackground);
                        }else {
                            colorBrush = lastSelectedColor;
                            mPaintView.setBrushColor(colorBrush);
                        }
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).build()
                    .show();

    }

    private void showDialogSize(boolean isEraser){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null, false);
        TextView toolsSelected = view.findViewById(R.id.status_tools_selected);
        TextView statusSize = view.findViewById(R.id.status_size);
        ImageView ivTools = view.findViewById(R.id.iv_tools);
        SeekBar seekBar = view.findViewById(R.id.seekbar_size);
        seekBar.setMax(99);

        if (isEraser){

            toolsSelected.setText("Eraser Size");
            ivTools.setImageResource(R.drawable.eraser_black);
            statusSize.setText("Selected Size: "+eraserSize);

        }else{

            toolsSelected.setText("Brush Size");
            ivTools.setImageResource(R.drawable.baseline_brush_black_24);
            statusSize.setText("Selected Size: "+brushSize);

        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(isEraser){

                    eraserSize = i + 1;
                    statusSize.setText("Selected Size: "+eraserSize);
                    mPaintView.setSizeEraser(eraserSize);

                }else {

                    brushSize = i + 1;
                    statusSize.setText("Selected Size: "+brushSize);
                    mPaintView.setSizeBrush(brushSize);

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setView(view);
        builder.show();

    }
}
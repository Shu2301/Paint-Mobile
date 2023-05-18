package com.example.appmobileproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmobileproject.Interface.ToolsListener;
import com.example.appmobileproject.widget.PaintView;
import com.example.appmobileproject.adapters.ToolsAdapter;
import com.example.appmobileproject.common.common;
import com.example.appmobileproject.model.ToolsItem;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ToolsListener {

    private static final int REQUEST_PERMISSION = 1001;
    private static final int PICK_IMAGE = 1000;
    PaintView mPaintView;
    int colorBackground, colorBrush;
    int brushSize,eraserSize;
    private Context ActivityCompat;

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
        eraserSize = brushSize = 12;

        RecyclerView recyclerView = findViewById(R.id.recycler_view_tools);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL, false));
        ToolsAdapter toolsAdapters = new ToolsAdapter(loadTools(), this);
        recyclerView.setAdapter(toolsAdapters);
    }

    private List<ToolsItem> loadTools() {

        List<ToolsItem> result = new ArrayList<>();

        result.add(new ToolsItem(R.drawable.baseline_brush_24, common.BRUSH));
        result.add(new ToolsItem(R.drawable.eraser_white, common.ERASER));
        result.add(new ToolsItem(R.drawable.baseline_image_24, common.IMAGE));
        result.add(new ToolsItem(R.drawable.baseline_palette_24, common.COLORS));
        result.add(new ToolsItem(R.drawable.paint, common.BACKGROUND));
        result.add(new ToolsItem(R.drawable.baseline_undo_24, common.RETURN));

        return result;
    }

    public void finishPaint(View view) {
        finish();
    }

    public void shareApp(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String bodyText = "http://play.google.com/store/apps/details?id="+getPackageName();
        intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT,bodyText);
        startActivities(Intent.createChooser(intent, "share this app"));
    }

    public void showFiles(View view) {
        startActivities(new Intent[this, ListFileAct.class]);
    }

    public void saveFiles(View view) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);

        }else {
            saveBitmap();
        }
    }

    private void saveBitmap() {
        Bitmap bitmap = mPaintView.getBitmap();
        String file_name = UUID.randomUUID() + ".png";
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ File.separator+getString(R.id.app_name));

        if(!folder.exists()){
            folder.mkdir();
        }

        try{
            FileOutputStream fileOutputStream = new FileOutputStream(folder+File.separator+file_name);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            Toast.makeText(this, "picture save", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_PERMISSION && grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveBitmap();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSeclected(String name) {

        switch (name){
            case common.BRUSH:
                mPaintView.toMove = false;
                mPaintView.desableEraser();
                mPaintView.invalidate();
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
            case common.IMAGE:
                getImage();
                break;
        }
    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"),PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == PICK_IMAGE && data != null && resultCode == RESULT_OK){
            Uri pickedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pickedImage,filePath,null, null,null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            mPaintView.setImage(bitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
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
                            mPaintView.setColorBackground(colorBackground);
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
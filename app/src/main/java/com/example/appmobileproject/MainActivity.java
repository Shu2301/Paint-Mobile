package com.example.appmobileproject;



import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ToolsListener {

    private static final int REQUEST_PERMISSION = 1001;
//    private static final int PICK_IMAGE = 1000;
    private static final int REQUEST_FOR_GET_IMAGE_FROM_GALLERY = 1002;

    private TextView statusNumber;
    private SeekBar seekBar;
    private android.widget.Button minusBtn;
    private android.widget.Button plusBtn;

    private ImageView sizeView;

    PaintView mPaintView;

    int colorBackground, colorBrush;
    int brushSize,eraserSize;
    private int hSizeView, wSizeView;

    private ActivityResultLauncher<String> pickImageLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTools();
        showDialogSize(false);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                Uri imageUri = result;
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    mPaintView.setImage(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
//
//    @Override
//    protected void onResume(){
//        super.onResume();
//        mPaintView.startDrawThread();
//    }
//
//    @Override
//    protected void onPause() {
//        mPaintView.stopDrawThread();
//        super.onPause();
//    }
//
//    protected void onDestroy(){
//        super.onDestroy();
//    }


    private void initTools() {

        colorBackground = Color.WHITE;
        colorBrush = Color.BLACK;

        mPaintView = findViewById(R.id.paint_view);
        eraserSize = brushSize = 1;

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
        startActivity(Intent.createChooser(intent, "share this app"));
    }

    public void showFiles(View view) {
        startActivity(new Intent(this, ListFileAct.class));
    }

    public void saveFiles(View view) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);

        }else {
            try {
                saveBitmap();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveBitmap() throws IOException{

        Bitmap bitmap = mPaintView.getBitmapFromView();
        String file_name = UUID.randomUUID() + ".png";
        OutputStream outputStream;
        boolean saved;
        File folder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ File.separator+getString(R.string.app_name));
        }else {
            folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ File.separator+getString(R.string.app_name));
        }

        if(!folder.exists()){
            folder.mkdirs();
        }
        File image = new File(folder+File.separator+file_name);
        Uri imageUri = Uri.fromFile(image);
        outputStream = new FileOutputStream(image);
        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file_name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+File.separator+getString(R.string.app_name));
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
            outputStream = resolver.openOutputStream(uri);
            saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        }else {
            sendPictureToGallery(imageUri);
        }

        if(saved)
            Toast.makeText(this, "picture save", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "picture not save", Toast.LENGTH_SHORT).show();
        outputStream.flush();
        outputStream.close();

    }
    private void sendPictureToGallery(Uri imageUri) {
        String[] filePaths = {imageUri.getPath()};
        MediaScannerConnection.scanFile(
                this,
                filePaths,
                null,
                (path, uri) -> {
                    // Media scan is complete, you can perform any additional actions here
                }
        );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if( grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(requestCode == REQUEST_PERMISSION){
                try {
                    saveBitmap();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == REQUEST_FOR_GET_IMAGE_FROM_GALLERY){
                getImage();
            }
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
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        !=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FOR_GET_IMAGE_FROM_GALLERY);
                }else
                getImage();
                break;
        }
    }

    private void getImage() {
        pickImageLauncher.launch("image/*");
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
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
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
        statusNumber = findViewById(R.id.status_number);
        seekBar = findViewById(R.id.seekbar);

        minusBtn = findViewById(R.id.minusButton);
        plusBtn = findViewById(R.id.plusButton);

        sizeView = findViewById(R.id.size_view);
        ViewGroup.LayoutParams params = sizeView.getLayoutParams();

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int progress = seekBar.getProgress() - 1;
                seekBar.setProgress(progress);
            }
        });

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int progress = seekBar.getProgress() + 1;
                seekBar.setProgress(progress);
            }
        });

        if (isEraser){
            seekBar.setMax(999);
            statusNumber.setText(eraserSize + ".0");
        }else{
            seekBar.setMax(29);
            statusNumber.setText(brushSize + ".0");
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sizeView.setVisibility(View.VISIBLE);
                if(isEraser){
                    hSizeView = wSizeView = eraserSize = i + 1;
                    statusNumber.setText(eraserSize + ".0");
                    mPaintView.setSizeEraser(eraserSize);
                    params.width = dpToPx(wSizeView);
                    params.height = dpToPx(hSizeView);
                    sizeView.setLayoutParams(params);
                }else {
                    hSizeView = wSizeView = brushSize = i + 1;
                    statusNumber.setText(brushSize + ".0");
                    mPaintView.setSizeBrush(brushSize);
                    params.width = dpToPx(wSizeView);
                    params.height = dpToPx(hSizeView);
                    sizeView.setLayoutParams(params);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sizeView.setVisibility(View.INVISIBLE);
            }
        });
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}
package com.example.appmobileproject.widget;

import static android.provider.MediaStore.Images.Media.getBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.appmobileproject.R;

import java.util.ArrayList;

public class PaintView extends View {

    private Bitmap btmBackground, btmView, image, captureImage;
    private Paint mPaint = new Paint();
    private Path mPath = new Path();
    private int colorBackground, sizeBrush, sizeEraser;
    private float mX, mY;
    private Canvas mCanvas;
    private final int DIFFERENCE_SPACE = 4;
    private ArrayList<Bitmap> listAction = new ArrayList<>();
    private int leftImage = 50,topImage = 50;
    public static boolean toMove = false;
    private float refX, refY;
    private int xCenter, yCenter;


    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);


        init();
    }

    private void init() {
        sizeEraser = sizeBrush = 12;
        colorBackground = Color.WHITE;

        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(toPx(sizeBrush));

        captureImage = BitmapFactory.decodeResource(getResources(), R.drawable.capture);
    }

    private float toPx(int sizeBrush) {
        return sizeBrush*(getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        btmBackground = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        btmView = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(btmView);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(colorBackground);
        canvas.drawBitmap(btmBackground, 0, 0, null);

        if(image != null && toMove) {
            canvas.drawBitmap(image, leftImage, topImage, null);
            xCenter = leftImage + image.getWidth()/2 - captureImage.getWidth()/2;
            yCenter = topImage + image.getHeight()/2 - captureImage.getHeight()/2;
            canvas.drawBitmap(captureImage, xCenter, yCenter, null);
        }
        canvas.drawBitmap(btmView, 0, 0, null);
    }

    public void setColorBackground(int color){
        colorBackground = color;
        invalidate();
    }

    public void setSizeBrush(int s){
        sizeBrush = s;
        mPaint.setStrokeWidth(toPx(sizeBrush));
    }

    public void setBrushColor(int color){
        mPaint.setColor(color);
    }

    public void setSizeEraser(int s){
        sizeEraser = s;
        mPaint.setStrokeWidth(toPx(sizeEraser));
    }

    public void enableEraser(){
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void desableEraser(){
        mPaint.setXfermode(null);
        mPaint.setShader(null);
        mPaint.setMaskFilter(null);
    }

    public void addLastAction(Bitmap bitmap){
        listAction.add(bitmap);
    }

    public void returnLastAction(){
        if(listAction.size() > 0){

            listAction.remove(listAction.size() - 1);

            if(listAction.size() > 0){

                btmView = listAction.get(listAction.size() - 1);

            }
            else {
                btmView = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            }

            mCanvas = new Canvas(btmView);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                refY = y;
                refX = x;

                if(toMove){
                    if(refX >= xCenter && refX < xCenter + captureImage.getWidth()
                    && refY >= yCenter && refY < yCenter + captureImage.getHeight()){
                        Canvas newCanvas = new Canvas(btmBackground);
                        newCanvas.drawBitmap(image, leftImage, topImage, null);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!toMove)
                    touchMove(x,y);
                else {
                    float nX = event.getX();
                    float nY = event.getY();

                    leftImage += nX - refX;
                    topImage += nY - refY;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                addLastAction(getBitmap());
                break;
        }

        return true;
    }

    private void touchUp() {
        mPath.reset();
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if(dx >= DIFFERENCE_SPACE || dy >= DIFFERENCE_SPACE){
            mPath.quadTo(x,y, (x+mX)/2, (y+mY)/2);

            mY = y;
            mX = x;

            mCanvas.drawPath(mPath, mPaint);
            invalidate();
        }

    }
    private void touchStart(float x, float y) {
        mPath.moveTo(x,y);
        mX = x;
        mY = y;
    }

    Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void setImage(Bitmap bitmap) {
        toMove = true;
        image = Bitmap.createScaledBitmap(bitmap, getWidth()/2, getHeight()/2, true);
        invalidate();
    }
}

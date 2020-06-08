package com.example.writemob;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ArrayList;


import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Matrix;
/* Bluetooth Importları */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 *
 * Class which provides the view on which drawing takes place.
 */
public class DrawingView extends View{
    int index = 0;
    float [][] xy = new float [999][999];
    float[][]coordinates;

    private MainActivity mActivity;

    // To hold the path that will be drawn.
    private Path drawPath;
    // Paint object to draw drawPath and drawCanvas.
    private Paint drawPaint, canvasPaint;
    // initial color
    private int paintColor = 0xff000000;
    private int previousColor = paintColor;
    // canvas on which drawing takes place.
    private Canvas drawCanvas;
    // canvas bitmap
    private Bitmap canvasBitmap;
    // Brush stroke width
    private float brushSize, lastBrushSize;
    // To enable and disable erasing mode.
    private boolean erase = false;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setUpDrawing();
    }

    /**
     * Initialize all objects required for drawing here.
     * One time initialization reduces resource consumption.
     */
    private void setUpDrawing(){
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        // Making drawing smooth.
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        // Initial brush size is medium.
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
        drawPaint.setStrokeWidth(brushSize);
    }
    //Elde edilen koordinatlarda null olan değerleri silmek için dinamik dizi oluşturduk.
    public float[][] createCoordinates(float xy[][], int index){
        float [][] coords = new float[index][index];
        for(int i=0; i<index; i ++){
            coords[i][0] = xy[i][0];
            coords[i][1] = xy[i][1];
        }
        return coords;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // X and Y position of user touch.
        float touchX = event.getX();
        float touchY = event.getY();
        // Draw the path according to the touch event taking place.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                Log.i("touchX DOWN =" , String.valueOf(touchX));
                Log.i("touchy DOWN=" , String.valueOf(touchY));
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                index += 1;
                xy[index-1][0] = touchX;
                xy[index-1][1] = touchY;

                Log.i("Koordinatlar" , String.valueOf(xy[index-1][0] +".."+ xy[index-1][1])); ; //.add(touchY);
          //      Log.i("tampon değeri" , String.valueOf(coordinates[0][0]+ "..." +  coordinates[0][1]));
                break;
            case MotionEvent.ACTION_UP:
                if (erase){
                    drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }
                drawCanvas.drawPath(drawPath, drawPaint);
                coordinates = createCoordinates(xy,index);
                Log.i("Nihai Koordinat arrayi boyutu =" , String.valueOf(coordinates.length));
                for(int i=0; i<index; i++){
                    Log.i("Nihai Koordinat arrayi" ,  String.valueOf(i) +" nci dönüş = " + String.valueOf(coordinates[i][0]+ "..." +  coordinates[i][1]));
                }
                try {

                    mActivity.sendData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                drawPath.reset();
                drawPaint.setXfermode(null);
                break;
            default:
                return false;
        }

        // invalidate the view so that canvas is redrawn.
        invalidate();
        return true;
    }

    public void setColor(String newColor){
        // invalidate the view
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
        previousColor = paintColor;
    }

    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public void setErase(boolean isErase){
        //set erase true or false
        erase = isErase;
        if(erase) {
            drawPaint.setColor(Color.WHITE);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        else {
            drawPaint.setColor(previousColor);
            drawPaint.setXfermode(null);

        }
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}


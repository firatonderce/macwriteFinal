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
    String coords = "";
    String coordX = "";
    String coordY = "";
    int l;
    String b = "G1X29,89Y1,18F3000.00";
    boolean isPenDown = false;
    String penDownCommand = "M300 S30";
    String penUpCommand = "M300 S50.00";


    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xff000000;
    private int previousColor = paintColor;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize, lastBrushSize;
    private boolean erase = false;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setUpDrawing();
    }


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
//    public float[][] createCoordinates(float xy[][], int index){
//        float [][] coords = new float[index][index];
//        for(int i=0; i<index; i ++){
//            coords[i][0] = xy[i][0];
//            coords[i][1] = xy[i][1];
//        }
//        return coords;
//    }
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


    public void coordFunction(float touchX, float touchY) {
        coordX = String.format("%.2f",(touchX/26));
        coordY = String.format("%.2f",(touchY/26));
        coords ="G1 X" + coordX + " " + "Y"  +coordY + " " + "F3000.00" + "\n";
        Log.i("coords ====?==" , coords);
        try {
            MainActivity.getInstance().penDown();
            MainActivity.getInstance().sendData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void coordFunction(float touchX, float touchY, boolean penValue) {
        coordX = String.format("%.2f",(touchX/26));
        coordY = String.format("%.2f",(touchY/26));
        coords ="G1 X" + coordX + " " + "Y"  +coordY + " " + "F3000.00" + "\n";
        Log.i("coords ====?==" , coords);
        try {
            MainActivity.getInstance().sendData();
            if(penValue)
              MainActivity.getInstance().penDown();
            else
              MainActivity.getInstance().penUp();
            MainActivity.getInstance().penUp();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public void penDownAction(float touchX, float touchY){
//        MainActivity.getInstance().penDown();
//        coordX = String.format("%.2f",(touchX/26));
//        coordY = String.format("%.2f",(touchY/26));
//        coords ="G1 X" + coordX + " " + "Y"  +coordY + " " + "F3000.00" + "\n";
//    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPenDown = true;
                drawPath.moveTo(touchX, touchY);
                coordFunction(touchX,touchY,isPenDown);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                coordFunction(touchX,touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                    isPenDown = false;
                 coordFunction(touchX,touchY,isPenDown);

                drawPath.reset();
                drawPaint.setXfermode(null);
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }
}


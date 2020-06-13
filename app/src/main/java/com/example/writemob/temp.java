//package com.example.writemob;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.View;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//import java.text.CollationElementIterator;
//import java.util.UUID;
///* Bluetooth Importları */
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.EditText;
//import android.widget.Button;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Set;
//import java.util.UUID;
//import java.util.zip.CheckedOutputStream;
//
//public class MainActivity extends AppCompatActivity implements View.OnClickListener{
//    private static android.util.Log Log;
//    private static DrawingView mDrawingView;
//    private static TextView myLabel;
//    private static CheckedOutputStream mmmOutputStream;
//    TextView mmyLabel;
//    String slm = " nbr ";
//    // will enable user to enter any text to be printed
//    EditText myTextbox;
//    BluetoothAdapter mBluetoothAdapter;
//    BluetoothSocket mmSocket;
//    BluetoothDevice mmDevice;
//
//    OutputStream mmOutputStream;
//    InputStream mmInputStream;
//    Thread workerThread;
//
//    byte[] readBuffer;
//    int readBufferPosition;
//    int counter;
//    volatile boolean stopWorker;
//
//    private DrawingView mmDrawingView;
//    private ImageButton currPaint, drawButton, eraseButton, newButton;
//    private float smallBrush, mediumBrush, largeBrush;
//    private android.util.Log Logg;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        mDrawingView = (DrawingView)findViewById(R.id.drawing);
//        // Getting the initial paint color.
//        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
//        // 0th child is white color, so selecting first child to give black as initial color.
//        currPaint = (ImageButton)paintLayout.getChildAt(1);
//        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.pallet_pressed));
//        drawButton = (ImageButton) findViewById(R.id.buttonBrush);
//        drawButton.setOnClickListener(this);
//        eraseButton = (ImageButton) findViewById(R.id.buttonErase);
//        eraseButton.setOnClickListener(this);
//        newButton = (ImageButton) findViewById(R.id.buttonNew);
//        newButton.setOnClickListener(this);
//
//        smallBrush = getResources().getInteger(R.integer.small_size);
//        mediumBrush = getResources().getInteger(R.integer.medium_size);
//        largeBrush = getResources().getInteger(R.integer.large_size);
//        // Set the initial brush size
//        mDrawingView.setBrushSize(mediumBrush);
//
//        try {
//            Button openButton = (Button) findViewById(R.id.open);
//            Button sendButton = (Button) findViewById(R.id.send);
//
//            myLabel = (TextView) findViewById(R.id.label);
//            myTextbox = (EditText) findViewById(R.id.entry);
//            // open bluetooth connection
//            openButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    try {
//                        findBT();
//                        openBT();
//                    } catch (IOException ex) {
//                    }
//                }
//            });
//            // send data typed by the user to be printed
//            sendButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    try {
//                        sendData();
//                    } catch (IOException ex) {
//                    }
//                }
//            });
//
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    void findBT() {
//        try {
//            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//            if (mBluetoothAdapter == null) {
//                myLabel.setText("No bluetooth adapter available");
//            }
//
//            if (!mBluetoothAdapter.isEnabled()) {
//                Intent enableBluetooth = new Intent(
//                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBluetooth, 0);
//            }
//
//            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
//                    .getBondedDevices();
//            if (pairedDevices.size() > 0) {
//                for (BluetoothDevice device : pairedDevices) {
//                    if (device.getName() != null) {
//                        mmDevice = device;
//                        break;
//                    }
//                }
//            }
//            myLabel.setText("Bluetooth Device Found");
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    void openBT() throws IOException {
//        try {
//            // Standard SerialPortService ID
//            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
//            mmSocket.connect();
//            mmOutputStream = mmSocket.getOutputStream();
//            mmInputStream = mmSocket.getInputStream();
//
//            beginListenForData();
//            myLabel.setText("Bluetooth Opened");
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    void beginListenForData() {
//        try {
//            final Handler handler = new Handler();
//            // This is the ASCII code for a newline character
//            final byte delimiter = 10;
//            stopWorker = false;
//            readBufferPosition = 0;
//            readBuffer = new byte[1024];
//
//            workerThread = new Thread(new Runnable() {
//                public void run() {
//                    while (!Thread.currentThread().isInterrupted()
//                            && !stopWorker) {
//
//                        try {
//
//                            int bytesAvailable = mmInputStream.available();
//                            if (bytesAvailable > 0) {
//                                byte[] packetBytes = new byte[bytesAvailable];
//                                mmInputStream.read(packetBytes);
//                                for (int i = 0; i < bytesAvailable; i++) {
//                                    byte b = packetBytes[i];
//                                    if (b == delimiter) {
//                                        byte[] encodedBytes = new byte[readBufferPosition];
//                                        System.arraycopy(readBuffer, 0,
//                                                encodedBytes, 0,
//                                                encodedBytes.length);
//                                        final String data = new String(
//                                                encodedBytes, "US-ASCII");
//                                        readBufferPosition = 0;
//
//                                        handler.post(new Runnable() {
//                                            public void run() {
//                                                myLabel.setText(data);
//                                            }
//                                        });
//                                    } else {
//                                        readBuffer[readBufferPosition++] = b;
//                                    }
//                                }
//                            }
//
//                        } catch (IOException ex) {
//                            stopWorker = true;
//                        }
//
//                    }
//                }
//            });
//
//            workerThread.start();
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public static void tryData() throws IOException{
//        try {
//            String sa = " sl";
//            Log.i("senddata action up çalıştı",sa);
//            mmmOutputStream.write(sa.getBytes());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//    void sendData() throws IOException {
//        try {
//            float[][] coordinates = mDrawingView.coordinates;
//            int size = mDrawingView.index;
//            int msg = 11;
//            String payload = "";
//            String temp = "";
//            Log.i("senddata action up çalıştı",temp);
//
//            for(int i =0; i<size; i++){
//                temp = String.valueOf(coordinates[i][0]) + "," + String.valueOf(coordinates[i][1]) + ";" ;
//                payload += temp;
//            }
//            Log.i("senddata payload" , payload);
//            mmOutputStream.write(payload.getBytes());
//            // myLabel.setText("Data Sent");
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    void trytry() {
//        int msg = 99;
//    }
//    /**
//     * Method is called when color is clicked from pallet.
//     * @param view ImageButton on which click took place.
//     */
//    public void paintClicked(View view){
//        if (view != currPaint){
//            // Update the color
//            ImageButton imageButton = (ImageButton) view;
//            String colorTag = imageButton.getTag().toString();
//            mDrawingView.setColor(colorTag);
//            // Swap the backgrounds for last active and currently active image button.
//            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.pallet_pressed));
//            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.pallet));
//            currPaint = (ImageButton)view;
//            mDrawingView.setErase(false);
//            mDrawingView.setBrushSize(mDrawingView.getLastBrushSize());
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        int id = v.getId();
//        switch(id){
//            case R.id.buttonBrush:
//                // Show brush size chooser dialog
//                showBrushSizeChooserDialog();
//                break;
//            case R.id.buttonErase:
//                // Show eraser size chooser dialog
//                showEraserSizeChooserDialog();
//                break;
//            case R.id.buttonNew:
//                // Show new painting alert dialog
//                showNewPaintingAlertDialog();
//                break;
//        }
//    }
//
//    private void showBrushSizeChooserDialog(){
//        final Dialog brushDialog = new Dialog(this);
//        brushDialog.setContentView(R.layout.dialog_brush_size);
//        brushDialog.setTitle("Brush size:");
//        ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
//        smallBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.setBrushSize(smallBrush);
//                mDrawingView.setLastBrushSize(smallBrush);
//                brushDialog.dismiss();
//            }
//        });
//        ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
//        mediumBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.setBrushSize(mediumBrush);
//                mDrawingView.setLastBrushSize(mediumBrush);
//                brushDialog.dismiss();
//            }
//        });
//
//        ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
//        largeBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                mDrawingView.setBrushSize(largeBrush);
//                mDrawingView.setLastBrushSize(largeBrush);
//                brushDialog.dismiss();
//            }
//        });
//        mDrawingView.setErase(false);
//        brushDialog.show();
//    }
//
//    private void showEraserSizeChooserDialog(){
//        final Dialog brushDialog = new Dialog(this);
//        brushDialog.setTitle("Eraser size:");
//        brushDialog.setContentView(R.layout.dialog_brush_size);
//        ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
//        smallBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                mDrawingView.setErase(true);
//                mDrawingView.setBrushSize(smallBrush);
//                brushDialog.dismiss();
//            }
//        });
//        ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
//        mediumBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.setErase(true);
//                mDrawingView.setBrushSize(mediumBrush);
//                brushDialog.dismiss();
//            }
//        });
//        ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
//        largeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.setErase(true);
//                mDrawingView.setBrushSize(largeBrush);
//                brushDialog.dismiss();
//            }
//        });
//        brushDialog.show();
//    }
//
//    private void showNewPaintingAlertDialog(){
//        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
//        newDialog.setTitle("New drawing");
//        newDialog.setMessage("Start new drawing (you will lose the current drawing)?" + mDrawingView.coordinates.length);
//        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                mDrawingView.startNew();
//                dialog.dismiss();
//            }
//        });
//        newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        newDialog.show();
//    }
//
//    private void showSavePaintingConfirmationDialog(){
//        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
//        saveDialog.setTitle("Save drawing");
//        saveDialog.setMessage("Save drawing to device Gallery?");
//        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
//            public void onClick(DialogInterface dialog, int which){
//                //save drawing
//                mDrawingView.setDrawingCacheEnabled(true);
//                String imgSaved = MediaStore.Images.Media.insertImage(
//                        getContentResolver(), mDrawingView.getDrawingCache(),
//                        UUID.randomUUID().toString()+".png", "drawing");
//                if(imgSaved!=null){
//                    Toast savedToast = Toast.makeText(getApplicationContext(),
//                            "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
//                    savedToast.show();
//                }
//                else{
//                    Toast unsavedToast = Toast.makeText(getApplicationContext(),
//                            "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
//                    unsavedToast.show();
//                }
//                // Destroy the current cache.
//                mDrawingView.destroyDrawingCache();
//            }
//        });
//        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
//            public void onClick(DialogInterface dialog, int which){
//                dialog.cancel();
//            }
//        });
//        saveDialog.show();
//    }
//
//}

package com.example.writemob;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.text.CollationElementIterator;
import java.util.UUID;
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
import java.util.zip.CheckedOutputStream;


public class MainActivity extends AppCompatActivity{
    private static android.util.Log Log;
    private static DrawingView mDrawingView;
    private static TextView myLabel;
    private static CheckedOutputStream mmmOutputStream;
    /*Bluetooth componentleri*/
    // will show the statuses
    TextView mmyLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    private DrawingView mmDrawingView;
    private ImageButton currPaint, drawButton, eraseButton, newButton;
    private float smallBrush, mediumBrush, largeBrush;
    private android.util.Log Logg;
    private static MainActivity instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        mDrawingView = (DrawingView)findViewById(R.id.drawing);

        try {
            Button openButton = (Button) findViewById(R.id.open);
            Button sendButton = (Button) findViewById(R.id.send);
            Button upButton = (Button) findViewById(R.id.up);
            Button downButton = (Button) findViewById(R.id.down);

            myLabel = (TextView) findViewById(R.id.label);
            myTextbox = (EditText) findViewById(R.id.entry);

            // open bluetooth connection
            openButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        findBT();
                        openBT();
                    } catch (IOException ex) {
                    }
                }
            });
            upButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        penUp();
                    } catch (IOException ex) {
                    }
                }
            });
            downButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        penDown();
                    } catch (IOException ex) {
                    }
                }
            });

            sendButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        fakedata();
                    } catch (IOException ex) {
                    }
                }
            });

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static MainActivity getInstance(){
        return instance;
    }
    void findBT() {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                myLabel.setText("Bluetooth baglantisi bulunamadi");
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName() != null) {
                        mmDevice = device;
                        break;
                    }
                }
            }
            myLabel.setText("Bluetooth Cihazi bulundu.");
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void openBT() throws IOException {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            myLabel.setText("Bluetooth Baglandi!");
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void beginListenForData() {
        try {
            final Handler handler = new Handler();
            // This is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted()
                            && !stopWorker) {
                        try {

                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length);
                                        final String data = new String(
                                                encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        handler.post(new Runnable() {
                                            public void run() {
                                                myLabel.setText(data);
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });
            workerThread.start();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void fakedata() throws IOException{
        String [] datas = new String[11];
        datas[0] = "G1 X-19.29 Y112.43 F3500.00\n";
        datas[1] = "G1 X-19.52 Y110.67 F3500.00\n";
        datas[2] = "G1 X-19.80 Y97.00 F3500.00\n";
        datas[3] = "G1 X20.02 Y-11.01 F3500.00\n";
        datas[4] = "G1 X19.90 Y-135.78 F3500.00\n";
        datas[5] = "G1 X19.51 Y-36.46 F3500.00\n";
        datas[6] = "G1 X-18.88 Y-136.58 F3500.00\n";
        datas[7] = "G1 X-18.63 Y-135.98 F3500.00\n";
        datas[8] = "G1 X-18.44 Y-133.57 F3500.00\n";
        datas[9] = "G1 X-18.20 Y-118.35 F3500.00\n";
        datas[10] = "G1 X-18.08 Y-11.80 F3500.00\n";
        for(int i = 0; i<11 ; i++){
            mmOutputStream.write(datas[i].getBytes());
        }
    }
//    void penDownButton() throws IOException{  //useless
//        try {
//            String penDownCommand = mDrawingView.penDownCommand;
//            mmOutputStream.write(penDownCommand.getBytes());
//            Log.i("penDown action  çalisti",penDownCommand);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//    void penUpButton() throws IOException{  //useless
//        try {
//            String penUpCommand = mDrawingView.penUpCommand;
//            mmOutputStream.write(penUpCommand.getBytes());
//            Log.i("penup action  çalisti", penUpCommand);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
     void penDown() throws IOException{  //useless
        try {
            String penDownCommand = mDrawingView.penDownCommand;
            String payload ="";
            payload = mDrawingView.coords;
            mmOutputStream.write(penDownCommand.getBytes());
            Log.i("penDown action  çalisti",payload);
            mmOutputStream.write(payload.getBytes()); //StandardCharsets.UTF_8
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    void penUp() throws IOException{  //useless
        try {
            String penUpCommand = mDrawingView.penUpCommand;
            String payload ="";
            payload = mDrawingView.coords;
            mmOutputStream.write(penUpCommand.getBytes());
            Log.i("penUp action  çalisti",payload);
            mmOutputStream.write(payload.getBytes()); //StandardCharsets.UTF_8
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void sendData() throws IOException {
        try {
            String payload = "";
            payload = mDrawingView.coords;
            Log.i("sendData action çalıştı",payload);
            mmOutputStream.write(payload.getBytes()); //StandardCharsets.UTF_8
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
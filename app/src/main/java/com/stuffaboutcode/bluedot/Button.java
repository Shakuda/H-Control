package com.stuffaboutcode.bluedot;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.app.ProgressDialog;

import com.stuffaboutcode.logger.Log;

public class Button extends AppCompatActivity {

    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private StringBuffer mInStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;

    String address = null;
    String deviceName = null;

    private ProgressDialog progress;
    private double last_x = 0;
    private double last_y = 0;

    private DynamicMatrix matrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_button);

        Intent newint = getIntent();
        deviceName = newint.getStringExtra(Devices.EXTRA_NAME);
        address = newint.getStringExtra(Devices.EXTRA_ADDRESS);

        TextView statusView = (TextView)findViewById(R.id.status);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        // Initialize the buffer for incoming messages
        mInStringBuffer = new StringBuffer("");

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, true);

        matrix = findViewById(R.id.matrix);

        // Once connected setup the listener
        matrix.setOnUseListener(new DynamicMatrix.DynamicMatrixListener() {
            @Override
            public void onPress(DynamicMatrix.MatrixCell cell, int pointerId, float actual_x, float actual_y) {
                double x = calcX(cell, actual_x);
                double y = calcY(cell, actual_y);
                send(buildMessage("1", x, y));
                last_x = x;
                last_y = y;
            }

            @Override
            public void onMove(DynamicMatrix.MatrixCell cell, int pointerId, float actual_x, float actual_y) {
                double x = calcX(cell, actual_x);
                double y = calcY(cell, actual_y);
                if ((x != last_x) || (y != last_y)) {
                    send(buildMessage("2", x, y));
                    last_x = x;
                    last_y = y;
                }
            }

            @Override
            public void onRelease(DynamicMatrix.MatrixCell cell, int pointerId, float actual_x, float actual_y) {
                double x = calcX(cell, actual_x);
                double y = calcY(cell, actual_y);
                send(buildMessage("0", x, y));
                last_x = x;
                last_y = y;
            }

        });

    }

    public void Auto(View view) {
        send(autoMessage("4"));

//        android.widget.Button buttAuto = findViewById(R.id.buttAuto);
//        buttAuto.animate().alpha(0).setDuration(300).start();
//        buttAuto.setEnabled(false);
//
//        android.widget.Button buttManual = findViewById(R.id.buttManual);
//        buttManual.animate().alpha(1).setDuration(300).start();
//        buttManual.setEnabled(true);
    }

    public void Manual(View view) {
        send(autoMessage("5"));

//        android.widget.Button buttManual = findViewById(R.id.buttManual);
//        buttManual.animate().alpha(0).setDuration(300).start();
//        buttManual.setEnabled(false);
//
//        android.widget.Button buttAuto = findViewById(R.id.buttAuto);
//        buttAuto.animate().alpha(1).setDuration(300).start();
//        buttAuto.setEnabled(true);
    }

    private double calcX(DynamicMatrix.MatrixCell cell, float actual_x) {

        double relative_x = actual_x - cell.getBounds().left;
        relative_x = (relative_x - (cell.getWidth() / 2)) / (cell.getWidth() / 2);
        return (double)Math.round(relative_x * 10000d) / 10000d;
    }

    private double calcY(DynamicMatrix.MatrixCell cell, float actual_y) {

        double relative_y = actual_y - cell.getBounds().top;
        relative_y = (relative_y - (cell.getHeight() / 2)) / (cell.getHeight() / 2) * -1;
        return (double)Math.round(relative_y * 10000d) / 10000d;
    }

    private double calcX(View roundButton, MotionEvent event) {
        double x = (event.getX() - (roundButton.getWidth() / 2)) / (roundButton.getWidth() / 2);
        x = (double)Math.round(x * 10000d) / 10000d;
        return x;
    }

    private double calcY(View roundButton, MotionEvent event) {
        double y = (event.getY() - (roundButton.getHeight() / 2)) / (roundButton.getHeight() /2) * -1;
        y = (double)Math.round(y * 10000d) / 10000d;
        return y;
    }

    private String buildMessage(String operation, double x, double y) {
        return (operation + "," + String.valueOf(x) + "," + String.valueOf(y) + "\n");
    }
    private String autoMessage(String operation) {
        return (operation + "," + "0" + "," + "0" + "\n");
    }

    public void send(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "cant send message - not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private void disconnect() {
        if (mChatService != null) {
            mChatService.stop();
        };

        finish();
    }

    private void msg(String message) {
        TextView statusView = (TextView)findViewById(R.id.status);
        statusView.setText(message);
    }

    private void parseData(String data) {
        //msg(data);

        // add the message to the buffer
        mInStringBuffer.append(data);

        // debug - log data and buffer
        //Log.d("data", data);
        //Log.d("mInStringBuffer", mInStringBuffer.toString());
        //msg(data.toString());

        // find any complete messages
        String[] messages = mInStringBuffer.toString().split("\\n");
        int noOfMessages = messages.length;
        // does the last message end in a \n, if not its incomplete and should be ignored
        if (!mInStringBuffer.toString().endsWith("\n")) {
            noOfMessages = noOfMessages - 1;
        }

        // clean the data buffer of any processed messages
        if (mInStringBuffer.lastIndexOf("\n") > -1)
            mInStringBuffer.delete(0, mInStringBuffer.lastIndexOf("\n") + 1);

        // process messages
        for (int messageNo = 0; messageNo < noOfMessages; messageNo++) {
            processMessage(messages[messageNo]);
        }

    }

    private void processMessage(String message) {
        // Debug
        // msg(message);
        String parameters[] = message.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        boolean invalidMessage = false;

        // Check the message
        if (parameters.length > 0) {
            // check length
            if (parameters.length == 5) {

                // set matrix
                if (parameters[0].equals("4")) {
                    if (!parameters[1].equals("")) {
                        try {
                            // convert color from #rrggbbaa to #aarrggbb
                            String color =
                                parameters[1].substring(0,1) +
                                parameters[1].substring(7,9) +
                                parameters[1].substring(1,7);

                            matrix.setColor(Color.parseColor(color));
                        }
                        catch(Exception i){
                            invalidMessage = true;
                        }
                    }
                    if (!parameters[2].equals(""))
                        matrix.setSquare(parameters[2].equals("1") ? true : false);
                    if (!parameters[3].equals(""))
                        matrix.setBorder(parameters[3].equals("1") ? true : false);
                    if (!parameters[4].equals(""))
                        matrix.setVisible(parameters[4].equals("1") ? true : false);
                    matrix.update();

                }  else {
                    invalidMessage = true;
                }
            } else {
                invalidMessage = true;
            }
        } else {
            invalidMessage = true;
        }

        if (invalidMessage) {
            msg("Error - Invalid message received '" + message +"'");
        }
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.d("status","connected");
                            msg("Connected to " + deviceName);
                            matrix.setVisibility(View.VISIBLE);
                            // send the protocol version to the server
                            send("3," + Constants.PROTOCOL_VERSION + "," + Constants.CLIENT_NAME + "\n");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.d("status","connecting");
                            msg("Connecting to " + deviceName);
                            matrix.setVisibility(View.INVISIBLE);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Log.d("status","not connected");
                            msg("Not connected");
                            disconnect();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readData = new String(readBuf, 0, msg.arg1);
                    // message received
                    parseData(readData);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }
    };

    @Override
    public void onBackPressed() {
        disconnect();
    }
}

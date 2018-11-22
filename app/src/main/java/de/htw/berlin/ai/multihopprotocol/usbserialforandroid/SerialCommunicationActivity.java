package de.htw.berlin.ai.multihopprotocol.usbserialforandroid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.berlin.htw.de.seriallibrary.driver.UsbSerialPort;
import ai.berlin.htw.de.seriallibrary.util.HexDump;
import ai.berlin.htw.de.seriallibrary.util.SerialInputOutputManager;
import de.htw.berlin.ai.multihopprotocol.R;
import timber.log.Timber;

public class SerialCommunicationActivity extends AppCompatActivity {

    private static final byte[] LINE_FEED = {'\r', '\n'};

    private static UsbSerialPort sPort = null;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private TextView tvConsoleText;
    private TextView tvUsbStatus;
    private EditText etCommand;
    private ScrollView scrollView;
    private Button btnSend;

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onRunError(Exception e) {
            Timber.d("Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            SerialCommunicationActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateReceivedData(data);
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_communication);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scrollView = findViewById(R.id.demoScroller);
        tvConsoleText = findViewById(R.id.consoleText);
        tvUsbStatus = findViewById(R.id.tv_usb_status);
        etCommand = findViewById(R.id.et_console_input);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            if (!etCommand.getText().toString().equals("")) {
                String data = etCommand.getText().toString().trim();

                try {
                    mSerialIoManager.writeSync(data.getBytes());
                    mSerialIoManager.writeSync(LINE_FEED);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        finish();
    }

    private void showStatus(TextView theTextView, String theLabel, boolean theValue) {
        String msg = theLabel + ": " + (theValue ? "enabled" : "disabled") + "\n";
        theTextView.append(msg);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        //  TODO refactor permission request
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        ((UsbManager) getSystemService(Context.USB_SERVICE)).requestPermission(sPort.getDriver().getDevice(), mPendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("Resumed, port=%s", sPort);
        if (sPort == null) {
            tvUsbStatus.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            requestUserPermission();
            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                tvUsbStatus.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                showStatus(tvConsoleText, "CD  - Carrier Detect", sPort.getCD());
                showStatus(tvConsoleText, "CTS - Clear To Send", sPort.getCTS());
                showStatus(tvConsoleText, "DSR - Data Set Ready", sPort.getDSR());
                showStatus(tvConsoleText, "DTR - Data Terminal Ready", sPort.getDTR());
                showStatus(tvConsoleText, "DSR - Data Set Ready", sPort.getDSR());
                showStatus(tvConsoleText, "RI  - Ring Indicator", sPort.getRI());
                showStatus(tvConsoleText, "RTS - Request To Send", sPort.getRTS());

            } catch (IOException e) {
                Timber.e(e, "Error setting up device: %s", e.getMessage());
                tvUsbStatus.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            tvUsbStatus.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Timber.i("Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Timber.i("Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        tvConsoleText.append(new String(data));
        scrollView.smoothScrollTo(0, tvConsoleText.getBottom());
    }

    /**
     * Starts the activity, using the supplied driver instance.
     *
     * @param context
     */
    static void show(Context context, UsbSerialPort port) {
        sPort = port;
        final Intent intent = new Intent(context, SerialCommunicationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

}

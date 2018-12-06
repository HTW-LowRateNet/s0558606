package de.htw.berlin.ai.multihopprotocol.usbserialforandroid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import ai.berlin.htw.de.seriallibrary.driver.UsbSerialPort;
import de.htw.berlin.ai.multihopprotocol.R;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.LoraTransceiver;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.SerialMessageListener;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.WriteSerialRunnable;
import timber.log.Timber;

public class SerialCommunicationActivity extends AppCompatActivity {

    private static UsbSerialPort sPort = null;

    private TextView tvConsoleText;
    private TextView tvUsbStatus;
    private EditText etCommand;
    private ScrollView scrollView;
    private Button btnSend;

    private LoraTransceiver loraTransceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_communication);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loraTransceiver = new LoraTransceiver(sPort, (UsbManager) getSystemService(Context.USB_SERVICE));
        loraTransceiver.addListener((SerialMessageListener) this::updateReceivedData);

        loraTransceiver.getConnectionStatus().observe(this, connectionStatus -> {
            if (connectionStatus != null) {
                tvUsbStatus.setText(connectionStatus.toString());
            }
        });

        requestUserPermission();

        scrollView = findViewById(R.id.demoScroller);
        tvConsoleText = findViewById(R.id.consoleText);
        tvUsbStatus = findViewById(R.id.tv_usb_status);
        etCommand = findViewById(R.id.et_message_input);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            if (!etCommand.getText().toString().equals("")) {
                String data = etCommand.getText().toString().trim();

                loraTransceiver.writeSerial(data, new WriteSerialRunnable.Callback() {
                    @Override
                    public void onSerialWriteSuccess() {
                        updateReceivedData("Serial write successful");
                    }

                    @Override
                    public void onSerialWriteFailure() {
                        updateReceivedData("Serial write unsuccessful");
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("Resumed, port=%s", sPort);
        onDeviceStateChange();

    }

    @Override
    protected void onPause() {
        super.onPause();
        loraTransceiver.stop();
    }

    private void requestUserPermission() {
        //  TODO refactor permission request
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        ((UsbManager) getSystemService(Context.USB_SERVICE)).requestPermission(sPort.getDriver().getDevice(), mPendingIntent);
    }


    private void onDeviceStateChange() {
        loraTransceiver.stop();
        loraTransceiver.start();
    }

    private void updateReceivedData(String data) {
        runOnUiThread(() -> {
            tvConsoleText.append(data);
            scrollView.smoothScrollTo(0, tvConsoleText.getBottom());

        });
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

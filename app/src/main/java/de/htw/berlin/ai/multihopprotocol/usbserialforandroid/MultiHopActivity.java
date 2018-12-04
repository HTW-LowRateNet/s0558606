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

import java.util.ArrayList;
import java.util.List;

import ai.berlin.htw.de.seriallibrary.driver.UsbSerialDriver;
import ai.berlin.htw.de.seriallibrary.driver.UsbSerialPort;
import ai.berlin.htw.de.seriallibrary.driver.UsbSerialProber;
import de.htw.berlin.ai.multihopprotocol.R;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.LoraTransceiver;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.TransceiverDevice;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.MultihopProtocol;
import timber.log.Timber;

public class MultiHopActivity extends AppCompatActivity {

    private static UsbSerialPort sPort = null;

    private TextView tvConsoleText;
    private TextView tvUsbStatus;
    private EditText etCommand;
    private ScrollView scrollView;
    private Button btnSend;

    private MultihopProtocol multihopProtocol;
    private TransceiverDevice transceiverDevice;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_hop);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sPort = findSerialPort();

        transceiverDevice = new LoraTransceiver(sPort, (UsbManager) getSystemService(Context.USB_SERVICE));
        multihopProtocol = new MultihopProtocol(transceiverDevice);

        transceiverDevice.addListener(this::updateReceivedData);

        transceiverDevice.getConnectionStatus().observe(this, connectionStatus -> {
            if (connectionStatus != null) {
                tvUsbStatus.setText(connectionStatus.toString());
            }
        });

        requestUserPermission();

        scrollView = findViewById(R.id.demoScroller);
        tvConsoleText = findViewById(R.id.consoleText);
        tvUsbStatus = findViewById(R.id.tv_usb_status);
        etCommand = findViewById(R.id.et_console_input);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            if (!etCommand.getText().toString().equals("")) {
                String data = etCommand.getText().toString().trim();

                transceiverDevice.send(data);
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
        transceiverDevice.stop();
    }

    private void requestUserPermission() {
        //  TODO refactor permission request
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        ((UsbManager) getSystemService(Context.USB_SERVICE)).requestPermission(sPort.getDriver().getDevice(), mPendingIntent);
    }


    private void onDeviceStateChange() {
        transceiverDevice.stop();
        transceiverDevice.start();
    }

    private void updateReceivedData(String data) {
        runOnUiThread(() -> {
            tvConsoleText.append(data);
            scrollView.smoothScrollTo(0, tvConsoleText.getBottom());

        });
    }

    private UsbSerialPort findSerialPort() {
        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(((UsbManager) getSystemService(Context.USB_SERVICE)));

        final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            Timber.d(String.format("+ %s: %s port%s", driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
            result.addAll(ports);
        }

        return result.get(0);
    }

}

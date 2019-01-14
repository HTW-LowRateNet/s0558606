package de.htw.berlin.ai.multihopprotocol.usbserialforandroid;

import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    private TextView tvUsbStatus, tvProtocolStatus;
    private EditText etMessage;
    private ScrollView scrollView;
    private Button btnSend, btnConnect;

    private MultihopProtocol multihopProtocol;
    private TransceiverDevice transceiverDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_hop);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Timber.plant(new Timber.DebugTree());

        sPort = findSerialPort();

        if (sPort == null)
            return;

        transceiverDevice = new LoraTransceiver(sPort, (UsbManager) getSystemService(Context.USB_SERVICE));
        multihopProtocol = new MultihopProtocol(transceiverDevice);

        transceiverDevice.addListener(this::updateReceivedData);

        transceiverDevice.getConnectionStatus().observe(this, connectionStatus -> {
            if (connectionStatus != null) {
                tvUsbStatus.setText(connectionStatus.toString());
            }
        });

        multihopProtocol.getProtocolState().observe(this, new Observer<MultihopProtocol.ProtocolState>() {
            @Override
            public void onChanged(@Nullable MultihopProtocol.ProtocolState protocolState) {
                tvProtocolStatus.setText(protocolState.toString());
            }
        });

        requestUserPermission();

        scrollView = findViewById(R.id.demoScroller);
        tvConsoleText = findViewById(R.id.consoleText);
        tvUsbStatus = findViewById(R.id.tv_usb_status);
        tvProtocolStatus = findViewById(R.id.tv_protocol_status);
        etMessage = findViewById(R.id.et_message_input);
        btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(v -> startProtocol());
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            if (!etMessage.getText().toString().equals("")) {
                String data = etMessage.getText().toString().trim();

                multihopProtocol.sendTextMessage(data);
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

    private void startProtocol() {
        stopProtocol();

        multihopProtocol.start();
    }

    private void stopProtocol() {
        multihopProtocol.stop();
    }

    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        ((UsbManager) getSystemService(Context.USB_SERVICE)).requestPermission(sPort.getDriver().getDevice(), mPendingIntent);
    }

    private void onDeviceStateChange() {
        if (transceiverDevice != null) {
            transceiverDevice.stop();
            transceiverDevice.start();
        }
    }

    private void updateReceivedData(String data) {
        runOnUiThread(() -> {
            tvConsoleText.append(data);
            tvConsoleText.append("\r\n");
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

        if (result.size() > 0)
            return result.get(0);
        else
            return null;
    }

}

package edu.gatech.muc.jacquard;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Formatter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String MAC_ADDRESS = "E9:17:5F:F5:99:89";
    private static final String SERVICE_UUID = "d45c2000-4270-a125-a25d-ee458c085001";
    private static final String GESTURE_CHAR_UUID = "d45c2030-4270-a125-a25d-ee458c085001";

    private static final int SCAN_PERIOD = 10000;

    private boolean scanning = false;
    private Handler handler;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private Snackbar snackbar;
    private Button connectButton;

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i("Bluetooth", "Found device with address " + result.getDevice().getAddress() + " and name " + result.getDevice().getName());
            if (result.getDevice().getAddress().equals(MAC_ADDRESS)) {
                foundDevice(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("Bluetooth", "LE Scan Failed (error code " + errorCode + ")");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            pairWithJacket();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    456);
        }

        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setupBluetooth()) {
                    pairWithJacket();
                }
            }
        });
    }

    private boolean setupBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not Available", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 123);
            return false;
        }
        return true;
    }

    private void foundDevice(BluetoothDevice device) {
        scanning = false;
        bluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
        handler.post(new Runnable() {
            @Override
            public void run() {
                snackbar.setText("Connecting...");
            }
        });
        device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BluetoothGattCharacteristic charac = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(GESTURE_CHAR_UUID));
                gatt.setCharacteristicNotification(charac, true);
                BluetoothGattDescriptor descriptor = charac.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Finished Discovering Services", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                if (characteristic.getUuid().toString().equals(GESTURE_CHAR_UUID)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            byte[] bytes = characteristic.getValue();
                            Formatter formatter = new Formatter();
                            for (byte b : bytes) {
                                formatter.format("%02x", b);
                            }
                            String hex = "0x" + formatter.toString();
                            Toast.makeText(getBaseContext(), "Gesture: " + hex, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    if (snackbar != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                snackbar.setText("Disconnected");
                                snackbar.setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                        connectButton.setEnabled(true);
                                    }
                                });
                            }
                        });
                    }
                } else if (newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            snackbar.setText("Connected to " + gatt.getDevice().getName());
                            snackbar.setAction("Disconnect", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    gatt.disconnect();
                                    gatt.close();
                                    connectButton.setEnabled(true);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void onDisconnect() {

    }

    private void pairWithJacket() {
        connectButton.setEnabled(false);
        snackbar = Snackbar.make(findViewById(android.R.id.content), "Searching...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scanning) {
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
                    scanning = false;
                    snackbar.setText("Unable to find device");
                    snackbar.setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                            connectButton.setEnabled(true);
                        }
                    });
                }
            }
        }, SCAN_PERIOD);
        scanning = true;
        bluetoothAdapter.getBluetoothLeScanner().startScan(leScanCallback);
    }
}

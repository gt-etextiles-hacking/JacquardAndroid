package edu.gatech.muc.jacquard.lib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JacquardJacket {
    private static final String SERVICE_UUID = "D45C2000-4270-A125-A25D-EE458C085001";
    private static final String GESTURE_CHAR_UUID = "D45C2030-4270-A125-A25D-EE458C085001";
    private static final String THREAD_CHAR_UUID = "D45C2010-4270-A125-A25D-EE458C085001";
    private static final String NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private String macAddress;
    private BluetoothAdapter bluetoothAdapter;
    private JacquardStatus status;
    private JacketStatusUpdateListener statusUpdateListener;

    private SuccessCallback scanSuccessCallback;
    private Handler scanHandler;
    private Context context;
    private ScanCallback scanResultCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getDevice().getAddress().equals(macAddress)) {
                foundDevice(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            scanHandler.removeCallbacksAndMessages(null);
            scanHandler = null;
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanResultCallback);
            if (scanSuccessCallback != null) {
                scanSuccessCallback.onResult(false);
            }
            updateStatus(JacquardStatus.DISCONNECTED);
        }
    };

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic glowCharacteristic;
    private JacketActionListener actionListener;
    private List<CustomGestureRecognizer> gestureRecognizers;

    /**
     * Instantiates a new Jacquard jacket object to connect to
     * @param macAddress The MAC address of the bluetooth adapter connected to the jacket
     * @param adapter The bluetooth adapter to use to establish a bluetooth connection
     */
    public JacquardJacket(@NonNull String macAddress, @NonNull BluetoothAdapter adapter) {
        if (macAddress.trim().equals("")) {
            throw new IllegalArgumentException("MAC address cannot be empty");
        }

        this.macAddress = macAddress;
        this.bluetoothAdapter = adapter;
        this.status = JacquardStatus.DISCONNECTED;
        this.gestureRecognizers = new ArrayList<>();
    }

    /**
     * Sets the update listener for this jacket. The update listener will be called with any state changes
     * @param listener The new update listener
     */
    public void setJacketStatusUpdateListener(@Nullable JacketStatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }

    /**
     * Sets the action listener for this jacket. The action listener will be called with any user actions on the jacket
     * @param listener The new action listener
     */
    public void setJacketActionListener(@Nullable JacketActionListener listener) {
        this.actionListener = listener;
    }

    /**
     * Returns all gesture recognizers associated with this jacket
     * @return The list of gesture recognizers
     */
    public List<CustomGestureRecognizer> getGestureRecognizers() {
        return this.gestureRecognizers;
    }

    /**
     * Adds a gesture recognizer to this jacket
     * @param recognizer The new gesture recognizer
     */
    public void addGestureRecognizer(@NonNull CustomGestureRecognizer recognizer) {
        this.gestureRecognizers.add(recognizer);
    }

    /**
     * Removes a gesture recognizer from this jacket
     * @param recognizer The gesture recognizer to be removed
     */
    public void removeGestureRecognizer(@NonNull CustomGestureRecognizer recognizer) {
        this.gestureRecognizers.remove(recognizer);
    }

    /**
     * Returns the current connection status of the jacket
     * @return The current connection status
     */
    public JacquardStatus getStatus() {
        return status;
    }

    /**
     * Returns whether or not the system is currently searching for a jacket blueooth connection
     * @return If the jacket is still searching/pairing
     */
    public boolean isSearching() {
        return this.scanHandler != null;
    }

    /**
     * Search for the jacket and pair with the jacket's bluetooth connection
     * @param ctx The context provided for the app
     * @param callback The callback to determine if pairing was successful
     */
    public void searchAndPair(@NonNull Context ctx, @Nullable SuccessCallback callback) {
        this.searchAndPair(10000, ctx, callback);
    }

    /**
     * Search for the jacket and pair with the jacket's bluetooth connection
     * @param scanPeriod The amount of time (in milliseconds) to search for the jacket before timing out
     * @param ctx The context provided for the app
     * @param callback The callback to determine if pairing was successful
     */
    public void searchAndPair(int scanPeriod, @NonNull Context ctx, @Nullable SuccessCallback callback) {
        if (this.isSearching()) {
            return;
        }
        if (this.status != JacquardStatus.DISCONNECTED) {
            throw new IllegalStateException("Cannot search for jackets while already connected to a jacket");
        }
        this.context = ctx;
        this.scanSuccessCallback = callback;
        scanHandler = new Handler();
        scanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isSearching()) {
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(scanResultCallback);
                    scanHandler = null;
                    if (scanSuccessCallback != null) {
                        scanSuccessCallback.onResult(false);
                    }
                    updateStatus(JacquardStatus.DISCONNECTED);
                }
            }
        }, scanPeriod);
        this.bluetoothAdapter.getBluetoothLeScanner().startScan(this.scanResultCallback);
        this.updateStatus(JacquardStatus.SEARCHING);
    }

    /**
     * Disconnect from the jacket's bluetooth service
     */
    public void disconnect() {
        if (status != JacquardStatus.READY) {
            return;
        }

        bluetoothGatt.disconnect();
        this.updateStatus(JacquardStatus.DISCONNECTED);
    }

    public void glow(String data) {
        if (status != JacquardStatus.READY) {
            throw new IllegalStateException("Cannot process command for jacket while not connected");
        }
        if (glowCharacteristic == null) {
            throw new IllegalStateException("Unable to find glow bluetooth characteristic");
        }
        glowCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        glowCharacteristic.setValue(JacketGlowHelper.convertHexStringToByteArray(data));
    }

    /**
     * Pair with a device that has been found through a bluetooth scan
     * @param device The device that was found
     */
    private void foundDevice(BluetoothDevice device) {
        if (status != JacquardStatus.SEARCHING) {
            return;
        }
        scanHandler.removeCallbacksAndMessages(null);
        scanHandler = null;
        this.bluetoothAdapter.getBluetoothLeScanner().stopScan(scanResultCallback);
        this.updateStatus(JacquardStatus.CONNECTING);
        bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BluetoothGattCharacteristic gestureCharac = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(GESTURE_CHAR_UUID));
                listenForCharacteristicChanges(gatt, gestureCharac);
                BluetoothGattCharacteristic threadCharac = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(THREAD_CHAR_UUID));
                listenForCharacteristicChanges(gatt, threadCharac);
                for (BluetoothGattService service : gatt.getServices()) {
                    for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                        if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                            glowCharacteristic = c;
                            break;
                        }
                    }
                }
                updateStatus(JacquardStatus.READY);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.i("CHARAC_UPDATE", "UUID=" + characteristic.getUuid().toString());
                if (characteristic.getUuid().toString().toUpperCase().equals(GESTURE_CHAR_UUID)) {
                    byte[] bytes = characteristic.getValue();
                    int value = bytes[0];
                    JacquardGesture gesture = JacquardGesture.findByID(value);
                    if (actionListener != null) {
                        actionListener.onGesturePerformed(gesture);
                    }
                } else if (characteristic.getUuid().toString().toUpperCase().equals(THREAD_CHAR_UUID)) {
                    byte[] bytes = characteristic.getValue();

                }
            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    updateStatus(JacquardStatus.DISCONNECTED);
                } else if (newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }
        });
    }

    /**
     * Register a listener for changes to the given characteristic
     * @param gatt The GATT object that contains the characteristic
     * @param characteristic The characteristic to listen for
     */
    private void listenForCharacteristicChanges(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(NOTIFICATION_DESCRIPTOR_UUID));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    /**
     * Change the status of the jacket and call the relevant status update listener
     * @param status The new jacket status
     */
    private void updateStatus(JacquardStatus status) {
        this.status = status;
        if (this.statusUpdateListener != null) {
            this.statusUpdateListener.onStatusChange(status);
        }
        if (this.status == JacquardStatus.READY) {
            this.glow(JacketGlowHelper.RAINBOW_1);
            this.glow(JacketGlowHelper.RAINBOW_2);
        }
    }
}

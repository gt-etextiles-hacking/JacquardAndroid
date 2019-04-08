package edu.gatech.muc.jacquard.demo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import edu.gatech.muc.jacquard.R;
import edu.gatech.muc.jacquard.lib.JacketActionListener;
import edu.gatech.muc.jacquard.lib.JacquardStatus;
import edu.gatech.muc.jacquard.lib.JacketStatusUpdateListener;
import edu.gatech.muc.jacquard.lib.JacquardGesture;
import edu.gatech.muc.jacquard.lib.JacquardJacket;
import edu.gatech.muc.jacquard.lib.SuccessCallback;

public class MainActivity extends AppCompatActivity implements JacketActionListener, JacketStatusUpdateListener {

    private static final String MAC_ADDRESS = "E9:17:5F:F5:99:89";

    private Handler handler;

    private BluetoothAdapter bluetoothAdapter;
    private Snackbar snackbar;
    private Button connectButton;
    private Button viewThreadButton;

    private JacquardJacket jacket;

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
        viewThreadButton = findViewById(R.id.viewThreadButton);
        viewThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setupBluetooth()) {
                    
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

    @Override
    public void onGesturePerformed(@NonNull final JacquardGesture gesture) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "Gesture: " + gesture.name(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onThreadPressed() {

    }

    @Override
    public void onStatusChange(JacquardStatus newStatus) {
        switch (newStatus) {
            case DISCONNECTED:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(true);
                        if (snackbar != null) {
                            snackbar.setText("Disconnected");
                            snackbar.setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                        }
                    }
                });
                break;
            case READY:
                if (snackbar != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            snackbar.setText("Connected to Jacket");
                            snackbar.setAction("Disconnect", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    jacket.disconnect();
                                }
                            });
                        }
                    });
                }
                break;
            case CONNECTING:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (snackbar != null) {
                            snackbar.setText("Connecting...");
                        }
                    }
                });
                break;
        }
    }

    private void pairWithJacket() {
        connectButton.setEnabled(false);
        snackbar = Snackbar.make(findViewById(android.R.id.content), "Searching...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        handler = new Handler();
        jacket = new JacquardJacket(MAC_ADDRESS, bluetoothAdapter);
        jacket.setJacketStatusUpdateListener(this);
        jacket.setJacketActionListener(this);
        jacket.searchAndPair(this, new SuccessCallback() {
            @Override
            public void onResult(boolean success) {
                if (!success) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Unable to find jacket", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}

package com.PiProject.Music_App;


import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.PiProject.Music_App.adapter.DeviceListAdapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class DeviceListActivity extends Activity {
    protected static final String TAG = "TAG";
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private BluetoothService mBluetoothService = null;
    private BluetoothAdapter bluetooth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_paired_devices);

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            mDeviceList = extras.getParcelableArrayList("device.list");
            mAdapter = new DeviceListAdapter(this);
            mAdapter.setData(mDeviceList);
            mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
                @Override
                public void onPairButtonClick(int position) {
                    BluetoothDevice device = mDeviceList.get(position);

                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        unpairDevice(device);
                    } else {
                        showToast("Pairing...");

                        pairDevice(device);
                    }
                }
            });

            mListView = (ListView) findViewById(R.id.lv_paired);



            mAdapter.setListener(new DeviceListAdapter.OnConnectButtonClickListener() {
                @Override
                public void onConnectButtonClick(int position) {
                    BluetoothDevice device = mDeviceList.get(position);

                    mBluetoothService.connect(device);
                }
            });


            mListView.setAdapter(mAdapter);
        }

        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        //TODO ervoor zorgen dat adequaat terug gegaan kan worden naar het vorige scherm
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //Pair and Unpair methods
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //BroadcastReceiver for pairing
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast("Unpaired");
                }

                mAdapter.notifyDataSetChanged();
            }
        }
    };

    //The BluetoothService for connecting
    public class BluetoothService {
        //unique UUID for this application
        private final UUID PI_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
        private ConnectThread mConnectThread;
        //private ConnectedThread mConnectedThread;
        private BluetoothAdapter bluetooth;

        public synchronized void connect(BluetoothDevice device) {
            //start ConnectThread to connect to given device
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        }
        private class ConnectThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final BluetoothDevice mmDevice;

            public ConnectThread(BluetoothDevice device) {
                // Use a temporary object that is later assigned to mmSocket,
                // because mmSocket is final
                BluetoothSocket tmp = null;
                mmDevice = device;

                // Get a BluetoothSocket to connect with the given BluetoothDevice
                try {
                    tmp = device.createRfcommSocketToServiceRecord(PI_UUID);
                } catch (IOException e) { }
                mmSocket = tmp;
            }

            public void run() {
                // Cancel discovery because it will slow down the connection
                bluetooth.cancelDiscovery();

                try {
                    // Connect the device through the socket. This will block
                    // until it succeeds or throws an exception
                    mmSocket.connect();
                } catch (IOException connectException) {
                    Log.d(TAG, "CouldNotConnectToSocket", connectException);
                    // Unable to connect; close the socket and get out
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) { }
                    return;
                }

                //Showing toast for connection status
                if (mmSocket.isConnected()){
                    showToast("Connected");
                }
                else {
                    showToast("Could not Connect");
                }

                // Do work to manage the connection (in a separate thread)
                //TODO referentie naar de manage thread aanmaken
                //MainActivity.manageConnectedSocket(mmSocket);
            }

            /** Will cancel an in-progress connection, and close the socket */
            public  void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) { }
            }
        }
    }
}

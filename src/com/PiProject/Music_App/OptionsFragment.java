package com.PiProject.Music_App;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class OptionsFragment extends Fragment {

    public OptionsFragment() {}

    Callback mCallback;

    Button onOffButton,deviceButton, searchButton;
    TextView bluetoothStatus, btConnected, btConDevice;

    private BluetoothAdapter bluetooth;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View optionsRootView = inflater.inflate(R.layout.fragment_options, container, false);

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        onOffButton = (Button)optionsRootView.findViewById(R.id.buttonOnOff);
        onOffButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onOffClick();
            }
        });
        deviceButton = (Button)optionsRootView.findViewById(R.id.buttonDevices);
        deviceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.listPaired();
            }
        });
        searchButton = (Button)optionsRootView.findViewById(R.id.buttonSearch);
        bluetoothStatus = (TextView)optionsRootView.findViewById(R.id.bluetoothStatus);
        btConnected = (TextView)optionsRootView.findViewById(R.id.connectedTitle);
        btConDevice = (TextView) optionsRootView.findViewById(R.id.connectedDevice);

        if (bluetooth.isEnabled()) {
            onState();
        }
        else if (!bluetooth.isEnabled()){
            offState();
        }
        else {
            noState();
        }

        return optionsRootView;
    }

    public void onState() {
        onOffButton.setText("Turn OFF");

        bluetoothStatus.setText("Bluetooth is ON");
        bluetoothStatus.setTextColor(Color.GREEN);

        deviceButton.setEnabled(true);
        searchButton.setEnabled(true);
    }
    public void offState() {
        onOffButton.setText("Turn ON");

        bluetoothStatus.setText("Bluetooth is OFF");
        bluetoothStatus.setTextColor(Color.RED);

        deviceButton.setEnabled(false);
        searchButton.setEnabled(false);
    }
    public void noState() {
        onOffButton.setText("N/A");

        bluetoothStatus.setText("No Bluetooth");
        bluetoothStatus.setTextColor(Color.RED);

        onOffButton.setEnabled(false);
        deviceButton.setEnabled(false);
        searchButton.setEnabled(false);
    }
    public void connected(String device) {
        btConDevice.setText(device);
    }
    public void disconnected() {
        btConDevice.setText("");
    }

}


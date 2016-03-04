package com.PiProject.Music_App;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class OptionsFragment extends Fragment {

    public OptionsFragment() {}

    Button onOffButton,deviceButton, searchButton;
    TextView BluetoothListTitle, bluetoothStatus;
    ListView mBluetoothList;

    private BluetoothAdapter bluetooth;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View optionsRootView = inflater.inflate(R.layout.fragment_options, container, false);

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        onOffButton = (Button)optionsRootView.findViewById(R.id.buttonOnOff);
        deviceButton = (Button)optionsRootView.findViewById(R.id.buttonDevices);
        searchButton = (Button)optionsRootView.findViewById(R.id.buttonSearch);
        bluetoothStatus = (TextView)optionsRootView.findViewById(R.id.bluetoothStatus);
        BluetoothListTitle = (TextView)optionsRootView.findViewById(R.id.listTitle);
        mBluetoothList = (ListView)optionsRootView.findViewById(R.id.bluetoothList);

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

}


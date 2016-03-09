package com.PiProject.Music_App;

import android.app.*;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.PiProject.Music_App.adapter.DeviceListAdapter;
import com.PiProject.Music_App.adapter.NavDrawerListAdapter;
import com.PiProject.Music_App.model.NavDrawerItem;

import java.lang.String;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String local_frag_tag="";

    // Bluetooth buttons and stuff
    private final static int REQUEST_ENABLE_BT = 1;
    private final UUID PI_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");

    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter bluetooth;
    private DeviceListAdapter btListAdapter;
    private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
    private ProgressDialog mProgressDlg;

    TextView bluetoothStatus, btConnected;

    // Music List
    private ListView musicListView;
    private ListAdapter musicListAdapter;
    //TODO array list en muziek adapter doen
    private ArrayList musicPiList = new ArrayList();

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTitle = mDrawerTitle = getTitle();

        // Bluetooth part
        // TODO checken welke elementen behouden kunnen worden en welke niet a.d.h.v. de aan/uit dingen van OptionsFragment

        bluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        btConnected = (TextView)findViewById(R.id.connectedTitle);
        bluetooth = BluetoothAdapter.getDefaultAdapter();


        mProgressDlg = new ProgressDialog(this);
        btListAdapter = new DeviceListAdapter(this);

        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                bluetooth.cancelDiscovery();
            }
        });

        btListAdapter.setData(btDeviceList);
        btListAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position) {
                BluetoothDevice device = btDeviceList.get(position);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device);
                } else {
                    showToast("Pairing...");

                    pairDevice(device);
                }
            }
        });

        IntentFilter btSearchFilter = new IntentFilter();

        btSearchFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btSearchFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btSearchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        btSearchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, btSearchFilter);

        // Music List part
        musicListView = (ListView) findViewById(R.id.music_list);
        musicListAdapter.setData(musicPiList);

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // TODO eventueel items toevoegen
        // Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Music
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));

        // Settings - Bluetooth
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));


        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                // TODO icoon voor het menu toevoegen
                R.drawable.ic_placeholder, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
            local_frag_tag="Home";
        }

    }

    @Override
    public void onPause() {
        if (bluetooth != null) {
            if (bluetooth.isDiscovering()) {
                bluetooth.cancelDiscovery();
            }
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    /**
     * Standard Toast constructor
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Slide menu item click listener
    **/
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
    **/
    private void displayView(int position) {
        // update the menu content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                local_frag_tag = "Home";
                break;
            // TODO uncomment cases wanneer de referentie ervoor aangemaakt is(ook de refentie hernoemen indien nodig)
            case 1:
                fragment = new MusicListFragment();
                local_frag_tag = "MusicList";
                break;
            case 2:
                fragment = new OptionsFragment();
                local_frag_tag = "Options";
                break;
            /*
            case 3:
                fragment = new PagesFragment();
                break;
            case 4:
                fragment = new WhatsHotFragment();
                break;
            */
            default:
                break;
        }


        //Fragment constructor with tags created by the switch
        if (fragment != null) {
            FragmentManager fragManager= getFragmentManager();
            FragmentTransaction fragTransaction =fragManager.beginTransaction();
            if (local_frag_tag == "Home") {
                fragTransaction.replace(R.id.frame_container, fragment, "frag_home");
                fragTransaction.commit();
            }
            else if (local_frag_tag == "MusicList") {
                fragTransaction.replace(R.id.frame_container, fragment, "frag_mlist");
                fragTransaction.commit();
            }
            else if (local_frag_tag == "Options") {
                fragTransaction.replace(R.id.frame_container, fragment, "frag_options");
                fragTransaction.commit();
            }

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Bluetooth Connectie e.d.
     */
    public void onOff(View v){
        OptionsFragment options_frag = (OptionsFragment)getFragmentManager().findFragmentByTag("frag_options");
        if (!bluetooth.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 1000);
            if (local_frag_tag == "Options") {
                options_frag.onState();
                showToast("Turned On");
            }
            else {
                showToast("No options_frag found");
            }
        }
        else if (bluetooth.isEnabled()) {
            bluetooth.disable();
            if (local_frag_tag == "Options") {
                options_frag.offState();
                showToast("Turned off");
            }
            else {
                showToast("No options_frag found");
            }
        }
        else {
            if (local_frag_tag == "Options") {
                options_frag.noState();
                showToast("Bluetooth not Supported");
            }
            else {
                showToast("No options_frag found");
            }
        }
    }

    public void listPaired(View v){
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            showToast("No Paired Devices Found");
        }
        else {
            ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
            btDeviceList.addAll(pairedDevices);

            Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
            intent.putParcelableArrayListExtra("device.list", btDeviceList);
            startActivity(intent);
        }
    }

    public void bluetoothSearch (View v) {
        if(bluetooth.isEnabled()) {
            bluetooth.startDiscovery();
        }
        else {
            showToast("Bluetooth is not turned on");
        }
    }



    //Functions that perform when pairing
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

    //BroadcastReceiver for Discovery
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                btDeviceList = new ArrayList<BluetoothDevice>();

                mProgressDlg.show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                //Show found Devices
                Intent foundIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                foundIntent.putParcelableArrayListExtra("device.list", btDeviceList);
                startActivity(foundIntent);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDeviceList.add(device);

                showToast("Found device " + device.getName());
            }
        }
    };


    //TODO Managing the Connection


}
package com.PiProject.Music_App;

import android.app.*;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity implements Callback{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String local_frag_tag = "";

    // Bluetooth buttons and stuff
    private final static int REQUEST_ENABLE_BT = 1;
    private static final String BTAG = "BluetoothService";
    private final UUID PI_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");

    Button onOffButton;

    public boolean connected = false;

    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter bluetooth;
    private BluetoothSocket socket;
    private DeviceListAdapter btListAdapter;
    private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
    private ProgressDialog mProgressDlg;
    //TODO uncomment if needed
    static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    private ConnectedThread mConnectedThread;
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

        bluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        onOffButton = (Button) findViewById(R.id.buttonOnOff);
        btConnected = (TextView) findViewById(R.id.connectedTitle);
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
        btSearchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        btSearchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        btSearchFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btSearchFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        btSearchFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(mReceiver, btSearchFilter);

        // Music List part
        musicListView = (ListView) findViewById(R.id.music_list);
        //musicListAdapter.setData(musicPiList);

        /*
        TODO Fix this
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }); */

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
                R.drawable.ic_drawer, //nav menu toggle icon
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
            local_frag_tag = "Home";
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
                Log.i(BTAG, "opening options fragment ");
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
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            if (local_frag_tag == "Home") {
                fragTransaction.replace(R.id.frame_container, fragment, "frag_home");
                fragTransaction.commit();
            } else if (local_frag_tag == "MusicList") {
                fragTransaction.replace(R.id.frame_container, fragment, "frag_mlist");
                fragTransaction.commit();
            } else if (local_frag_tag == "Options") {
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
    @Override
    public void onOffClick() {
        Log.i(BTAG, "onOff ");
        OptionsFragment options_frag = (OptionsFragment) getFragmentManager().findFragmentByTag("frag_options");
        if (!bluetooth.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 1000);
            if (local_frag_tag == "Options") {
                options_frag.onState();
                showToast("Turned On");
            } else {
                showToast("No options_frag found");
            }
        } else if (bluetooth.isEnabled()) {
            bluetooth.disable();
            if (local_frag_tag == "Options") {
                options_frag.offState();
                showToast("Turned off");
            } else {
                showToast("No options_frag found");
            }
        } else {
            if (local_frag_tag == "Options") {
                options_frag.noState();
                showToast("Bluetooth not Supported");
            } else {
                showToast("No options_frag found");
            }
        }
    }

    @Override
    public void listPaired() {
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            showToast("No Paired Devices Found");
        } else {
            ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
            btDeviceList.addAll(pairedDevices);

            Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
            intent.putParcelableArrayListExtra("device.list", btDeviceList);
            startActivity(intent);
        }
    }

    public void bluetoothSearch(View view) {
        if (bluetooth.isEnabled()) {
            bluetooth.startDiscovery();
        } else {
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
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                //Show found Devices
                Intent foundIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                foundIntent.putParcelableArrayListExtra("device.list", btDeviceList);
                startActivity(foundIntent);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDeviceList.add(device);

                showToast("Found device " + device.getName());
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.i(BTAG, "Connected to a device");
                showToast("Connected with " + device.getName());
                //TODO figure out how to reference connectedthread.run from this static method
                Intent BtSIntent = new Intent(MainActivity.this, BtSerial.class);
                connected = true;
                BtSIntent.putExtra("Con", connected);
                startActivity(BtSIntent);
                mConnectedThread.run();
            }
        }
    };

    public class MusicData {
        // TODO variabelen van de nummers bepalen en contrueren
        int id;
        String songName;
        String songArtist;
        String songAlbum;
    }

    //TODO Managing the Connection, ziet developer.android pagina
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final int mBufferLength;
        protected final InputStream mmInStream;
        protected final OutputStream mmOutStream;

        private int bufferlength = 128;
        private byte[] rawbuffer;
        private byte[] buffer;
        private int bufferIndex;
        private int bufferLast;
        private int available;

        private BtSerial mBtSerial;

        public ConnectedThread(BluetoothSocket socket, int bufferlength) {

            mmSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mBufferLength = bufferlength;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            buffer = new byte[mBufferLength]; // buffer store for the stream
            //Log.i(TAG, "started");
        }

        @Override
        public void run() {
            Log.i(BTAG, "ConnectedThread running");

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    //String outputMessage = mmInStream.available() + " bytes available";
                    //Log.i(TAG, outputMessage);
                    // Read from the InputStream
                    while (mmInStream.available() > 0) {

                        synchronized (buffer) {
                            if (bufferLast == buffer.length) {
                                byte temp[] = new byte[bufferLast << 1];
                                System.arraycopy(buffer, 0, temp, 0, bufferLast);
                                buffer = temp;
                            }
                            buffer[bufferLast++] = (byte) mmInStream.read();
                        }
                        btSerialEvent();
                    }
                } catch (IOException e) {
                    Log.e(BTAG, e.getMessage());
                    break;
                }
            }
        }

        public void btSerialEvent() {
            mBtSerial.btSerialEvent();
            Log.i(BTAG, "btSerialEvent called from ConnectedThread");
        }

        /* Call this from the main Activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                for(int i=0; i<bytes.length; i++) {
                    mmOutStream.write(bytes[i] & 0xFF);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Returns the next byte in the buffer as an int (0-255);
         *
         * @return int value of the next byte in the buffer
         */
        public int read() {
            Log.i(BTAG, "reading the buffer");
            if (bufferIndex == bufferLast)
                return -1;

            synchronized (buffer) {
                int outgoing = buffer[bufferIndex++] & 0xff;
                if (bufferIndex == bufferLast) { // rewind
                    bufferIndex = 0;
                    bufferLast = 0;
                }
                return outgoing;
            }
        }

        /**
         * Returns the whole byte buffer.
         *
         * @return
         */
        public byte[] readBytes() {
            Log.i(BTAG, "reading the whole buffer");
            if (bufferIndex == bufferLast)
                return null;

            synchronized (buffer) {
                int length = bufferLast - bufferIndex;
                byte outgoing[] = new byte[length];
                System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

                bufferIndex = 0; // rewind
                bufferLast = 0;
                return outgoing;
            }
        }

        /**
         * Returns the available number of bytes in the buffer, and copies the
         * buffer contents to the passed byte[]
         *
         * @return
         */
        public int readBytes(byte outgoing[]) {
            Log.i(BTAG, "determining the total number of bytes");
            if (bufferIndex == bufferLast)
                return 0;

            synchronized (buffer) {
                int length = bufferLast - bufferIndex;
                if (length > outgoing.length)
                    length = outgoing.length;
                System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

                bufferIndex += length;
                if (bufferIndex == bufferLast) {
                    bufferIndex = 0; // rewind
                    bufferLast = 0;
                }
                return length;
            }
        }

        /**
         * Returns a byte buffer until the byte interesting. If the byte interesting
         * doesn't exist in the current buffer, null is returned.
         *
         * @param interesting
         * @return
         */
        public byte[] readBytesUntil(int interesting) {
            Log.i(BTAG, "reading until interesting byte");
            if (bufferIndex == bufferLast)
                return null;
            byte what = (byte) interesting;

            synchronized (buffer) {
                int found = -1;
                for (int k = bufferIndex; k < bufferLast; k++) {
                    if (buffer[k] == what) {
                        found = k;
                        break;
                    }
                }
                if (found == -1)
                    return null;

                int length = found - bufferIndex + 1;
                byte outgoing[] = new byte[length];
                System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

                bufferIndex += length;
                if (bufferIndex == bufferLast) {
                    bufferIndex = 0; // rewind
                    bufferLast = 0;
                }
                return outgoing;
            }
        }

        public int buffer(int bytes) {
            bufferlength = bytes;

            buffer = new byte[bytes];
            rawbuffer = buffer.clone();

            return bytes;
        }

        /**
         * Returns the last byte in the buffer.
         *
         * @return
         */
        public int last() {
            Log.i(BTAG, "returning last byte in buffer");
            if (bufferIndex == bufferLast)
                return -1;
            synchronized (buffer) {
                int outgoing = buffer[bufferLast - 1];
                bufferIndex = 0;
                bufferLast = 0;
                return outgoing;
            }
        }

        /**
         * Reads a byte from the buffer as char.
         *
         * @return
         */
        public char readChar() {
            Log.i(BTAG, "reading byte as char");
            if (bufferIndex == bufferLast)
                return (char) (-1);
            return (char) last();
        }

        /**
         * Returns the last byte in the buffer as char.
         *
         * @return
         */
        public char lastChar() {
            Log.i(BTAG, "returning last byte as char");
            if (bufferIndex == bufferLast)
                return (char) (-1);
            return (char) last();
        }

        public int available() {
            return (bufferLast - bufferIndex);
        }

        /**
         * Ignore all the bytes read so far and empty the buffer.
         */
        public void clear() {
            Log.i(BTAG, "clearing the buffer");
            bufferLast = 0;
            bufferIndex = 0;
        }

        /* Call this from the main Activity to shutdown the connection */
        public void cancel() {
            Log.i(BTAG, "closing the connection");
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
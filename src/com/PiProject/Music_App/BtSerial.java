package com.PiProject.Music_App;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

public class BtSerial {

    private static final String BtSTAG = "BluetoothService";
    private Context ctx;
    private BluetoothAdapter bluetooth;
    private BluetoothDevice mDevice;

    Method btSerialEventMethod;

    public BtSerial(Context ctx) {
        this.ctx = ctx;

        // reflection to check whether host applet has a call for
        // public void serialEvent(processing.serial.Serial)
        // which would be called each time an event comes in
        try {
            btSerialEventMethod = ctx.getClass().getMethod("btSerialEvent",
                    new Class[] { BtSerial.class });
        } catch (Exception e) {
            // no such method, or an error.. which is fine, just ignore
            //lel
        }
    }

    /*
	 * Callback triggered whenever there is data in the buffer.
	 */

    public void btSerialEvent() {
        if (btSerialEventMethod != null) {
            try {
                btSerialEventMethod.invoke(ctx, new Object[] { this });
                Log.i(BtSTAG, "btSerialEvent called from BtSerial");
            } catch (Exception e) {
                String msg = "error, disabling btSerialEvent() for " + mDevice.getName();
                Log.e(BtSTAG, msg);
                e.printStackTrace();
                btSerialEventMethod = null;
            }
        }
    }

}

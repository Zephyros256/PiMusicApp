package com.PiProject.Music_App.adapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.PiProject.Music_App.R;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnPairButtonClickListener mPListener;
    private OnConnectButtonClickListener mCListener;

    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mPListener = listener;
    }

    public void setListener(OnConnectButtonClickListener listener) {
        mCListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.device_list_item, null);

            holder 				= new ViewHolder();

            holder.nameTv		 = (TextView) convertView.findViewById(R.id.tv_name);
            holder.addressTv 	 = (TextView) convertView.findViewById(R.id.tv_address);
            holder.pairButton    = (Button) convertView.findViewById(R.id.buttonPair);
            holder.connectButton = (Button) convertView.findViewById(R.id.buttonConnect);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device	= mData.get(position);

        holder.nameTv.setText(device.getName());
        holder.addressTv.setText(device.getAddress());
        holder.pairButton.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");
        holder.pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPListener != null) {
                    mPListener.onPairButtonClick(position);
                }
            }
        });
        holder.connectButton.setText("Connect");
        holder.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCListener != null) {
                    mCListener.onConnectButtonClick(position);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
        TextView pairButton;
        TextView connectButton;
    }

    public interface OnPairButtonClickListener {
        public abstract void onPairButtonClick(int position);
    }
    public interface OnConnectButtonClickListener {
        public abstract void onConnectButtonClick(int position);
    }
}

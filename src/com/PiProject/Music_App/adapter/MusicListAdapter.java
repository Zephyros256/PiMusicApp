package com.PiProject.Music_App.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.PiProject.Music_App.R;

public class MusicListAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    //TODO array van nummer, artiest en identifier
    //private List<BluetoothDevice> mData;
    private View.OnClickListener mListener;

    public MusicListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.music_list_item, null);

            holder 				= new ViewHolder();

            holder.nameSong		 = (TextView) convertView.findViewById(R.id.tv_name);
            holder.nameArtist 	 = (TextView) convertView.findViewById(R.id.tv_address);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        /* TODO putting every position in the musiclist array into the holder following this example
        BluetoothDevice device	= mData.get(position);

        holder.nameTv.setText(device.getName());
        holder.addressTv.setText(device.getAddress());
        */

        return convertView;
    }

    static class ViewHolder {
        TextView nameSong;
        TextView nameArtist;
    }

    public interface OnClickListener {
        public abstract void On(int position);
    }
}

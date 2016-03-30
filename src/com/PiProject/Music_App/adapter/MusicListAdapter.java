package com.PiProject.Music_App.adapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.PiProject.Music_App.R;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends BaseAdapter{
    private static final String TAG = MusicListAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private ArrayList<String> Song;
    private ArrayList<String> Album;
    private ArrayList<String> Artist;
    private View.OnClickListener mListener;

    public MusicListAdapter(Context context, ArrayList<String> Song, ArrayList<String> Album, ArrayList<String> Artist) {
        this.Song = Song;
        this.Artist = Artist;
        this.Album = Album;
        mInflater = LayoutInflater.from(context);
    }

    public void setListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return Song.size();
    }

    public Object getItem(int position) {
        return Song.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.music_list_item, null);

            holder 				= new ViewHolder();

            holder.nameSong		 = (TextView) convertView.findViewById(R.id.ml_song);
            holder.nameArtist 	 = (TextView) convertView.findViewById(R.id.ml_artist);
            //TODO veranderen van de naam/ artiest indien van toepassing/ kijken hoe we het willen
            //holder.nameAlbum   = (TextView) convertView.findViewById(R.id.ml_album);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        /* TODO putting every position in the musiclist array into the holder following this example
        BluetoothDevice device	= mData.get(position);

        holder.nameTv.setText(device.getName());
        holder.addressTv.setText(device.getAddress());
        */
        String mSong = Song.get(position);
        Log.d(TAG, "Song: " + mSong);
        String mArtist = Artist.get(position);
        Log.d(TAG, "Artist: " + mArtist);
        String mAlbum = Album.get(position);
        Log.d(TAG, "Album: " + mAlbum);

        holder.nameSong.setText(mSong);
        holder.nameArtist.setText(mArtist);

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

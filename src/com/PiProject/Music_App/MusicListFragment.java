package com.PiProject.Music_App;

import android.app.Fragment;
import android.app.ListFragment;
import android.view.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class MusicListFragment extends ListFragment {

    private ListView mListView;

    public MusicListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);

        mListView = (ListView)  rootView.findViewById(R.id.music_list);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        return rootView;
    }
}

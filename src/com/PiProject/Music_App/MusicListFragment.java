package com.PiProject.Music_App;

import android.app.Fragment;
import android.view.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class MusicListFragment extends Fragment {

    public MusicListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);

        return rootView;
    }
}

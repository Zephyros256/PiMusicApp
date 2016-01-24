package com.PiProject.Music_App;

/** TODO bestand eventueel hernoemen wanneer functie bepaald is **/

import android.view.View;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MusicFragment extends Fragment {

    public MusicFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /** TODO layout referentie updaten indien bestand etc. hernoemd is **/
        View rootView = inflater.inflate(R.layout.fragment_music, container, false);

        return rootView;
    }
}

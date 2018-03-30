package com.ksopha.thanetearth.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ksopha.thanetearth.R;


/**
 * Unused: StartupFragment screen fragment that is shown when app starts
 * Created by Kelvin Sopha on 23/02/18.
 */
public class StartupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_startup, container, false);
    }

}

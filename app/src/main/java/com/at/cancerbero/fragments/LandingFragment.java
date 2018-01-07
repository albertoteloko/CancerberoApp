package com.at.cancerbero.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.at.cancerbero.CancerberoApp.R;

public class LandingFragment extends AppFragment {



    public LandingFragment() {
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        getSupportActionBar().show();

        // Login Controllers


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getMainService().loadUserDetails();
    }
}
package com.at.cancerbero.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.fragments.node.NodesFragment;

public class AboutFragment extends AppFragment {


    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        getSupportActionBar().show();


        Button backButton = (Button) view.findViewById(R.id.aboutBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().onBackPressed();
            }
        });

        return view;
    }

    @Override
    public boolean onBackPressed() {
        changeFragment(NodesFragment.class);
        return true;
    }
}

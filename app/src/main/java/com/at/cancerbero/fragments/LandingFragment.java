package com.at.cancerbero.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.InstallationAdapter;
import com.at.cancerbero.installations.model.domain.Installation;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.InstallationLoaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LandingFragment extends AppFragment {

    private ListView listView;

    private SwipeRefreshLayout swipeRefreshLayout;

    public LandingFragment() {
    }

    @Override
    public boolean handle(Event event) {
        boolean result = false;

        if (event instanceof InstallationLoaded) {
            showItems(((InstallationLoaded) event).installations);
            swipeRefreshLayout.setRefreshing(false);
            result = true;
        }

        return result;
    }

    private void showItems(Set<Installation> installations) {
        if (listView != null) {
            if (installations.isEmpty()) {
                listView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.VISIBLE);
                listView.setItemChecked(-1, true);

                List<Installation> values = new ArrayList<>(installations);
                listView.setAdapter(new InstallationAdapter(getContext(), values));
            }
        }
    }


    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_landing, container, false);

        getSupportActionBar().show();

        listView = view.findViewById(R.id.list_installations);
        registerForContextMenu(listView);

        swipeRefreshLayout = view.findViewById(R.id.layout_swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadInstallations(view);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.showContextMenuForChild(view);
            }

        });

        loadInstallations(view);

        return view;
    }

    private void loadInstallations(View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        getMainService().loadInstallations();
    }


    @Override
    public void onResume() {
        super.onResume();
        getMainService().loadUserDetails();
    }
}
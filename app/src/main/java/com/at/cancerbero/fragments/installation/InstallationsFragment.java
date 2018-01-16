package com.at.cancerbero.fragments.installation;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.InstallationsAdapter;
import com.at.cancerbero.fragments.AppFragment;
import com.at.cancerbero.installations.model.domain.Installation;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.InstallationsLoaded;
import com.at.cancerbero.service.events.ServerError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InstallationsFragment extends AppFragment {

    private ListView listView;

    public InstallationsFragment() {
    }

    @Override
    public boolean handle(Event event) {
        boolean result = false;

        if (event instanceof InstallationsLoaded) {
            showItems(((InstallationsLoaded) event).installations);
            setRefreshing(false);
            result = true;
        } else if (event instanceof ServerError) {
            setRefreshing(false);
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
                listView.setAdapter(new InstallationsAdapter(getContext(), values));
            }
        }
    }

    @Override
    public void onCreateOptionsMenuApp(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actions, menu);
    }


    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_installations, container, false);

        getSupportActionBar().show();

        listView = view.findViewById(R.id.list_installations);
        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Installation installation = (Installation) listView.getItemAtPosition(position);
                selectInstallation(installation);
            }

        });

        getMainActivity().setActivityTitle(R.string.title_installations);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            loadInstallations(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void selectInstallation(Installation itemValue) {
        changeFragment(InstallationFragment.class);
        InstallationFragment currentFragment = (InstallationFragment) getCurrentFragment();
        currentFragment.setInstallationId(itemValue.id);
        currentFragment.showItems(itemValue);
    }

    private void loadInstallations(boolean force) {
        getMainActivity().setRefreshing(true);
        getMainService().loadInstallations(force);
    }


    @Override
    public void onResume() {
        super.onResume();
        loadInstallations(false);
        getMainService().loadUserDetails();
    }
}
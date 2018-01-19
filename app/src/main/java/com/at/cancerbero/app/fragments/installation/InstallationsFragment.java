package com.at.cancerbero.app.fragments.installation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.InstallationsAdapter;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.domain.model.Installation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InstallationsFragment extends AppFragment {

    private ListView listView;

    public InstallationsFragment() {
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

        listView.setOnItemClickListener((parent, v, position, id) -> {
            Installation installation = (Installation) listView.getItemAtPosition(position);
            selectInstallation(installation);

        });

        getMainActivity().setActivityTitle(R.string.title_installations);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            loadInstallations();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void selectInstallation(Installation installation) {
        Bundle bundle = new Bundle();
        bundle.putString("installationId", installation.id.toString());
        changeFragment(InstallationFragment.class, bundle);
    }

    private void loadInstallations() {
        setRefreshing(true);
        getMainService().getInstallationService().loadInstallations().handle((installations, t) -> {
            runOnUiThread(() -> {
                if (t != null) {
                    showToast(R.string.label_unable_to_load_installations);
                } else {
                    showItems(installations);
                    setRefreshing(false);
                }
            });
            return null;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInstallations();
    }
}
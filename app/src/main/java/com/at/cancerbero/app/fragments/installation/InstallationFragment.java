package com.at.cancerbero.app.fragments.installation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.InstallationAdapter;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.app.fragments.node.NodeFragment;
import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.domain.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstallationFragment extends AppFragment {

    private ListView listView;


    private UUID installationId;

    public InstallationFragment() {
    }

    public void showItems(Installation installation) {
        getMainActivity().setActivityTitle(installation.name);
        if (listView != null) {
            if (installation.nodes.isEmpty()) {
                listView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.VISIBLE);
                listView.setItemChecked(-1, true);

                List<Node> values = new ArrayList<>(installation.nodes);
                listView.setAdapter(new InstallationAdapter(getContext(), values));
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

        restoreFromBundle(savedInstanceState);

        getSupportActionBar().show();

        listView = view.findViewById(R.id.list_installations);
        registerForContextMenu(listView);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            Node node = (Node) listView.getItemAtPosition(position);
            selectNode(node);
        });

        getMainActivity().setActivityTitle(R.string.title_installation);

        return view;
    }

    private void selectNode(Node node) {
        Bundle bundle = new Bundle();
        bundle.putString("nodeId", node.id);
        changeFragment(NodeFragment.class, bundle);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (installationId != null) {
            outState.putString("installationId", installationId.toString());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreFromBundle(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            loadInstallation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadInstallation() {
        if (installationId != null) {
            setRefreshing(true);
            getMainService().getInstallationService().loadInstallation(installationId).handle((installation, t) -> {
                runOnUiThread(() -> {
                    if (t != null) {
                        showToast(R.string.label_unable_to_load_installations);
                    } else {
                        showItems(installation);
                    }
                    setRefreshing(false);
                });
                return null;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInstallation();
    }

    @Override
    public boolean onBackPressed() {
        changeFragment(InstallationsFragment.class);
        return true;
    }

    private void restoreFromBundle(Bundle extras) {
        if (extras != null) {
            if (extras.containsKey("installationId")) {
                installationId = UUID.fromString(extras.getString("installationId"));
                loadInstallation();
            }
        }
    }
}
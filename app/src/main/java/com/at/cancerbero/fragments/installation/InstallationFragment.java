package com.at.cancerbero.fragments.installation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.InstallationAdapter;
import com.at.cancerbero.fragments.AppFragment;
import com.at.cancerbero.fragments.node.NodeFragment;
import com.at.cancerbero.installations.model.domain.Installation;
import com.at.cancerbero.installations.model.domain.Node;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.InstallationLoaded;
import com.at.cancerbero.service.events.ServerError;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstallationFragment extends AppFragment {

    private ListView listView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private UUID installationId;

    public InstallationFragment() {
    }

    public void setInstallationId(UUID installationId) {
        this.installationId = installationId;
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
    public boolean handle(Event event) {
        boolean result = false;

        if (event instanceof InstallationLoaded) {
            Installation installation = ((InstallationLoaded) event).installation;

            if (installation.id.equals(installationId)) {
                showItems(installation);
            }
            swipeRefreshLayout.setRefreshing(false);
            result = true;
        } else if (event instanceof ServerError) {
            swipeRefreshLayout.setRefreshing(false);
        }

        return result;
    }


    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_installations, container, false);

        getSupportActionBar().show();

        listView = view.findViewById(R.id.list_installations);
        registerForContextMenu(listView);

        swipeRefreshLayout = view.findViewById(R.id.layout_swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadInstallation(view);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Node node = (Node) listView.getItemAtPosition(position);
                selectNode(node);
            }

        });

        loadInstallation(view);

        getMainActivity().setActivityTitle(R.string.title_installation);

        return view;
    }

    private void selectNode(Node node) {
        changeFragment(NodeFragment.class);
        NodeFragment currentFragment = (NodeFragment) getCurrentFragment();
        currentFragment.setNodeId(node.id);
        currentFragment.showItem(node);
    }

    private void loadInstallation(View view) {
        if (installationId != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            getMainService().loadInstallation(installationId);
        }
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

        if((savedInstanceState != null)  && (savedInstanceState.containsKey("installationId"))){
            installationId = UUID.fromString(savedInstanceState.getString("installationId"));
            loadInstallation(getView());
        }
    }

    @Override
    public boolean onBackPressed() {
        changeFragment(InstallationsFragment.class);
        return true;
    }
}
package com.at.cancerbero.app.fragments.node;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.NodesAdapter;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.domain.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NodesFragment extends AppFragment {

    private ListView listView;


    public void showItems(List<Node> nodes) {
//        if (nodes.size() == 1) {
//            Node node = nodes.iterator().next();
//            selectNode(node);
//        } else {
            if (listView != null) {
                if (nodes.isEmpty()) {
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                    listView.setItemChecked(-1, true);

                    listView.setAdapter(new NodesAdapter(getContext(), nodes));
                }
//            }
        }
    }


    @Override
    public void onCreateOptionsMenuApp(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actions, menu);
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_nodes, container, false);

        getSupportActionBar().show();

        listView = view.findViewById(R.id.list_installations);
        registerForContextMenu(listView);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            Node node = (Node) listView.getItemAtPosition(position);
            selectNode(node);
        });

        getMainActivity().setActivityTitle(R.string.title_nodes);

        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            loadNodes();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadNodes() {
        setRefreshing(true);
        getMainService().getNodeService().loadNodes().handle((nodes, t) -> {
            runOnUiThread(() -> {
                if (t != null) {
                    showToast(R.string.label_unable_to_load_nodes);
                } else {
                    showItems(nodes);
                }
                setRefreshing(false);
            });
            return null;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNodes();
    }
}
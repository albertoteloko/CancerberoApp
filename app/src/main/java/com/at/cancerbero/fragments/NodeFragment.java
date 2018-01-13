package com.at.cancerbero.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.installations.model.domain.Node;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.NodeLoaded;
import com.at.cancerbero.service.events.ServerError;

public class NodeFragment extends AppFragment {

    private ListView listView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String nodeId;

    public NodeFragment() {
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void showItem(Node node) {
//        if (listView != null) {
//            if (node.nodes.isEmpty()) {
//                listView.setVisibility(View.GONE);
//            } else {
//                listView.setVisibility(View.VISIBLE);
//                listView.setItemChecked(-1, true);
//
//                List<Node> values = new ArrayList<>(node.nodes);
//                listView.setAdapter(new NodeAdapter(getContext(), values));
//            }
//        }
    }


    @Override
    public boolean handle(Event event) {
        boolean result = false;

        if (event instanceof NodeLoaded) {
            Node node = ((NodeLoaded) event).node;

            if (node.id.equals(nodeId)) {
                showItem(node);
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
        final View view = inflater.inflate(R.layout.fragment_node, container, false);

        getSupportActionBar().show();

        listView = view.findViewById(R.id.list_pins);
        registerForContextMenu(listView);

        swipeRefreshLayout = view.findViewById(R.id.layout_swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNode(view);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.showContextMenuForChild(view);
            }

        });

        loadNode(view);

        return view;
    }

    private void loadNode(View view) {
        if (nodeId != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            getMainService().loadNode(nodeId);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}
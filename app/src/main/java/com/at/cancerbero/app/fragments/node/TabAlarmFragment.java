package com.at.cancerbero.app.fragments.node;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.InstallationAdapter;
import com.at.cancerbero.adapter.NodeUtils;
import com.at.cancerbero.adapter.PinsAdapter;
import com.at.cancerbero.domain.model.AlarmPin;
import com.at.cancerbero.domain.model.AlarmStatus;
import com.at.cancerbero.domain.model.Node;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class TabAlarmFragment extends TabFragment {

    private TextView nodeName;

    private ImageView statusImage;

    private ListView listView;

    @Override
    public void showItem(Node node) {
        if (nodeName != null) {
            nodeName.setText(getStatusText(node));
        }

        if (statusImage != null) {
            statusImage.setImageResource(getImage(node));
        }

        List<AlarmPin> pins = getPins(node);
        if (listView != null) {
            if (pins.isEmpty()) {
                listView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.VISIBLE);
                listView.setItemChecked(-1, true);

                listView.setAdapter(new PinsAdapter(getContext(), pins));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tab_alarm, container, false);

        nodeName = view.findViewById(R.id.node_name);

        statusImage = view.findViewById(R.id.node_status);

        listView = view.findViewById(R.id.list_pins);
        registerForContextMenu(listView);

//        listView.setOnItemClickListener((parent, v, position, id) -> {
//            Node node = (Node) listView.getItemAtPosition(position);
//            selectNode(node);
//        });

        return view;
    }

    private int getImage(Node node) {
        AlarmStatus status = AlarmStatus.IDLE;

        if ((node.modules.alarm != null) && (node.modules.alarm.status != null)) {
            status = node.modules.alarm.status.value;
        }

        return NodeUtils.getImage(status);
    }

    private int getStatusText(Node node) {
        AlarmStatus status = AlarmStatus.IDLE;

        if ((node.modules.alarm != null) && (node.modules.alarm.status != null)) {
            status = node.modules.alarm.status.value;
        }

        return NodeUtils.getText(status);
    }

    private List<AlarmPin> getPins(Node node) {
        List<AlarmPin> result = new ArrayList<>();

        if ((node.modules.alarm != null) && (node.modules.alarm.pins != null)) {
            result = StreamSupport.stream(node.modules.alarm.pins.values()).sorted((v1, v2) -> v1.name.compareTo(v2.name)).collect(Collectors.toList());
        }

        return result;
    }

}

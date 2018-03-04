package com.at.cancerbero.app.fragments.node;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.NodeUtils;
import com.at.cancerbero.adapter.PinsAdapter;
import com.at.cancerbero.domain.model.AlarmPin;
import com.at.cancerbero.domain.model.AlarmStatus;
import com.at.cancerbero.domain.model.AlarmStatusEvent;
import com.at.cancerbero.domain.model.Node;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import java8.util.concurrent.CompletableFuture;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class TabAlarmFragment extends TabFragment {

    private TextView nodeName;

    private ImageView statusImage;

    private ListView listView;

    private ImageButton imageButton;

    private String nodeId;

    private AlarmStatus currentStatus;

    @Override
    public void showItem(Node node) {
        AlarmStatusEvent status = getStatus(node);
        nodeId = node.id;
        currentStatus = status.value;
        nodeName.setText(NodeUtils.getText(currentStatus));
        statusImage.setImageResource(NodeUtils.getImage(currentStatus));

        List<AlarmPin> pins = getPins(node);
        if (pins.isEmpty()) {
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
            listView.setItemChecked(-1, true);

            listView.setAdapter(new PinsAdapter(getContext(), status, pins));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tab_alarm, container, false);

        nodeName = view.findViewById(R.id.node_name);

        statusImage = view.findViewById(R.id.node_status);

        listView = view.findViewById(R.id.list_pins);
        registerForContextMenu(listView);

        imageButton = view.findViewById(R.id.key_button);

        imageButton.setOnClickListener((view1 -> {
            CompletableFuture<Boolean> future = getMainService().getNodeService().alarmKey(nodeId);
            ProgressDialog dialog;
            if (currentStatus != AlarmStatus.IDLE) {
                dialog = showProgressMessage(R.string.label_disabling_alarm);
            } else {
                dialog = showProgressMessage(R.string.label_enabling_alarm);
            }

            future.handle((v, t) -> {
                runOnUiThread(() -> {
                    if (t != null) {
                        showToast(R.string.label_unable_to_perform_action);
                        Log.e(TAG, "Unable to key", t);
                    } else {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    loadNode();
                                });
                            }
                        }, 500);
                    }
                    dialog.dismiss();
                });
                return null;
            });
        }));

        getNodeFragment().addNodeListener(this::showItem);

        return view;
    }

    private List<AlarmPin> getPins(Node node) {
        List<AlarmPin> result = new ArrayList<>();

        if ((node.modules.alarm != null) && (node.modules.alarm.pins != null)) {
            result = StreamSupport.stream(node.modules.alarm.pins.values()).sorted((v1, v2) -> v1.name.compareTo(v2.name)).collect(Collectors.toList());
        }

        return result;
    }

    private AlarmStatusEvent getStatus(Node node) {
        AlarmStatusEvent status = new AlarmStatusEvent(
                "",
                Calendar.getInstance().getTime(),
                AlarmStatus.IDLE
        );

        if ((node.modules.alarm != null) && (node.modules.alarm.status != null)) {
            status = node.modules.alarm.status;
        }

        return status;
    }
}

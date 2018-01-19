package com.at.cancerbero.app.fragments.node;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.NodeUtils;
import com.at.cancerbero.domain.model.AlarmStatus;
import com.at.cancerbero.domain.model.Node;

public class TabAlarmFragment extends TabFragment {

    private TextView nodeName;

    private ImageView statusImage;

    @Override
    public void showItem(Node node) {
        if (nodeName != null) {
            nodeName.setText(getStatusText(node));
        }

        if (statusImage != null) {
            statusImage.setImageResource(getImage(node));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tab_alarm, container, false);

        nodeName = view.findViewById(R.id.node_name);

        statusImage = view.findViewById(R.id.node_status);

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

}

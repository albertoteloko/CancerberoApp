package com.at.cancerbero.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.installations.model.common.AlarmStatus;
import com.at.cancerbero.installations.model.domain.Installation;
import com.at.cancerbero.installations.model.domain.Node;

import java.util.List;

public class InstallationAdapter extends ArrayAdapter<Node> {
    private final Context context;
    private final List<Node> values;

    public InstallationAdapter(Context context, List<Node> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.layout_installations, parent, false);
        }


        TextView textView = rowView.findViewById(R.id.text_description);
        ImageView imageView = rowView.findViewById(R.id.image_food);

        Node node = values.get(position);
        textView.setText(node.name);
        imageView.setImageResource(getImage(node));
        return rowView;
    }

    private int getImage(Node node) {
        AlarmStatus status = AlarmStatus.IDLE;

        if ((node.modules.alarm != null) && (node.modules.alarm.status != null)) {
            status = node.modules.alarm.status.value;
        }

        switch (status) {
            case SABOTAGE:
                return R.drawable.status_sabotage;
            case SAFETY:
                return R.drawable.status_safety;
            case ALARMED:
                return R.drawable.status_alarmed;
            case SUSPICIOUS:
                return R.drawable.status_suspicious;
            case ACTIVATED:
                return R.drawable.status_activated;
            case ACTIVATING:
                return R.drawable.status_activating;
            default:
                return R.drawable.status_idle;
        }
    }
}
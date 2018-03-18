package com.at.hal9000.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.at.hal9000.Hal9000App.R;
import com.at.hal9000.domain.model.AlarmStatus;
import com.at.hal9000.domain.model.Node;

import java.util.List;

public class NodesAdapter extends ArrayAdapter<Node> {
    private final Context context;
    private final List<Node> values;

    public NodesAdapter(Context context, List<Node> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.layout_list_item, parent, false);
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

        return NodeUtils.getImage(status);
    }
}
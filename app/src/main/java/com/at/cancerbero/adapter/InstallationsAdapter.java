package com.at.cancerbero.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.installations.model.domain.Installation;

import java.util.List;

public class InstallationsAdapter extends ArrayAdapter<Installation> {
    private final Context context;
    private final List<Installation> values;

    public InstallationsAdapter(Context context, List<Installation> values) {
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

        Installation installation = values.get(position);
        textView.setText(installation.name);
        imageView.setImageResource(getImage(installation));
        return rowView;
    }

    private int getImage(Installation installation) {
        switch (installation.getAlarmStatus()) {
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
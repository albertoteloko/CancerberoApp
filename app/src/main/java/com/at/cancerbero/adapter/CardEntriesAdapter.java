package com.at.cancerbero.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.domain.model.AlarmPin;
import com.at.cancerbero.domain.model.AlarmPinChangeEvent;
import com.at.cancerbero.domain.model.AlarmStatusChangeEvent;
import com.at.cancerbero.domain.model.PinInput;
import com.at.cancerbero.domain.model.PinMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CardEntriesAdapter extends ArrayAdapter<Map.Entry<String, String>> {
    private final Context context;
    private final List<Map.Entry<String, String>> values;

    public CardEntriesAdapter(Context context, List<Map.Entry<String, String>> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.layout_list_card_entries, parent, false);
        }


        TextView textView = rowView.findViewById(R.id.text_name);
        TextView textValue = rowView.findViewById(R.id.text_value);

        Map.Entry<String, String> pin = values.get(position);
        textView.setText(pin.getValue());
        textValue.setText(pin.getKey());

        return rowView;
    }

    private boolean isEnable(AlarmPin pin) {
        int value = getChangeEvent(pin).value;
        if (pin.input == PinInput.DIGITAL) {
            if (pin.mode == PinMode.LOW) {
                return value == 0;
            } else {
                return value != 0;
            }
        } else {
            if (pin.mode == PinMode.LOW) {
                return value <= pin.threshold;
            } else {
                return value >= pin.threshold;
            }
        }
    }

    private AlarmPinChangeEvent getChangeEvent(AlarmPin pin) {
        return pin.activations;
    }
}
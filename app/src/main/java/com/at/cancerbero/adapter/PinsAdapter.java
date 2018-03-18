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
import com.at.cancerbero.domain.model.PinValue;
import com.at.cancerbero.domain.model.AlarmStatusEvent;
import com.at.cancerbero.domain.model.PinInput;
import com.at.cancerbero.domain.model.PinMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PinsAdapter extends ArrayAdapter<AlarmPin> {
    private final Context context;
    private final List<AlarmPin> values;

    public PinsAdapter(Context context, AlarmStatusEvent status, List<AlarmPin> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.layout_list_pin, parent, false);
        }


        TextView textView = rowView.findViewById(R.id.text_description);
        TextView textValue = rowView.findViewById(R.id.text_value);
        TextView textDate = rowView.findViewById(R.id.text_date);

        AlarmPin pin = values.get(position);
        textView.setText(pin.name);

        int value = pin.readings.value;
        Date date = pin.readings.timestamp;
        DateFormat format =  new SimpleDateFormat("dd/MM/YYYY HH:mm");
        textDate.setText(format.format(date));

        if (pin.input == PinInput.DIGITAL) {
            if (isEnable(pin)) {
                textValue.setText(R.string.label_ko);
            } else {
                textValue.setText(R.string.label_ok);
            }
        } else {
            textValue.setText(value + " " + pin.unit);
        }
        if (isEnable(pin)) {
            textValue.setTextColor(ContextCompat.getColor(context, R.color.alert));
        } else {
            textValue.setTextColor(ContextCompat.getColor(context, R.color.success));
        }
        return rowView;
    }

    private boolean isEnable(AlarmPin pin) {
        int value = pin.readings.value;
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
}
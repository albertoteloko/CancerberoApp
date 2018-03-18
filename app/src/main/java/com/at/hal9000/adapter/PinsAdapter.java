package com.at.hal9000.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.at.hal9000.Hal9000App.R;
import com.at.hal9000.domain.model.AlarmPin;
import com.at.hal9000.domain.model.AlarmStatusEvent;
import com.at.hal9000.domain.model.PinInput;
import com.at.hal9000.domain.model.PinMode;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
        DateFormat format = new SimpleDateFormat("dd/MM/YYYY HH:mm");
        textDate.setText(format.format(date));

        if (pin.input == PinInput.DIGITAL) {
            if (isEnable(pin)) {
                textValue.setText(R.string.label_ko);
            } else {
                textValue.setText(R.string.label_ok);
            }
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            float finalValue = value;

            if (pin.scale != null) {
                finalValue = finalValue * pin.scale;
            }
            textValue.setText(df.format(finalValue) + " " + pin.unit);
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
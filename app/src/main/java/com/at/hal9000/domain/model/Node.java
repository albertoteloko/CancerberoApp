package com.at.hal9000.domain.model;

import android.util.Log;

import com.at.hal9000.domain.service.push.model.AlarmPinValueEvent;
import com.at.hal9000.domain.service.push.model.AlarmStatusChanged;
import com.at.hal9000.domain.service.push.model.Event;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Node {

    protected final String TAG = getClass().getSimpleName();

    public final String id;
    public final String name;
    public final NodeType type;
    public final Date lastPing;
    public final NodeModules modules;

    public void handleEvent(Event event) {
        if (modules.alarm != null) {
            if (event instanceof AlarmStatusChanged) {
                AlarmStatusChanged alarmStatusChanged = (AlarmStatusChanged) event;
                modules.alarm.status = new AlarmStatusEvent(
                        alarmStatusChanged.getSource(),
                        alarmStatusChanged.getSourceName(),
                        alarmStatusChanged.getTimestamp(),
                        alarmStatusChanged.getValue()
                );
            } else if (event instanceof AlarmPinValueEvent) {
                AlarmPinValueEvent alarmPinValueEvent = (AlarmPinValueEvent) event;
                AlarmPin pin = modules.alarm.pins.get(alarmPinValueEvent.getPinId());

                if (pin != null) {
                    pin.readings = new PinValue(
                            alarmPinValueEvent.getTimestamp(),
                            Integer.parseInt(alarmPinValueEvent.getValue())
                    );
                }else{
                    Log.w(TAG, "Pin not found: " + alarmPinValueEvent.getPinId());
                }
            }
        }
    }
}

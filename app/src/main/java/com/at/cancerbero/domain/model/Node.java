package com.at.cancerbero.domain.model;

import android.util.Log;

import com.at.cancerbero.domain.service.push.model.AlarmPinActivatedEvent;
import com.at.cancerbero.domain.service.push.model.AlarmPinValueChangedEvent;
import com.at.cancerbero.domain.service.push.model.AlarmStatusChanged;
import com.at.cancerbero.domain.service.push.model.Event;

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
                        alarmStatusChanged.getTimestamp(),
                        alarmStatusChanged.getValue()
                );
            } else if (event instanceof AlarmPinValueChangedEvent) {
                AlarmPinValueChangedEvent alarmPinValueChangedEvent = (AlarmPinValueChangedEvent) event;
                AlarmPin pin = modules.alarm.pins.get(alarmPinValueChangedEvent.getPinId());

                if (pin != null) {
                    pin.readings = new AlarmPinEvent(
                            alarmPinValueChangedEvent.getTimestamp(),
                            Integer.parseInt(alarmPinValueChangedEvent.getValue())
                    );
                }else{
                    Log.w(TAG, "Pin not found: " + alarmPinValueChangedEvent.getPinId());
                }
            } else if (event instanceof AlarmPinActivatedEvent) {
                AlarmPinActivatedEvent alarmPinActivatedEvent = (AlarmPinActivatedEvent) event;
                AlarmPin pin = modules.alarm.pins.get(alarmPinActivatedEvent.getPinId());

                if (pin != null) {
                    pin.activations = new AlarmPinEvent(
                            alarmPinActivatedEvent.getTimestamp(),
                            Integer.parseInt(alarmPinActivatedEvent.getValue())
                    );
                    pin.readings = new AlarmPinEvent(
                            alarmPinActivatedEvent.getTimestamp(),
                            Integer.parseInt(alarmPinActivatedEvent.getValue())
                    );
                }else{
                    Log.w(TAG, "Pin not found: " + alarmPinActivatedEvent.getPinId());
                }
            }
        }
    }
}

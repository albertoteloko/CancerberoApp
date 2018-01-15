package com.at.cancerbero.adapter;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.installations.model.common.AlarmStatus;

public class ImageUtils {

    public static int getImage(AlarmStatus status) {
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
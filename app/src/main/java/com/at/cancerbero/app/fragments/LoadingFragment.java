package com.at.cancerbero.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;

public class LoadingFragment extends AppFragment {

    private Handler timerHandler = new Handler();

    final Runnable updateText = new Runnable() {

        private int times = 0;

        @Override
        public void run() {
            String spaces = "";
            String dots = "";

            for (int i = 0; i < (times % 4); i++) {
                spaces += " ";
                dots += ".";
            }

            textView.setText(spaces + originalText + dots);
            times++;
            timerHandler.postDelayed(updateText, 1000);
        }
    };

    private String originalText;
    private TextView textView;

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_loading, container, false);

        textView = view.findViewById(R.id.text);
        setText(R.string.label_loading);

        timerHandler.postDelayed(updateText, 1000);

        return view;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    public void setText(int textId) {
        originalText = getString(textId);
        textView.setText(originalText);
    }

}

package com.at.cancerbero.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.at.cancerbero.CancerberoApp.R;

public class LoginFragment extends AppFragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;

    private Button signUpButton;
    private Button forgotPasswordButton;


    public LoginFragment() {
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = (EditText) view.findViewById(R.id.editTextUserId);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewUserIdLabel);
                    label.setText(R.string.email);
                    emailEditText.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewUserIdMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewUserIdLabel);
                    label.setText("");
                }
            }
        });

        passwordEditText = (EditText) view.findViewById(R.id.editTextUserPassword);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewUserPasswordLabel);
                    label.setText(R.string.Password);
                    passwordEditText.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewUserPasswordMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewUserPasswordLabel);
                    label.setText("");
                }
            }
        });


        return view;
    }

    public void showErrorDialog(String error) {
        Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }
}
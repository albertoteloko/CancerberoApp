package com.at.cancerbero.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.ForgotPasswordFail;
import com.at.cancerbero.service.events.ForgotPasswordSuccess;

public class ForgotPasswordFragment extends AppFragment {

    private EditText passwordInput;
    private EditText codeInput;
    private Button setPassword;
    private AlertDialog alertDialog;

    public ForgotPasswordFragment() {
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle extras) {
        final View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        if (extras != null) {
            if (extras.containsKey("userId")) {
                String userId = extras.getString("userId");
                TextView message = (TextView) view.findViewById(R.id.textViewForgotPasswordMessage);
                String textToDisplay = "Code to set a new password was sent to " + userId;
                message.setText(textToDisplay);
            }
        }

        passwordInput = (EditText) view.findViewById(R.id.editTextForgotPasswordPass);
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordUserIdLabel);
                    label.setText(passwordInput.getHint());
                    passwordInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordUserIdMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordUserIdLabel);
                    label.setText("");
                }
            }
        });

        codeInput = (EditText) view.findViewById(R.id.editTextForgotPasswordCode);
        codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordCodeLabel);
                    label.setText(codeInput.getHint());
                    codeInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordCodeMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordCodeLabel);
                    label.setText("");
                }
            }
        });

        setPassword = (Button) view.findViewById(R.id.ForgotPassword_button);
        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword(view);
            }
        });
        passwordInput.requestFocus();

        return view;
    }

    private void changePassword(View view) {
        String newPassword = passwordInput.getText().toString();

        if (newPassword == null || newPassword.length() < 1) {
            TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordUserIdMessage);
            label.setText(passwordInput.getHint() + " cannot be empty");
            passwordInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
            return;
        }

        String verCode = codeInput.getText().toString();

        if (verCode == null || verCode.length() < 1) {
            TextView label = (TextView) view.findViewById(R.id.textViewForgotPasswordCodeMessage);
            label.setText(codeInput.getHint() + " cannot be empty");
            codeInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
            return;
        }
        getMainService().changePasswordForgotten(newPassword, verCode);
    }

    private void closeProgressDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public boolean handle(Event event) {
        boolean result = false;

        if (event instanceof ForgotPasswordFail) {
            showErrorDialog("Unable to change the password");
            closeProgressDialog();
            result = true;
        } else if (event instanceof ForgotPasswordSuccess) {
            showErrorDialog("Password changed");
            changeFragment(LoginFragment.class);
            closeProgressDialog();
            result = true;
        }

        return result;
    }
}
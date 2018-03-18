package com.at.hal9000.app.fragments.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.at.hal9000.Hal9000App.R;
import com.at.hal9000.app.fragments.AppFragment;

public class ForgotPasswordFragment extends AppFragment {

    private TextView message;
    private EditText passwordInput;
    private EditText codeInput;
    private Button setPassword;

    private String userId;

    public ForgotPasswordFragment() {
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreFromBundle(savedInstanceState);
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle extras) {
        final View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        message = view.findViewById(R.id.textViewForgotPasswordMessage);
        restoreFromBundle(extras);

        passwordInput = view.findViewById(R.id.editTextForgotPasswordPass);
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewForgotPasswordUserIdLabel);
                    label.setText(passwordInput.getHint());
                    passwordInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewForgotPasswordUserIdMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewForgotPasswordUserIdLabel);
                    label.setText("");
                }
            }
        });

        codeInput = view.findViewById(R.id.editTextForgotPasswordCode);
        codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewForgotPasswordCodeLabel);
                    label.setText(codeInput.getHint());
                    codeInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewForgotPasswordCodeMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewForgotPasswordCodeLabel);
                    label.setText("");
                }
            }
        });

        setPassword = view.findViewById(R.id.ForgotPassword_button);
        setPassword.setOnClickListener((v) -> {
            String newPassword = passwordInput.getText().toString();

            if (newPassword.isEmpty()) {
                TextView label = view.findViewById(R.id.textViewForgotPasswordUserIdMessage);
                label.setText(R.string.label_password_empty);
                passwordInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
                return;
            }

            String verCode = codeInput.getText().toString();

            if (verCode.isEmpty()) {
                TextView label = view.findViewById(R.id.textViewForgotPasswordCodeMessage);
                label.setText(R.string.label_code_empty);
                codeInput.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
                return;
            }

            ProgressDialog dialog = showProgressMessage(R.string.label_changing_password);

            getMainService().getSecurityService().changePasswordForgotten(userId, newPassword, verCode).handle((vo, t) -> {
                if (t != null) {
                    showToast(R.string.label_unable_to_change_password);
                    Log.e(TAG, "Unable to change password", t);
                } else {
                    showToast(R.string.label_password_changed);
                    changeFragment(LoginFragment.class);
                }
                dialog.dismiss();
                return null;
            });
        });
        passwordInput.requestFocus();

        return view;
    }

    private void restoreFromBundle(Bundle extras) {
        if (extras != null) {
            if (extras.containsKey("userId")) {
                userId = extras.getString("userId");
                String textToDisplay = "Code to set a new password was sent to " + userId;
                message.setText(textToDisplay);
            }
        }
    }


    @Override
    public boolean onBackPressed() {
        changeFragment(LoginFragment.class);
        return true;
    }
}
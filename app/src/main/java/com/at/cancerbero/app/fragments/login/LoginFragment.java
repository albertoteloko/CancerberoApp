package com.at.cancerbero.app.fragments.login;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.app.fragments.installation.InstallationsFragment;
import com.at.cancerbero.domain.service.exceptions.AuthenticationContinuationRequired;
import com.at.cancerbero.domain.service.handlers.AuthenticationContinuations;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.ForgotPasswordFail;
import com.at.cancerbero.service.events.ForgotPasswordStart;
import com.at.cancerbero.service.events.LogInFail;

public class LoginFragment extends AppFragment {

    private EditText emailEditText;
    private EditText passwordEditText;

    private TextView inUsername;
    private TextView inPassword;

    private Button signUpButton;
    private TextView forgotPasswordButton;


    public LoginFragment() {
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);

        inUsername = view.findViewById(R.id.textViewUserIdLabel);
        inPassword = view.findViewById(R.id.textViewUserPasswordLabel);

        signUpButton = view.findViewById(R.id.buttonLogIn);
        forgotPasswordButton = view.findViewById(R.id.textViewUserForgotPassword);

        emailEditText = view.findViewById(R.id.editTextUserId);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewUserIdLabel);
                    label.setText(R.string.label_email);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewUserIdMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewUserIdLabel);
                    label.setText("");
                }
            }
        });

        passwordEditText = view.findViewById(R.id.editTextUserPassword);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewUserPasswordLabel);
                    label.setText(R.string.label_password);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewUserPasswordMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewUserPasswordLabel);
                    label.setText("");
                }
            }
        });

        signUpButton.setOnClickListener((targetView) -> {
            String email = emailEditText.getText().toString();
            if (email.isEmpty()) {
                inUsername.setText(R.string.label_email_empty);
                inUsername.setTextColor(Color.parseColor("red"));
                return;
            }


            String password = passwordEditText.getText().toString();
            if (password.isEmpty()) {
                inPassword.setText(R.string.label_password_empty);
                inPassword.setTextColor(Color.parseColor("red"));
                return;
            }

            showToast(R.string.message_title_signing);
            getMainService().getSecurityService().login(email, password).handle((u, t) -> {
                if (t != null) {
                    if ((t.getCause() instanceof AuthenticationContinuationRequired) && (((AuthenticationContinuationRequired) t.getCause()).authenticationContinuations == AuthenticationContinuations.NewPassword)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("userId", email);
                        changeFragment(LoginFirstTimeFragment.class, bundle);
                    } else {
                        showAlertMessage(R.string.message_title_unable_to_login, t.getMessage());
                        Log.e(TAG, "Unable to log in", t);
                    }
                } else {
                    changeFragment(InstallationsFragment.class);
                }
                return null;
            });
        });

        TextView forgotPassword = view.findViewById(R.id.textViewUserForgotPassword);
        forgotPassword.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();

            if (email.isEmpty()) {
                TextView label = view.findViewById(R.id.textViewUserIdMessage);
                label.setText(R.string.label_email_empty);
                inUsername.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
                return;
            }

            getMainService().getSecurityService().forgotPassword(email).handle((u, t) -> {
                if (t != null) {
                    showAlertMessage(R.string.message_title_unable_forgot_password, t.getMessage());
                    Log.e(TAG, "Unable to forgot password in", t);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", email);
                    changeFragment(ForgotPasswordFragment.class, bundle);
                }
                return null;
            });
        });
        emailEditText.requestFocus();

        return view;
    }

    @Override
    public boolean handle(Event event) {
        boolean result = false;

        if (event instanceof LogInFail) {
            Exception exception = ((LogInFail) event).exception;

            if (!exception.getMessage().equals("user ID cannot be null")) {
                showToast("Unable to log in");
            }
            result = true;
        } else if (event instanceof ForgotPasswordStart) {
            ForgotPasswordStart input = ((ForgotPasswordStart) event);

            Bundle bundle = new Bundle();
            bundle.putString("userId", input.userId);
            changeFragment(ForgotPasswordFragment.class, bundle);
            result = true;
        } else if (event instanceof ForgotPasswordFail) {
            showToast("Unable to reset password");
            result = true;
        }

        return result;
    }
}
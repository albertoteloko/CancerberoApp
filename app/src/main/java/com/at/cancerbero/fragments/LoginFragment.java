package com.at.cancerbero.fragments;

import android.graphics.Color;
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

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.service.handlers.AuthenticationChallenge;
import com.at.cancerbero.service.handlers.Event;
import com.at.cancerbero.service.handlers.LogInFail;
import com.at.cancerbero.service.handlers.LogInSuccess;

public class LoginFragment extends AppFragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;

    private TextView inUsername;
    private TextView inPassword;

    private Button signUpButton;
    private TextView forgotPasswordButton;


    public LoginFragment() {
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);

        inUsername = (TextView) view.findViewById(R.id.textViewUserIdLabel);
        inPassword = (TextView) view.findViewById(R.id.textViewUserPasswordLabel);

        signUpButton = (Button) view.findViewById(R.id.buttonLogIn);
        forgotPasswordButton = (TextView) view.findViewById(R.id.textViewUserForgotPassword);

        emailEditText = (EditText) view.findViewById(R.id.editTextUserId);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewUserIdLabel);
                    label.setText(R.string.email);
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

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                if (email == null || email.length() < 1) {
                    inUsername.setText(R.string.label_email_empty);
                    inUsername.setTextColor(Color.parseColor("red"));
                    return;
                }


                String password = passwordEditText.getText().toString();
                if (password == null || password.length() < 1) {
                    inPassword.setText(R.string.label_password_empty);
                    inPassword.setTextColor(Color.parseColor("red"));
                    return;
                }

                showErrorDialog("Signing in...");
                getMainService().login(email, password);
            }
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
                showErrorDialog("Unable to log in");
            }
            result = true;
        } else if (event instanceof LogInSuccess) {
            LogInSuccess logInSuccess = ((LogInSuccess) event);

            changeFragment(LandingFragment.class);
            result = true;
        } else if (event instanceof AuthenticationChallenge) {
            ChallengeContinuation continuation = ((AuthenticationChallenge) event).continuation;
            getMainActivity().setChallengeContinuation(continuation);
            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                changeFragment(LoginFirstTimeFragment.class);
            } else if ("SELECT_MFA_TYPE".equals(continuation.getChallengeName())) {
            }
            result = true;
        }

        return result;
    }

    public void logIn(View view) {

//        AppHelper.getPool().getUser(email).getSessionInBackground(authenticationHandler);
    }
//
//    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
//        @Override
//        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
//            Log.d(TAG, " -- Auth Success");
//            AppHelper.setCurrSession(cognitoUserSession);
//            AppHelper.newDevice(device);
//            closeWaitDialog();
//            launchUser();
//        }
//
//        @Override
//        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
//            closeWaitDialog();
//            Locale.setDefault(Locale.US);
//            getUserAuthentication(authenticationContinuation, username);
//        }
//
//        @Override
//        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
//            closeWaitDialog();
//            mfaAuth(multiFactorAuthenticationContinuation);
//        }
//
//        @Override
//        public void onFailure(Exception e) {
//            closeWaitDialog();
//            TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
//            label.setText("Sign-in failed");
//            inPassword.setBackground(getDrawable(R.drawable.text_border_error));
//
//            label = (TextView) findViewById(R.id.textViewUserIdMessage);
//            label.setText("Sign-in failed");
//            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
//
//            showDialogMessage("Sign-in failed", AppHelper.formatException(e));
//        }
//
//        @Override
//        public void authenticationChallenge(ChallengeContinuation continuation) {
//            /**
//             * For Custom authentication challenge, implement your logic to present challenge to the
//             * user and pass the user's responses to the continuation.
//             */
//            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
//                // This is the first sign-in attempt for an admin created user
//                newPasswordContinuation = (NewPasswordContinuation) continuation;
//                AppHelper.setUserAttributeForDisplayFirstLogIn(newPasswordContinuation.getCurrentUserAttributes(),
//                        newPasswordContinuation.getRequiredAttributes());
//                closeWaitDialog();
//                firstTimeSignIn();
//            } else if ("SELECT_MFA_TYPE".equals(continuation.getChallengeName())) {
//                closeWaitDialog();
//                mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
//                List<String> mfaOptions = mfaOptionsContinuation.getMfaOptions();
//                selectMfaToSignIn(mfaOptions, continuation.getParameters());
//            }
//        }
//    };
}
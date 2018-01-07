package com.at.cancerbero.fragments;

import android.app.ProgressDialog;
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
import com.at.cancerbero.service.handlers.ChangePasswordFail;
import com.at.cancerbero.service.handlers.ChangePasswordSuccess;
import com.at.cancerbero.service.handlers.Event;
import com.at.cancerbero.service.handlers.LogInFail;

public class ChangePasswordFragment extends AppFragment {

    private EditText currPassword;
    private EditText newPassword;
    private Button changeButton;

    private ProgressDialog progressDialog;


    public ChangePasswordFragment() {
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        currPassword = (EditText) view.findViewById(R.id.editTextChangePassCurrPass);
        currPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText(currPassword.getHint());
                    currPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewChangePassCurrPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText("");
                }
            }
        });


        newPassword = (EditText) view.findViewById(R.id.editTextChangePassNewPass);
        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText(newPassword.getHint());
                    newPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewChangePassNewPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText("");
                }
            }
        });

        changeButton = (Button) view.findViewById(R.id.change_pass_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword(view);
            }
        });
        currPassword.requestFocus();

        return view;
    }

    private void changePassword(View view) {
        String cPass = currPassword.getText().toString();

        if (cPass == null || cPass.length() < 1) {
            TextView label = (TextView) view.findViewById(R.id.textViewChangePassCurrPassMessage);
            label.setText(currPassword.getHint() + " cannot be empty");
            currPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
            return;
        }

        String nPass = newPassword.getText().toString();

        if (nPass == null || nPass.length() < 1) {
            TextView label = (TextView) view.findViewById(R.id.textViewChangePassNewPassMessage);
            label.setText(newPassword.getHint() + " cannot be empty");
            newPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
            return;
        }

        closeProgressDialog();
        progressDialog = showProgressMessage("Changing password...");
        getMainService().changePassword(cPass, nPass);
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean handle(Event event) {
        boolean result = false;

        if (event instanceof ChangePasswordFail) {
            showErrorDialog("Unable to change the password");
            closeProgressDialog();
            result = true;
        } else if (event instanceof ChangePasswordSuccess) {
            showErrorDialog("Password changed");
            changeFragment(LandingFragment.class);
            closeProgressDialog();
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
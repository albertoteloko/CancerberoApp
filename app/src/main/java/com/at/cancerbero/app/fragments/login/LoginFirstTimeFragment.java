/*
 * Copyright 2013-2017 Amazon.com,
 * Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the
 * License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, express or implied. See the License
 * for the specific language governing permissions and
 * limitations under the License.
 */

package com.at.cancerbero.app.fragments.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.LogInFail;

import java.util.HashMap;
import java.util.Map;

public class LoginFirstTimeFragment extends AppFragment {
    private String TAG = "LoginFirstTimeFragment";

    private String userId;

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreFromBundle(savedInstanceState);
    }


    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login_first_time, container, false);
        super.onCreate(savedInstanceState);

        restoreFromBundle(savedInstanceState);


        TextView newPassword = view.findViewById(R.id.editTextNewPassPass);
        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewNewPassPassLabel);
                    label.setText(R.string.label_password_empty);
                    newPassword.setBackground(view.getResources().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewNewPassPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewNewPassPassLabel);
                    label.setText("");
                }
            }
        });

        EditText newName = view.findViewById(R.id.editTextName);
        newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewNewNameLabel);
                    label.setText(R.string.label_name_empty);
                    newPassword.setBackground(view.getResources().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewNewNameLabel);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewNewNameLabel);
                    label.setText("");
                }
            }
        });

        Button continueSignIn =  view.findViewById(R.id.buttonNewPass);
        continueSignIn.setOnClickListener((v) -> {
            String newUserPassword = newPassword.getText().toString();
            String newNameValue = newName.getText().toString();

            if (checkString(newUserPassword) && checkString(newNameValue)) {

                ProgressDialog dialog = showProgressMessage(R.string.label_logging);
                getMainService().getSecurityService().firstLogin(userId, newUserPassword, newNameValue).handle((vo, t) -> {
                    if (t != null) {
                        showToast(R.string.message_title_unable_to_login);
                        Log.e(TAG, "Unable to login", t);
                    } else {
                        changeFragment(InstallationsFragment.class);
                    }
                    dialog.dismiss();
                    return null;
                });
            } else {
                showToast(R.string.message_missing_fields);
            }
        });

        newPassword.requestFocus();

        return view;
    }

    private boolean checkString(String newUserPassword) {
        return (newUserPassword != null) && (!newUserPassword.isEmpty());
    }

    private void restoreFromBundle(Bundle extras) {
        if (extras != null) {
            if (extras.containsKey("userId")) {
                userId = extras.getString("userId");
            }
        }
    }


    @Override
    public boolean onBackPressed() {
        changeFragment(LoginFragment.class);
        return true;
    }
}

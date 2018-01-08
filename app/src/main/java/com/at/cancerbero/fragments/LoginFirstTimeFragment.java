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

package com.at.cancerbero.fragments;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.at.cancerbero.service.events.LogInFail;

import java.util.HashMap;
import java.util.Map;

public class LoginFirstTimeFragment extends AppFragment {
    private String TAG = "NewPassword";
    private EditText newPassword;
    private EditText newName;

    private Button continueSignIn;
    private AlertDialog userDialog;


    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login_first_time, container, false);
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        newPassword = (EditText) view.findViewById(R.id.editTextNewPassPass);
        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewNewPassPassLabel);
                    label.setText(newPassword.getHint());
                    newPassword.setBackground(view.getResources().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewNewPassPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewNewPassPassLabel);
                    label.setText("");
                }
            }
        });

        newName = (EditText) view.findViewById(R.id.editTextName);
        newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewNewNameLabel);
                    label.setText(newName.getHint());
                    newPassword.setBackground(view.getResources().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) view.findViewById(R.id.textViewNewNameLabel);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) view.findViewById(R.id.textViewNewNameLabel);
                    label.setText("");
                }
            }
        });

        continueSignIn = (Button) view.findViewById(R.id.buttonNewPass);
        continueSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUserPassword = newPassword.getText().toString();
                String newNameValue = newName.getText().toString();
                Map<String, String> userParams = new HashMap<>();
                userParams.put("given_name", newNameValue);
                if (checkString(newUserPassword) && checkString(newNameValue)) {
                    getMainService().continueWithFirstTimeSignIn(newUserPassword, userParams);
                } else {
                    showDialogMessage("Error", "Enter all required attributed");
                }
            }
        });

        newPassword.requestFocus();

        return view;
    }

    private boolean checkString(String newUserPassword) {
        return (newUserPassword != null) && (!newUserPassword.isEmpty());
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
        }

        return result;
    }
}

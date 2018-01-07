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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.FirstTimeLoginAttributesDisplayAdapter;
import com.at.cancerbero.adapter.ItemToDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginFirstTimeFragment extends AppFragment {
    private String TAG = "NewPassword";
    private EditText newPassword;

    private Button continueSignIn;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;

    private List<ItemToDisplay> userAttributes = new ArrayList<>();

    public List<ItemToDisplay> getUserAttributes() {
        return userAttributes;
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_new_password, container, false);
        super.onCreate(savedInstanceState);

        userAttributes = loadUserAttributes();

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

        continueSignIn = (Button) view.findViewById(R.id.buttonNewPass);
        continueSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUserPassword = newPassword.getText().toString();
                Map<String, String> userParams = toUserParamMap();
                if ((newUserPassword != null) && (!newUserPassword.isEmpty())) {
                    if (checkAttributes(userParams)) {
                        getMainService().continueWithFirstTimeSignIn(newUserPassword, userParams);
                    }
                }
                showDialogMessage("Error", "Enter all required attributed", false);
            }
        });
        refreshItemsDisplayed(view);

        return view;
    }

    private List<ItemToDisplay> loadUserAttributes() {
        List<ItemToDisplay> items = new ArrayList<>();

        Map<String, String> userAttributes = getMainService().getNewPasswordContinuation().getCurrentUserAttributes();
        List<String> mandatoryFields = getMainService().getNewPasswordContinuation().getRequiredAttributes();

        if (mandatoryFields == null) {
            mandatoryFields = new ArrayList<>();
        }

        for (Map.Entry<String, String> attr : userAttributes.entrySet()) {
            if ("phone_number_verified".equals(attr.getKey()) || "email_verified".equals(attr.getKey())) {
                continue;
            }
            String message = "";
            if (mandatoryFields.contains(attr.getKey())) {
                message = "Required";
            }
            items.add(new ItemToDisplay(attr.getKey(), attr.getValue(), message, Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null));
        }

        for (String attr : mandatoryFields) {
            if (!userAttributes.containsKey(attr)) {
                items.add(new ItemToDisplay(attr, "", "Required", Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null));
            }
        }

        return items;
    }

    private Map<String, String> toUserParamMap() {
        Map<String, String> result = new ArrayMap<>();

        for (ItemToDisplay userAttribute : userAttributes) {
            result.put(userAttribute.getLabelText(), userAttribute.getDataText());
        }

        return result;
    }

    private void refreshItemsDisplayed(View view) {
        final FirstTimeLoginAttributesDisplayAdapter attributesAdapter = new FirstTimeLoginAttributesDisplayAdapter(this);
        final ListView displayListView = (ListView) view.findViewById(R.id.listViewCurrentUserDetails);
        displayListView.setAdapter(attributesAdapter);
//        displayListView.setClickable(false);
        displayListView.setItemsCanFocus(true);
        //        displayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TextView data = (TextView) view.findViewById(R.id.editTextUserDetailInput);
//                String attributeType = data.getHint().toString();
//                String attributeValue = data.getText().toString();
//                showAttributeDetail(view, attributeType, attributeValue);
//            }
//        });
    }

    private void showAttributeDetail(final View view, final String attributeType, final String attributeValue) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(attributeType);
        final EditText input = new EditText(getContext());
        input.setText(attributeValue);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        input.requestFocus();
        builder.setView(input);

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showErrorDialog("How you doing?");
                try {
                    String newValue = input.getText().toString();
                    if (!newValue.equals(attributeValue)) {
                        setUserAttribute(attributeType, newValue);
                        refreshItemsDisplayed(view);
                    }
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void setUserAttribute(String key, String value) {
        for (ItemToDisplay userAttribute : userAttributes) {
            if (userAttribute.getLabelText().equals(key)) {
                userAttribute.setDataText(value);
            }
        }
    }

    private boolean checkAttributes(Map<String, String> userParams) {
        boolean result = true;
        Map<String, String> userAttributes = getMainService().getNewPasswordContinuation().getCurrentUserAttributes();
        List<String> mandatoryFields = getMainService().getNewPasswordContinuation().getRequiredAttributes();

        for (String key : userParams.keySet()) {
            if (!userAttributes.containsKey(key)) {
                result = false;
                break;
            }
        }

        if (result) {
            for (String mandatoryField : mandatoryFields) {
                if (!userParams.containsKey(mandatoryField)) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showErrorDialog("How you doing 2?");
                userDialog.dismiss();
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }
}

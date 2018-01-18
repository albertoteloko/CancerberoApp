package com.at.cancerbero.app.fragments.login;

import android.app.ProgressDialog;
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

public class ChangePasswordFragment extends AppFragment {

    private EditText currPassword;
    private EditText newPassword;
    private Button changeButton;


    public ChangePasswordFragment() {
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        currPassword = view.findViewById(R.id.editTextChangePassCurrPass);
        currPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText(currPassword.getHint());
                    currPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewChangePassCurrPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText("");
                }
            }
        });


        newPassword = view.findViewById(R.id.editTextChangePassNewPass);
        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText(newPassword.getHint());
                    newPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = view.findViewById(R.id.textViewChangePassNewPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = view.findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText("");
                }
            }
        });

        changeButton = view.findViewById(R.id.change_pass_button);
        changeButton.setOnClickListener((v) -> {
            String cPass = currPassword.getText().toString();

            if (cPass.isEmpty()) {
                TextView label = view.findViewById(R.id.textViewChangePassCurrPassMessage);
                label.setText(R.string.label_current_password_empty);
                currPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
                return;
            }

            String nPass = newPassword.getText().toString();

            if (nPass.isEmpty()) {
                TextView label = view.findViewById(R.id.textViewChangePassNewPassMessage);
                label.setText(R.string.label_new_password_empty);
                newPassword.setBackground(view.getContext().getDrawable(R.drawable.text_border_error));
                return;
            }

            ProgressDialog dialog = showProgressMessage(R.string.label_changing_password);

            getMainService().getSecurityService().changePassword(cPass, nPass).handle((vo, t) -> {
                if (t != null) {
                    showToast(R.string.label_unable_to_change_password);
                    Log.e(TAG, "Unable to change password", t);
                } else {
                    showToast(R.string.label_password_changed);
                    changeFragment(InstallationsFragment.class);
                }
                dialog.dismiss();
                return null;
            });
        });
        currPassword.requestFocus();

        return view;
    }

    @Override
    public boolean onBackPressed() {
        changeFragment(InstallationsFragment.class);
        return true;
    }
}
package com.at.hal9000.app.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.at.hal9000.Hal9000App.R;

public abstract class AppCompatActivity extends android.support.v7.app.AppCompatActivity {


    public void showToast(int message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public AlertDialog showAlertMessage(int title, int body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AppCompatActivity.this);
        builder.setTitle(title).setMessage(body).setNeutralButton(R.string.message_ok, (DialogInterface dialog, int which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public AlertDialog showAlertMessage(int title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AppCompatActivity.this);
        builder.setTitle(title).setMessage(body).setNeutralButton(R.string.message_ok, (DialogInterface dialog, int which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public AlertDialog showAlertMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AppCompatActivity.this);
        builder.setTitle(title).setMessage(body).setNeutralButton(R.string.message_ok, (DialogInterface dialog, int which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public ProgressDialog showProgressMessage(int message) {
        ProgressDialog waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
        return waitDialog;
    }

    public ProgressDialog showProgressMessage(String message) {
        ProgressDialog waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
        return waitDialog;
    }
}

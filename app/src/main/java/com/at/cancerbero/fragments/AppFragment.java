package com.at.cancerbero.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.activities.MainActivity;
import com.at.cancerbero.service.MainService;
import com.at.cancerbero.service.handlers.Event;
import com.at.cancerbero.service.handlers.Handler;

public abstract class AppFragment extends Fragment implements Handler {

    protected final String TAG = getClass().getSimpleName();

    private MainActivity mainActivity;


    public void showErrorDialog(String error) {
        Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }


    public AlertDialog showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public void afterCreation(MainActivity mainActivity, Bundle arguments) {
        this.mainActivity = mainActivity;
        setArguments(arguments);
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass) {
        changeFragment(fragmentClass, Bundle.EMPTY);
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass, Bundle params) {
        getMainActivity().changeFragment(fragmentClass, params);
    }

    public AppFragment getCurrentFragment() {
        return getMainActivity().getCurrentFragment();
    }

    public MainService getMainService() {
        return getMainActivity().getMainService();
    }

    public ActionBar getSupportActionBar() {
        return getMainActivity().getSupportActionBar();
    }

//    public TabLayout getTabLayout() {
//        return (TabLayout) getMainActivity().findViewById(R.id.layout_tabs);
//    }

    @Override
    public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        onCreateOptionsMenuApp(menu, inflater);
    }

    protected void onCreateOptionsMenuApp(Menu menu, MenuInflater inflater) {

    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);

//        TabLayout tabLayout = getTabLayout();
//        tabLayout.removeAllTabs();
//        tabLayout.setVisibility(View.GONE);
//        tabLayout.setOnTabSelectedListener(null);

        getSupportActionBar().hide();
//        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        getMainActivity().invalidateOptionsMenu();
        return onCreateViewApp(inflater, container, savedInstanceState);
    }

    public abstract View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected void sendEvent(Event event) {
        if (event != null) {
            getMainActivity().handle(event);
        }
    }

    @Override
    public boolean handle(Event event) {
        return false;
    }
}

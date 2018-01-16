package com.at.cancerbero.app.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.Handler;

public abstract class AppFragment extends Fragment implements Handler {

    protected final String TAG = getClass().getSimpleName();

    private MainActivity mainActivity;


    public void showErrorDialog(String error) {
        getMainActivity().showErrorDialog(error);
    }

    protected void setRefreshing(boolean value) {
        getMainActivity().setRefreshing(value);
    }

    public AlertDialog showDialogMessage(String title, String body) {
        return getMainActivity().showDialogMessage(title, body);
    }

    public ProgressDialog showProgressMessage(String message) {
        return getMainActivity().showProgressMessage(message);
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

    public MainAppService getMainService() {
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

    public boolean onBackPressed(){
        return false;
    }
}

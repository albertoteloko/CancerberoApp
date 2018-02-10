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

import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.app.fragments.installation.InstallationFragment;
import com.at.cancerbero.app.fragments.node.NodeFragment;
import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.domain.model.Node;

public abstract class AppFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName();

    private MainActivity mainActivity;

    public void runOnUiThread(Runnable runnable) {
        getMainActivity().runOnUiThread(runnable);
    }

    public void showToast(int message) {
        getMainActivity().showToast(message);
    }

    public void showToast(String message) {
        getMainActivity().showToast(message);
    }

    public AlertDialog showAlertMessage(int title, int body) {
        return getMainActivity().showAlertMessage(title, body);
    }

    public AlertDialog showAlertMessage(int title, String body) {
        return getMainActivity().showAlertMessage(title, body);
    }

    public AlertDialog showAlertMessage(String title, String body) {
        return getMainActivity().showAlertMessage(title, body);
    }

    protected void setRefreshing(boolean value) {
        getMainActivity().setRefreshing(value);
    }

    public AlertDialog showDialogMessage(String title, String body) {
        return getMainActivity().showAlertMessage(title, body);
    }

    public ProgressDialog showProgressMessage(int message) {
        return getMainActivity().showProgressMessage(message);
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

    protected void selectInstallation(Installation installation) {
        Bundle bundle = new Bundle();
        bundle.putString("installationId", installation.id.toString());
        changeFragment(InstallationFragment.class, bundle);
    }

    protected void selectNode(Node node) {
        Bundle bundle = new Bundle();
        bundle.putString("nodeId", node.id);
        changeFragment(NodeFragment.class, bundle);
    }

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

        getSupportActionBar().hide();
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        getMainActivity().invalidateOptionsMenu();
        return onCreateViewApp(inflater, container, savedInstanceState);
    }

    public abstract View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public boolean onBackPressed() {
        return false;
    }

    public void onCardIdRead(String cardId) {

    }
}

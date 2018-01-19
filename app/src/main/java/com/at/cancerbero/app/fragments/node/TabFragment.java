package com.at.cancerbero.app.fragments.node;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.domain.model.Node;

public abstract class TabFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName();

    protected NodeFragment nodeFragment;

    public NodeFragment getNodeFragment() {
        return nodeFragment;
    }

    public void setNodeFragment(NodeFragment nodeFragment) {
        this.nodeFragment = nodeFragment;
    }

    public void loadNode(){
        nodeFragment.loadNode();
    }

    public abstract void showItem(Node node);

    public MainActivity getMainActivity() {
        return nodeFragment.getMainActivity();
    }

    public Context getContext() {
        return getMainActivity().getApplicationContext();
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass) {
        getMainActivity().changeFragment(fragmentClass);
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

    public ProgressDialog showProgressMessage(int message) {
        return getMainActivity().showProgressMessage(message);
    }

    public ProgressDialog showProgressMessage(String message) {
        return getMainActivity().showProgressMessage(message);
    }
}

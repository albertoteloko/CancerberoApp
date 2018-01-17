package com.at.cancerbero.app.fragments.node;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.service.events.Event;

public abstract class TabFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName();

    private NodeFragment nodeFragment;

    public void setTabsFragment(NodeFragment nodeFragment) {
        this.nodeFragment = nodeFragment;
    }

    public NodeFragment getNodeFragment() {
        return nodeFragment;
    }

    public MainActivity getMainActivity() {
        return nodeFragment.getMainActivity();
    }

    public ActionBar getActionBar() {
        return getMainActivity().getSupportActionBar();
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass) {
        getMainActivity().changeFragment(fragmentClass);
    }

    public AppFragment getCurrentFragment() {
        return getMainActivity().getCurrentFragment();
    }

    public MainAppService getMainService() {
        return getMainActivity().getMainService();
    }

    protected void sendEvent(Event event) {
        if (event != null) {
//            getMainActivity().handle(event);
        }
    }

    public boolean handle(Event event) {
        return false;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActionBar().show();
    }
}

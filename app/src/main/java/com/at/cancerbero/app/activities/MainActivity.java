package com.at.cancerbero.app.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.app.fragments.AboutFragment;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.app.fragments.LoadingFragment;
import com.at.cancerbero.app.fragments.installation.InstallationsFragment;
import com.at.cancerbero.app.fragments.login.ChangePasswordFragment;
import com.at.cancerbero.app.fragments.login.LoginFragment;
import com.at.cancerbero.domain.model.User;
import com.at.cancerbero.domain.service.SecurityService;
import com.at.cancerbero.domain.service.push.QuickstartPreferences;
import com.at.cancerbero.domain.service.push.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.HashMap;
import java.util.Map;

import java8.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private static final String CURRENT_FRAGMENT = "CurrentFragment";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    private Map<Class<? extends AppFragment>, Bundle> bundles = new HashMap<>();

    protected final String TAG = getClass().getSimpleName();


    private AppFragment currentFragment;

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    public MainAppService getMainService() {
        return MainAppService.getInstance();
    }

    public void setRefreshing(boolean value) {
        ProgressBar spinner = findViewById(R.id.loading);

        if (spinner != null) {
            spinner.setVisibility(value ? View.VISIBLE : View.GONE);
        }
    }

    public void setActivityTitle(int resourceId) {
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText(resourceId);
    }

    public void setActivityTitle(String value) {
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText(value);
    }


    public AppFragment getCurrentFragment() {
        return currentFragment;
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass) {
        changeFragment(fragmentClass, Bundle.EMPTY);
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass, Bundle params) {
        try {
            if (currentFragment != null) {
                Class<? extends AppFragment> oldFragmentClass = currentFragment.getClass();
                Bundle bundle = getOrSetBundle(oldFragmentClass);
                currentFragment.onSaveInstanceState(bundle);
            }

            getSupportActionBar().hide();
            setRefreshing(false);
            setActivityTitle(R.string.app_name);
            Log.i(TAG, "Changing fragment to: " + fragmentClass.getName());
            currentFragment = fragmentClass.newInstance();
            currentFragment.afterCreation(this, params);

            Bundle bundle = getOrSetBundle(fragmentClass);
            bundle.putAll(params);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, currentFragment).commit();
            fragmentManager.executePendingTransactions();
            currentFragment.onViewStateRestored(bundle);
        } catch (Exception e) {
            Log.e(TAG, "Unable to create new fragment: " + fragmentClass.getName(), e);
        }
    }

    private Bundle getOrSetBundle(Class<? extends AppFragment> fragmentClass) {
        if (!bundles.containsKey(fragmentClass)) {
            bundles.put(fragmentClass, new Bundle());
        }
        return bundles.get(fragmentClass);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Log.i(TAG, "Saving!!");
        savedInstanceState.putString(CURRENT_FRAGMENT, currentFragment.getClass().getName());
        Log.i(TAG, savedInstanceState.getString(CURRENT_FRAGMENT));
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, MainAppService.class);
        startService(serviceIntent);

        toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText(R.string.app_name);
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        nDrawer = findViewById(R.id.nav_view);
        setNavDrawer();

        changeFragment(LoadingFragment.class);

        bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                onRestoreInstanceState(savedInstanceState);
                unbindService(this);
                login();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
            }
        };
        registerReceiver();

        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void login() {
        SecurityService securityService = getMainService().getSecurityService();

        CompletableFuture<User> future;
        if (!securityService.isLogged()) {
            future = securityService.login();
        } else {
            future = securityService.getCurrentUser();
        }

        future.handle((u, t) -> {
            if (t != null) {
                showToast(R.string.message_title_unable_to_login);
                changeFragment(LoginFragment.class);
                Log.e(TAG, "Unable to log in", t);
            } else {
                changeFragment(InstallationsFragment.class);
            }
            return null;
        });
    }

    private void logout() {
        getMainService().getSecurityService().logout().thenAccept(result -> {
            if (result) {
                changeFragment(LoginFragment.class);
            } else {
                showToast(R.string.message_title_unable_to_logout);
            }
        });
    }


    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener((item) -> {
            performAction(item);
            return true;
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();
        // Find which item was selected
        switch (item.getItemId()) {
            case R.id.nav_user_sign_out:
                logout();
                break;
            case R.id.nav_user_about:
                changeFragment(AboutFragment.class);
                break;
            case R.id.nav_user_change_password:
                changeFragment(ChangePasswordFragment.class);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState);
            String tabClassName = savedInstanceState.getString(CURRENT_FRAGMENT);

            if (tabClassName != null) {
                try {
                    Class<? extends AppFragment> savedFragmentClass = (Class<? extends AppFragment>) Class.forName(tabClassName);
                    changeFragment(savedFragmentClass);
                } catch (ClassNotFoundException e) {
                    Log.d(TAG, "Unable to load saved class: " + tabClassName);
                }
            } else {
                login();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        instance = null;
    }

    @Override
    public void onBackPressed() {
        if (!currentFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE)
            );
            isReceiverRegistered = true;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}

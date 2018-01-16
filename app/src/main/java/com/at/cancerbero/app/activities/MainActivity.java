package com.at.cancerbero.app.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.fragments.AboutFragment;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.app.fragments.login.ChangePasswordFragment;
import com.at.cancerbero.app.fragments.installation.InstallationsFragment;
import com.at.cancerbero.app.fragments.LoadingFragment;
import com.at.cancerbero.app.fragments.login.LoginFirstTimeFragment;
import com.at.cancerbero.app.fragments.login.LoginFragment;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.service.SecurityService;
import com.at.cancerbero.service.events.AuthenticationChallenge;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.Handler;
import com.at.cancerbero.service.events.LogInFail;
import com.at.cancerbero.service.events.LogInSuccess;
import com.at.cancerbero.service.events.Logout;
import com.at.cancerbero.service.events.ServerError;
import com.at.cancerbero.service.events.UserDetailsSuccess;
import com.at.cancerbero.service.push.QuickstartPreferences;
import com.at.cancerbero.service.push.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity implements Handler {

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private static final String CURRENT_FRAGMENT = "CurrentFragment";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    protected final String TAG = getClass().getSimpleName();

    @Override
    public boolean handle(Event event) {
        Log.i(TAG, "Incoming event: " + event);
        boolean result = false;

        if (currentFragment != null) {
            result = currentFragment.handle(event);
        }

        if (!result) {
            result = handleEventMyOwn(event);
        }

        return result;
    }

    public MainAppService getMainService() {
        return MainAppService.getInstance();
    }

    public AppFragment getCurrentFragment() {
        return currentFragment;
    }

    private AppFragment currentFragment;

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    private Bundle bundle = new Bundle();

    public void setRefreshing(boolean value) {
        ProgressBar spinner = findViewById(R.id.loading);

        if (spinner != null) {
            spinner.setVisibility(value ? View.VISIBLE : View.GONE);
        }


    }


    public void changeFragment(Class<? extends AppFragment> fragmentClass) {
        changeFragment(fragmentClass, Bundle.EMPTY);
    }

    public void showErrorDialog(String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }


    public AlertDialog showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    public ProgressDialog showProgressMessage(String message) {
        ProgressDialog waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
        return waitDialog;
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass, Bundle params) {
        try {
            if (currentFragment != null) {
                currentFragment.onSaveInstanceState(bundle);
            }
            getSupportActionBar().hide();
            setRefreshing(false);
            setActivityTitle(R.string.app_name);
            Log.i(TAG, "Changing fragment to: " + fragmentClass.getName());
            currentFragment = fragmentClass.newInstance();
            currentFragment.afterCreation(this, params);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, currentFragment).commit();
            fragmentManager.executePendingTransactions();
            currentFragment.onViewStateRestored(bundle);
        } catch (Exception e) {
            Log.e(TAG, "Unable to create new fragment: " + fragmentClass.getName(), e);
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
                initialLogin();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                showErrorDialog("Sent toke: " + sentToken);
            }
        };
        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void initialLogin() {

        SecurityService securityService = getMainService().getSecurityService();
        securityService.login().handle((u, t) -> {
            if(t != null){

            }
            return null;
        });
    }


    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                performAction(item);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();
        // Find which item was selected
        switch (item.getItemId()) {
            case R.id.nav_user_sign_out:
                getMainService().logout();
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
                failSafeWorkflow();
            }
        }
    }

    private void failSafeWorkflow() {
        if (getMainService().getCurrentUser() != null) {
            changeFragment(InstallationsFragment.class);
        } else {
            changeFragment(LoginFragment.class);
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

    private boolean handleEventMyOwn(Event event) {
        boolean result = false;

        Log.i(TAG, "Event: " + event.toString());

        if (event instanceof Logout) {
            changeFragment(LoginFragment.class);
            result = true;
        } else if (event instanceof LogInSuccess) {
            changeFragment(InstallationsFragment.class);
            result = true;
        } else if (event instanceof LogInFail) {
            Exception exception = ((LogInFail) event).exception;

            if (!exception.getMessage().equals("user ID cannot be null")) {
                showErrorDialog("Unable to log in");
            }
            changeFragment(LoginFragment.class);
            result = true;
        } else if (event instanceof AuthenticationChallenge) {
            ChallengeContinuation continuation = ((AuthenticationChallenge) event).continuation;
            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                changeFragment(LoginFirstTimeFragment.class);
            } else if ("SELECT_MFA_TYPE".equals(continuation.getChallengeName())) {
            }
            result = true;
        } else if (event instanceof UserDetailsSuccess) {
            TextView usernameDetails = (TextView) findViewById(R.id.textViewNavUserSub);
            if (usernameDetails != null) {
                UserDetailsSuccess input = (UserDetailsSuccess) event;
                usernameDetails.setText(input.userDetails.getAttributes().getAttributes().get("given_name"));
            }

            result = true;
        } else if (event instanceof ServerError) {
            ServerError input = (ServerError) event;
            showDialogMessage("Server error", input.exception.getMessage());
            result = true;
        }

        return result;
    }
}

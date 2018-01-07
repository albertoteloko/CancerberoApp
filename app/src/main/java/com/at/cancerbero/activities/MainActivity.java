package com.at.cancerbero.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.at.cancerbero.fragments.AppFragment;
import com.at.cancerbero.fragments.LandingFragment;
import com.at.cancerbero.fragments.LoadingFragment;
import com.at.cancerbero.fragments.LoginFragment;
import com.at.cancerbero.service.MainService;
import com.at.cancerbero.service.handlers.Event;
import com.at.cancerbero.service.handlers.Handler;
import com.at.cancerbero.CancerberoApp.R;

public class MainActivity extends AppCompatActivity implements Handler {

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private static final String CURRENT_FRAGMENT = "CurrentFragment";

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

    public MainService getMainService() {
        return MainService.getInstance();
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


    private ChallengeContinuation challengeContinuation;

    public ChallengeContinuation getChallengeContinuation() {
        return challengeContinuation;
    }

    public void setChallengeContinuation(ChallengeContinuation challengeContinuation) {
        this.challengeContinuation = challengeContinuation;
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass) {
        changeFragment(fragmentClass, Bundle.EMPTY);
    }

    public void changeFragment(Class<? extends AppFragment> fragmentClass, Bundle params) {
        try {
            if (currentFragment != null) {
                currentFragment.onSaveInstanceState(bundle);
            }
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

//    public void loginBasic(LoginController controller, boolean rememberMe) {
//        getMainService().loginController = controller;
//        Log.i("ShareFood", controller.getCurrentUser().toString());
//        if (controller instanceof DefaultLoginController) {
//            User user = controller.getCurrentUser();
//            BasicCredentials credentials = new BasicCredentials();
//            credentials.email = user.email;
//            credentials.password = user.password;
//
//            getMainService().loginBasic(credentials, rememberMe);
//        }
//
//        changeFragment(LoadingFragment.class);
//        ((LoadingFragment) currentFragment).setText(R.string.label_singUp);
//    }

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

        Intent intent = new Intent(this, MainService.class);
        startService(intent);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText(R.string.app_name);
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        nDrawer = (NavigationView) findViewById(R.id.nav_view);
        setNavDrawer();

        changeFragment(LoadingFragment.class);

        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                onRestoreInstanceState(savedInstanceState);
                unbindService(this);
                getMainService().login();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
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
        } else {
            failSafeWorkflow();
        }
    }

    private void failSafeWorkflow() {
        if (getMainService().getCurrentUser() != null) {
            changeFragment(LandingFragment.class);
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
        if (!isMainFragment()) {
            changeFragment(LandingFragment.class);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isMainFragment() {
        Class<?>[] mainFragments = {LoadingFragment.class, LoginFragment.class, LandingFragment.class};

        boolean result = false;

        if (currentFragment != null) {
            for (Class<?> fragmentClass : mainFragments) {
                if (fragmentClass.isAssignableFrom(currentFragment.getClass())) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private boolean handleEventMyOwn(Event event) {
        boolean result = false;

        Log.i(TAG, "Event: " + event.toString());


//        if (event instanceof Logged) {
//            changeFragment(TabsFragment.class);
//
//            if (getMainService().loginController instanceof TwitterController && getMainService().loginController.getCurrentUser().email == null) {
//                TwitterSession session = Twitter.getSessionManager().getActiveSession();
//                TwitterAuthClient authClient = new TwitterAuthClient();
//                authClient.requestEmail(session, new Callback<String>() {
//                    @Override
//                    public void success(Result<String> result) {
//                        String email = result.data;
//                        Log.i(TAG, "Email, " + email);
//                        User user = getMainService().loginController.getCurrentUser();
//                        user.email = email;
//                        getMainService().loginController.setCurrentUser(user);
//                        getMainService().saveUser(user);
//                    }
//
//                    @Override
//                    public void failure(TwitterException exception) {
//                        Log.i(TAG, "No Email!!");
//
//                    }
//                });
//            }
//
//        } else if (event instanceof LoginRequired) {
//            changeFragment(LoginFragment.class);
//            String explanationText = ((LoginRequired) event).explanationText;
//
//            if (explanationText != null) {
//                Toast.makeText(getBaseContext(), explanationText, Toast.LENGTH_LONG).show();
//            }
//        } else if (event instanceof UserCreated) {
//            changeFragment(LoginFragment.class);
//        }

        return result;
    }
}

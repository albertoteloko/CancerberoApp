package com.at.cancerbero.app.activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
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
import com.at.cancerbero.app.fragments.login.ChangePasswordFragment;
import com.at.cancerbero.app.fragments.login.LoginFragment;
import com.at.cancerbero.app.fragments.node.NodesFragment;
import com.at.cancerbero.domain.model.Node;
import com.at.cancerbero.domain.model.User;
import com.at.cancerbero.domain.service.SecurityService;

import java.util.HashMap;
import java.util.Map;

import java8.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    public static final String CURRENT_FRAGMENT = "CurrentFragment";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // list of NFC technologies detected:
    private static final String[][] techList = new String[][]{
            new String[]{
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(),
                    Ndef.class.getName()
            }
    };

    private Map<Class<? extends AppFragment>, Bundle> bundles = new HashMap<>();

    protected final String TAG = getClass().getSimpleName();


    private AppFragment currentFragment;

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private TextView navHeaderSubTitle;

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
            fragmentManager.executePendingTransactions();
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
    protected void onCreate(Bundle originalState) {
        super.onCreate(originalState);

        if ((originalState == null) && (getIntent() != null) && (getIntent().getExtras() != null)) {
            originalState = getIntent().getExtras();
        }

        final Bundle savedInstanceState = originalState;
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

        View navigationHeader = nDrawer.getHeaderView(0);
        navHeaderSubTitle = navigationHeader.findViewById(R.id.textViewNavUserSub);

        changeFragment(LoadingFragment.class);

        bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                onRestoreInstanceState(savedInstanceState);
                unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

//        checkPlayServices();
    }

    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        getUserFuture().handle((u, t) -> {
            if (t != null) {
                showToast(R.string.message_title_unable_to_login);
                changeFragment(LoginFragment.class);
                Log.e(TAG, "Unable to log in", t);
            } else {
                navHeaderSubTitle.setText(u.getName());
                Class<? extends AppFragment> fragmentClass = NodesFragment.class;
                Bundle bundle = Bundle.EMPTY;

                if (savedInstanceState != null) {
                    String tabClassName = savedInstanceState.getString(CURRENT_FRAGMENT);

                    if (tabClassName != null) {
                        try {
                            fragmentClass = (Class<? extends AppFragment>) Class.forName(tabClassName);
                            bundle = savedInstanceState;
                        } catch (ClassNotFoundException e) {
                            Log.d(TAG, "Unable to load saved class: " + tabClassName);
                        }
                    }
                }
                changeFragment(fragmentClass, bundle);
            }
            return null;
        });
    }


    public void onNodeChanged(Node node) {
        if (currentFragment != null) {
            runOnUiThread(() -> {
                currentFragment.onNodeChanged(node);
            });
        }
    }

    private CompletableFuture<User> getUserFuture() {
        SecurityService securityService = getMainService().getSecurityService();

        CompletableFuture<User> future;
        if (!securityService.isLogged()) {
            future = securityService.login();
        } else {
            future = securityService.getCurrentUser();
        }
        return future;
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
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            // creating intent receiver for NFC events:
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
            // enabling foreground dispatch for getting intent from NFC event:

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {

            if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                String cardId = byteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
                Log.i(TAG, "New NFC Tag: " + cardId);

                if (currentFragment != null) {
                    currentFragment.onCardIdRead(cardId);
                }
            }
        }
    }

    private String byteArrayToHexString(byte[] input) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < input.length; ++j) {
            if (j != 0) {
                out += ":";
            }
            in = (int) input[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

//    private boolean checkPlayServices() {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
//            } else {
//                Log.i(TAG, "This device is not supported.");
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }
}

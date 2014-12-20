package com.pwspray.trinitasrooster;

import android.app.NotificationManager;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.pwspray.trinitasrooster.Data.DayCursorAdapter;
import com.pwspray.trinitasrooster.Data.RoosterDbHelper;
import com.pwspray.trinitasrooster.Fragments.RoosterPageAdapter;
import com.pwspray.trinitasrooster.Fragments.RoosterDagFragment;
import com.pwspray.trinitasrooster.Fragments.RoosterDetailsFragment;
import com.pwspray.trinitasrooster.Receivers.BootReceiver;
import com.pwspray.trinitasrooster.Services.BackgroundSync;
import com.pwspray.trinitasrooster.Services.DownloadRoosterService;
import com.pwspray.trinitasrooster.Services.LoginService;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity implements
        RoosterDagFragment.RoosterDagFragCallBack, SwipeRefreshLayout.OnRefreshListener{//activity

    private static final String LOG_TAG = "MainActivity";
    public static final boolean LOG_D = true;

    public static final String PREFS_FILE_NAME = "TrinitasPrefsFile";
    public static final String PREFS_USERNAME = "username";
    public static final String PREFS_PASSWORD = "password";

    public static final String PARAM_USERNAME = "com.pwspray.trinitasrooster.extra.USERNAME";
    public static final String PARAM_PASSWORD = "com.pwspray.trinitasrooster.extra.PASSWORD";
    public static final String PARAM_REALNAME = "com.pwspray.trinitasrooster.extra.REALNAME";

    public static final int LOGIN_REQUEST = 0;

    public static CookieManager cookieManager;

    public String username;
    public String password;
    public String realName;
    public boolean loggedIn = false;
    public long lastLogin = 0;

    private ResponseReceiver receiver;

    private RoosterDetailsFragment roosterDetailsFragment;
    private DayCursorAdapter mondayCursorAdapter;
    private DayCursorAdapter tuesdayCursorAdapter;
    private DayCursorAdapter wednesdayCursorAdapter;
    private DayCursorAdapter thursdayCursorAdapter;
    private DayCursorAdapter fridayCursorAdapter;

    private RoosterPageAdapter pageAdapter;
    private ViewPager viewPager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int weekCorrection;

    private String dateMonday;
    private String dateTuesday;
    private String dateWednesday;
    private String dateThursday;
    private String dateFriday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weekCorrection = 0;

        setDates(weekCorrection);

        RoosterDbHelper roosterDbHelper = new RoosterDbHelper(this);
        Cursor monday = roosterDbHelper.getPeriodsByDate(dateMonday);
        Cursor tuesday = roosterDbHelper.getPeriodsByDate(dateTuesday);
        Cursor wednesday = roosterDbHelper.getPeriodsByDate(dateWednesday);
        Cursor thursday = roosterDbHelper.getPeriodsByDate(dateThursday);
        Cursor friday = roosterDbHelper.getPeriodsByDate(dateFriday);
        mondayCursorAdapter = new DayCursorAdapter(this, monday, 0);
        tuesdayCursorAdapter = new DayCursorAdapter(this, tuesday, 0);
        wednesdayCursorAdapter = new DayCursorAdapter(this, wednesday, 0);
        thursdayCursorAdapter = new DayCursorAdapter(this, thursday, 0);
        fridayCursorAdapter = new DayCursorAdapter(this, friday, 0);

        pageAdapter = new RoosterPageAdapter(getSupportFragmentManager(), this);
        pageAdapter.setAdapters(mondayCursorAdapter, tuesdayCursorAdapter, wednesdayCursorAdapter, thursdayCursorAdapter, fridayCursorAdapter);
        pageAdapter.setFragmentDates(dateMonday, dateTuesday, dateWednesday, dateThursday, dateFriday);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pageAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.view_SwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);

        viewPager.setOnTouchListener(new View.OnTouchListener(){ //zorgt er voor dat je perongeluk refreshed als je naar rechts swipe
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeRefreshLayout.setEnabled(false);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        swipeRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });

        setViewPagerToToday();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null)
            setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "savedInstanceState == null");

            //TODO http://developer.android.com/training/basics/activity-lifecycle/recreating.html
            MainActivity.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);

        }
        IntentFilter filter = new IntentFilter(LoginService.ACTION_RETURN_LOGIN);
        filter.addAction(DownloadRoosterService.ACTION_RETURN_DOWNLOAD_ROOSTER);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
        username = settings.getString(PREFS_USERNAME, null);
        password = settings.getString(PREFS_PASSWORD, null);

        if (username == null || password == null) { //Nog nooit ingelogd
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST);
        }

        if(username != null && !username.isEmpty() && password != null && !password.isEmpty()){
            if(savedInstanceState == null){
                BootReceiver.backgroundSync.syncImmediately(this, username, password);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {

            Log.d(LOG_TAG, "On activity result! LOGIN_REQUEST OK");
            String _username = data.getStringExtra(PARAM_USERNAME);
            String _password = data.getStringExtra(PARAM_PASSWORD);
            //String _realName = data.getStringExtra(PARAM_REALNAME);

            if (_username != null && _password != null) { //just logged in
                SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString(PREFS_USERNAME, _username);
                editor.putString(PREFS_PASSWORD, _password);
                editor.commit();

                username = _username;
                password = _password;

                BootReceiver.backgroundSync.cancel(this);
                BootReceiver.backgroundSync.startSync(this, username, password);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_service) {
            RoosterDbHelper helper = new RoosterDbHelper(this);
            helper.resetStates();
        } else if (id == R.id.action_debug) {
            //BackgroundSync bs = new BackgroundSync();
            //bs.syncImmediately(this, username, password, true);

            Log.d(LOG_TAG, "DEBUG REFRESH");
            DownloadRoosterService.startActionDownloadRooster(this, Util.getDateToDownload(), true);
            swipeRefreshLayout.setRefreshing(true);
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onHourSelected(String date, int hour) {
        RoosterDbHelper roosterDbHelper = new RoosterDbHelper(this);
        Cursor c = roosterDbHelper.getPeriodsByDate(date);
        String[][] data = roosterDbHelper.cursorToStringArray(c);
        Bundle args = new Bundle();
        for(int i = 0; i < 8;i++){
            args.putStringArray("period"+String.valueOf(i), data[i]);
            Log.d(LOG_TAG, "created bundle with " +data[i][0] + data[i][1]+ data[i][2]+ data[i][3]+ data[i][4]);
        }
        args.putInt("hourSelected", hour);
        Intent detailsIntent = new Intent(this, DetailsActivity.class);
        detailsIntent.putExtras(args);
        startActivity(detailsIntent);
        c.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRefresh() { //swipe down to refresh
        BackgroundSync bs = new BackgroundSync();
        bs.syncImmediately(this, username, password);

        Log.d(LOG_TAG, "onRefresh() van implement swiperefreshlayour");
    }

    public class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == LoginService.ACTION_RETURN_LOGIN) {
                String name = intent.getStringExtra(LoginService.PARAM_OUT_NAME);

                if (name != null && !name.isEmpty()) { //succes
                    loggedIn = true;
                    lastLogin = System.currentTimeMillis();
                    realName = name;

                    DownloadRoosterService.startActionDownloadRooster(context, Util.getDateToDownload());
                } else { //TODO failure

                }
            }
            else if(intent.getAction() == DownloadRoosterService.ACTION_RETURN_DOWNLOAD_ROOSTER){
                Boolean succes = intent.getBooleanExtra(DownloadRoosterService.PARAM_OUT_SUCCES, false);

                Log.d(LOG_TAG, "ResponseReceiver return download rooster");

                swipeRefreshLayout.setRefreshing(false);

                if(succes){
                    updateAdapter();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void updateAdapter(){
        RoosterDbHelper roosterDbHelper = new RoosterDbHelper(this);
        Cursor monday = roosterDbHelper.getPeriodsByDate(dateMonday);
        Cursor tuesday = roosterDbHelper.getPeriodsByDate(dateTuesday);
        Cursor wednesday = roosterDbHelper.getPeriodsByDate(dateWednesday);
        Cursor thursday = roosterDbHelper.getPeriodsByDate(dateThursday);
        Cursor friday = roosterDbHelper.getPeriodsByDate(dateFriday);
        mondayCursorAdapter.changeCursor(monday);
        tuesdayCursorAdapter.changeCursor(tuesday);
        wednesdayCursorAdapter.changeCursor(wednesday);
        thursdayCursorAdapter.changeCursor(thursday);
        fridayCursorAdapter.changeCursor(friday);

        viewPager.getAdapter().notifyDataSetChanged();
    }

    public void setDates(int cor){
        dateMonday = Util.getDateString(Util.getMonday(Calendar.getInstance(), cor));
        dateTuesday = Util.getDateString(Util.getTuesday(Calendar.getInstance(), cor));
        dateWednesday = Util.getDateString(Util.getWednesday(Calendar.getInstance(), cor));
        dateThursday = Util.getDateString(Util.getThursday(Calendar.getInstance(), cor));
        dateFriday = Util.getDateString(Util.getFriday(Calendar.getInstance(), cor));
    }

    public void setViewPagerToToday(){
        if(dateMonday.equals(Util.getDateToShow()))
            viewPager.setCurrentItem(0);
        else if(dateTuesday.equals(Util.getDateToShow()))
            viewPager.setCurrentItem(1);
        else if(dateWednesday.equals(Util.getDateToShow()))
            viewPager.setCurrentItem(2);
        else if(dateThursday.equals(Util.getDateToShow()))
            viewPager.setCurrentItem(3);
        else if(dateFriday.equals(Util.getDateToShow()))
            viewPager.setCurrentItem(4);
    }
}

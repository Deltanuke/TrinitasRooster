package com.pwspray.trinitasrooster;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.pwspray.trinitasrooster.Fragments.DetailsPageAdapter;
import com.pwspray.trinitasrooster.Fragments.RoosterDetailsFragment;


public class DetailsActivity extends ActionBarActivity {
    private DetailsPageAdapter detailsPageAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null)
            setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        Bundle args = i.getExtras();

        detailsPageAdapter = new DetailsPageAdapter(getSupportFragmentManager());
        detailsPageAdapter.setDetailsContentData(args);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        viewPager.setAdapter(detailsPageAdapter);
        viewPager.setCurrentItem(args.getInt("hourSelected") -1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

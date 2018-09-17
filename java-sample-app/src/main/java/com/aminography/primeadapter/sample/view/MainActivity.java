package com.aminography.primeadapter.sample.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

import com.aminography.primeadapter.sample.R;

/**
 * Created by aminography on 8/17/2018.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        final InstalledAppsFragment fragment = (InstalledAppsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (fragment != null) {
                    switch (compoundButton.getId()) {
                        case R.id.expandableCheckBox:
                            fragment.setExpandable(isChecked);
                            break;
                        case R.id.draggableCheckBox:
                            fragment.setDraggable(isChecked);
                            break;
                        case R.id.dividerCheckBox:
                            fragment.setDivider(isChecked);
                            break;
                    }
                }
            }
        };

        NavigationView navigationView = findViewById(R.id.navigationView);
        final View headerLayout = navigationView.getHeaderView(0);

        ((AppCompatCheckBox) headerLayout.findViewById(R.id.expandableCheckBox)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((AppCompatCheckBox) headerLayout.findViewById(R.id.draggableCheckBox)).setOnCheckedChangeListener(onCheckedChangeListener);
        ((AppCompatCheckBox) headerLayout.findViewById(R.id.dividerCheckBox)).setOnCheckedChangeListener(onCheckedChangeListener);

        headerLayout.findViewById(R.id.restoreDefaultLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }, 100);

                ((AppCompatCheckBox) headerLayout.findViewById(R.id.expandableCheckBox)).setChecked(false);
                ((AppCompatCheckBox) headerLayout.findViewById(R.id.draggableCheckBox)).setChecked(false);
                ((AppCompatCheckBox) headerLayout.findViewById(R.id.dividerCheckBox)).setChecked(true);

                if (fragment != null) {
                    fragment.setExpandable(false);
                    fragment.setDraggable(false);
                    fragment.setDivider(true);
                }
            }
        });
    }

}

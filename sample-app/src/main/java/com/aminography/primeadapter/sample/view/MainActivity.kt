package com.aminography.primeadapter.sample.view

import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatCheckBox
import android.view.View
import android.widget.CompoundButton
import com.aminography.primeadapter.sample.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val fragment = supportFragmentManager.findFragmentById(R.id.fragment) as InstalledAppsFragment

        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
            when (view.id) {
                R.id.expandableCheckBox -> fragment.expandable = isChecked
                R.id.draggableCheckBox -> fragment.draggable = isChecked
                R.id.dividerCheckBox -> fragment.hasDivider = isChecked
            }
        }

        val headerLayout = navigationView.getHeaderView(0)
        headerLayout.findViewById<AppCompatCheckBox>(R.id.expandableCheckBox).setOnCheckedChangeListener(onCheckedChangeListener)
        headerLayout.findViewById<AppCompatCheckBox>(R.id.draggableCheckBox).setOnCheckedChangeListener(onCheckedChangeListener)
        headerLayout.findViewById<AppCompatCheckBox>(R.id.dividerCheckBox).setOnCheckedChangeListener(onCheckedChangeListener)

        headerLayout.findViewById<View>(R.id.restoreDefaultLayout).setOnClickListener {
            Handler().postDelayed({ drawerLayout.closeDrawer(GravityCompat.START) }, 100)
            headerLayout.apply {
                findViewById<AppCompatCheckBox>(R.id.expandableCheckBox).isChecked = false
                findViewById<AppCompatCheckBox>(R.id.draggableCheckBox).isChecked = false
                findViewById<AppCompatCheckBox>(R.id.dividerCheckBox).isChecked = true
            }
            fragment.expandable = false
            fragment.draggable = false
            fragment.hasDivider = true
        }
    }

}

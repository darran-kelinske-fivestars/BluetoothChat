package com.glodanif.bluetoothchat.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.fragment.SettingsFragment

class SettingsActivity : SkeletonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings, ActivityType.CHILD_ACTIVITY)

        fragmentManager.beginTransaction()
                .add(R.id.fl_settings_container, SettingsFragment())
                .commitAllowingStateLoss()
    }

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}

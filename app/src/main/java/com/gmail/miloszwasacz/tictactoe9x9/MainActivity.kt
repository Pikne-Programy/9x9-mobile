package com.gmail.miloszwasacz.tictactoe9x9

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        when(PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getString(getString(R.string.key_theme), "AppTheme")) {
            getString(R.string.theme_dark) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonJoin.setOnClickListener {
            val intent = Intent(this@MainActivity, BoardActivity::class.java)
            intent.putExtra("EXTRA_ROOM_NAME", when(editTextRoomName.text.toString().trim()) {
                "" -> "public"
                else -> editTextRoomName.text.toString().trim()
            })
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if(id == R.id.action_settings) {
            // launch settings activity
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}


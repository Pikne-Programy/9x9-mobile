package com.gmail.miloszwasacz.tictactoe9x9

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.takisoft.preferencex.PreferenceFragmentCompat

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        if(savedInstanceState == null) {
            val fragment = SettingsFragment()
            supportFragmentManager.beginTransaction().replace(R.id.settings, fragment).commit()
        }
    }

    //Updatowanie Theme'a
    override fun onResume() {
        super.onResume()
        when(PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity).getString(getString(R.string.key_theme), "AppTheme")) {
            getString(R.string.theme_value_dark) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        delegate.applyDayNight()
    }
}

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        
        val themePreference = findPreference<ListPreference>(getString(R.string.key_theme))
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, _ ->
            activity?.recreate()
            return@OnPreferenceChangeListener true
        }
    }
}
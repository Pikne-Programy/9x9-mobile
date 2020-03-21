package com.gmail.miloszwasacz.tictactoe9x9

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //Ustawianie motywu
        setTheme(when(PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity).getString(getString(R.string.key_theme), "AppTheme")) {
                     getString(R.string.theme_dark) -> R.style.AppThemeDark
                     else -> R.style.AppTheme
                 })
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }

    //Tworzenie ustawień
    class SettingsFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val themePreference = findPreference<ListPreference>(getString(R.string.key_theme))
            themePreference?.summary = themePreference?.entries?.get(themePreference.findIndexOfValue(themePreference.value))
            themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, _ ->
                activity?.recreate()
                return@OnPreferenceChangeListener true
            }
        }
    }
}
package dev.abhattacharyea.safety.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dev.abhattacharyea.safety.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
    
    
    
}
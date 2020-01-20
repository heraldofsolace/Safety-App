package dev.abhattacharyea.safety.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import dev.abhattacharyea.safety.MainActivity
import dev.abhattacharyea.safety.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.startActivity

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    
        val aboutPreference = findPreference<Preference>("about")
        val dataPreference = findPreference<Preference>("data_collection_accepted")
        val logoutPreference = findPreference<Preference>("logout")
        aboutPreference?.setOnPreferenceClickListener {
            context?.let {
                LibsBuilder()
                    .withActivityStyle(Libs.ActivityStyle.LIGHT)
                    .start(it)
            
            }
            true
        }
    
        dataPreference?.setOnPreferenceClickListener {
            context?.let {
                it.alert(
                    "In order to provide the users with helpful service, the app requires" +
                            " the users to log in with email or Google account." +
                            " This data is stored securely and never disclosed to a 3rd party. " +
                            " This app does not collect or upload your contacts, or location",
                    "How we handle user data"
                ) {
                    positiveButton("Ok") {
                    
                    }
                
                }
            }
            true
        }
    
        logoutPreference?.setOnPreferenceClickListener {
            context?.let {
                it.alert("Are you sure you want to log out?", "Log Out") {
                    positiveButton("Yes") {
                        FirebaseAuth.getInstance().signOut()
                        startActivity<MainActivity>()
                        activity?.finish()
                    }
                
                    negativeButton("No") { }
                
                    show()
                }
            }
        
            true
        }
    }
    
    
    
}
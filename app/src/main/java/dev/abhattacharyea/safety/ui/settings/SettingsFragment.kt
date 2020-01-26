package dev.abhattacharyea.safety.ui.settings

import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import ch.derlin.changelog.Changelog
import ch.derlin.changelog.Changelog.getAppVersion
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import dev.abhattacharyea.safety.LockScreenService
import dev.abhattacharyea.safety.MainActivity
import dev.abhattacharyea.safety.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.support.v4.stopService
import org.jetbrains.anko.support.v4.toast

class SettingsFragment : PreferenceFragmentCompat() {
	var showContactNotificationPreference: SwitchPreference? = null
	var showAudioNotificationPreference: SwitchPreference? = null
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences, rootKey)
		
		val aboutPreference = findPreference<Preference>("about")
		val dataPreference = findPreference<Preference>("data_collection_accepted")
		val logoutPreference = findPreference<Preference>("logout")
		val tutorialPreference = findPreference<Preference>("tutorial")
		val changelogPreference = findPreference<Preference>("changelog")
		showContactNotificationPreference = findPreference("enable_contact_notification")
		showAudioNotificationPreference = findPreference("enable_audio_notification")
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
		
		tutorialPreference?.setOnPreferenceClickListener {
			PreferenceManager.getDefaultSharedPreferences(context).edit {
				putBoolean("tutorial_showed", false)
				toast("Tutorials will be shown again")
			}
			true
		}
		
		changelogPreference?.setOnPreferenceClickListener {
			activity?.let {
				val version = it.getAppVersion()
				val dialog = Changelog.createDialog(it, versionCode = version.first)
				dialog.show()
			}
			true
		}
		
		showContactNotificationPreference?.setOnPreferenceChangeListener { preference, newValue ->
			stopService<LockScreenService>()
//			newValue?.toString()?.let { toast(it) }
			if(newValue as? Boolean == true) {
				startService<LockScreenService>()
			} else {
				val showAudio = showAudioNotificationPreference?.isChecked ?: false
				if(showAudio) startService<LockScreenService>()
			}
			
			true
		}
		showAudioNotificationPreference?.setOnPreferenceChangeListener { preference, newValue ->
			stopService<LockScreenService>()
			if(newValue as? Boolean == true) {
				startService<LockScreenService>()
			} else {
				val showContact = showAudioNotificationPreference?.isChecked ?: false
				if(showContact) startService<LockScreenService>()
			}
			
			true
			
		}
	}
	
	
}
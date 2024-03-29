package dev.abhattacharyea.safety

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import ch.derlin.changelog.Changelog
import ch.derlin.changelog.Changelog.getAppVersion
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startService
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
	
	val RC_SIGN_IN = 100
	val permissionsList = arrayListOf(
		Manifest.permission.ACCESS_FINE_LOCATION to 1001,
		Manifest.permission.READ_CONTACTS to 1002,
		Manifest.permission.CALL_PHONE to 1003,
		Manifest.permission.SEND_SMS to 1004,
		Manifest.permission.READ_SMS to 1005,
		Manifest.permission.RECORD_AUDIO to 1006
	)
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		Log.d(TAG, requestCode.toString())
		if(requestCode == RC_SIGN_IN) {
			val response = IdpResponse.fromResultIntent(data)
			
			if(resultCode == Activity.RESULT_OK) {
				// Successfully signed in
				val user = FirebaseAuth.getInstance().currentUser
				toast("Welcome, ${user?.email}")
				// ...
			} else {
				
				toast("Sign in failed, some features might be unavailable")
				// Sign in failed. If response is null the user canceled the
				// sign-in flow using the back button. Otherwise check
				// response.getError().getErrorCode() and handle the error.
				// ...
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
		
	}
	
	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
			toast("Permission denied. This might cause unexpected behaviour")
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = "lock_screen"
			val descriptionText = "Test"
			val importance = NotificationManager.IMPORTANCE_HIGH
			val channel = NotificationChannel("lock_screen", name, importance).apply {
				description = descriptionText
			}
			// Register the channel with the system
			val notificationManager: NotificationManager =
				getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
		
		
		setContentView(R.layout.activity_main)
		val navView: BottomNavigationView = findViewById(R.id.nav_view)
		
		val navController = findNavController(R.id.nav_host_fragment)
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.navigation_contact,
				R.id.navigation_map,
				R.id.navigation_settings,
				R.id.navigation_donation
			)
		)
		setupActionBarWithNavController(navController, appBarConfiguration)
		navView.setupWithNavController(navController)
		
		val pref = getSharedPreferences("prefs", Context.MODE_PRIVATE)
		
		if(pref.getBoolean("first_run", true)) {
			startActivity<MainIntroActivity>()
			pref.edit().putBoolean("first_run", false).apply()
			finish()
		}
		
		if(FirebaseAuth.getInstance().currentUser == null) {
			val providers = arrayListOf(
				AuthUI.IdpConfig.EmailBuilder().build(),
				AuthUI.IdpConfig.GoogleBuilder().build()
			)
			
			// Create and launch sign-in intent
			startActivityForResult(
				AuthUI.getInstance()
					.createSignInIntentBuilder()
					.setAvailableProviders(providers)
					.build(),
				RC_SIGN_IN
			)
		}
		
		ActivityCompat.requestPermissions(
			this,
			permissionsList.map { it.first }.toTypedArray(),
			1000
		)
//		permissionsList.forEach {
//			if(ContextCompat.checkSelfPermission(this, it.first) != PackageManager.PERMISSION_GRANTED) {
//				ActivityCompat.requestPermissions(this, arrayOf(it.first), it.second)
//			}
//		}
		
		val defaultPref = PreferenceManager.getDefaultSharedPreferences(this)
		if(defaultPref.getString("preference_emergency_message", "") == "") {
			defaultPref.edit {
				putString("preference_emergency_message", "I might be in danger")
			}
		}
		MobileAds.initialize(this, "ca-app-pub-1922993119999106~6839490262")
		MobileAds.initialize(
			this
		) { }
		val mAdView = findViewById<AdView>(R.id.adView)
		val adRequest: AdRequest = AdRequest.Builder().build()
		mAdView.loadAd(adRequest)
		
		val version = getAppVersion()
		if(!defaultPref.getBoolean("tutorial_showed_for_version_${version.first}", false)) {
			val dialog = Changelog.createDialog(this, versionCode = version.first)
			dialog.setOnDismissListener {
				toast("You can view the changelog again from the settings")
			}
			
			dialog.show()
			defaultPref.edit {
				putBoolean("tutorial_showed_for_version_${version.first}", true)
			}
		}
		
		
		val showMainNotification = defaultPref.getBoolean("show_contact_notification", true)
		val showAudioNotification = defaultPref.getBoolean("show_audio_notification", true)
		
		if(showMainNotification || showAudioNotification)
			startService<LockScreenService>()
		
		
	}
	
	companion object {
		val TAG = MainActivity::class.java.simpleName
	}
}

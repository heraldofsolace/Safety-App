package dev.abhattacharyea.safety

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
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
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		
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


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true)
//        }
		setContentView(R.layout.activity_main)
		val navView: BottomNavigationView = findViewById(R.id.nav_view)
		
		val navController = findNavController(R.id.nav_host_fragment)
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.navigation_contact, R.id.navigation_map, R.id.navigation_settings
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
		
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
//            val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//            startActivityForResult(i, 100)
//        }
		
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
		
		startService<LockScreenService>()
	}
	
}

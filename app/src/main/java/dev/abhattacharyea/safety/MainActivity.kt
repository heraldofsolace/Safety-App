package dev.abhattacharyea.safety

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startService

class MainActivity : AppCompatActivity() {
	
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
				R.id.navigation_home, R.id.navigation_map, R.id.navigation_settings
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
		
		startService<LockScreenService>()
	}
	
}

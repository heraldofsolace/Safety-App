package dev.abhattacharyea.safety

import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dev.abhattacharyea.safety.ui.CallingDialog
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class LockScreenService: Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        
        
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenStateReceiver, screenFilter)

        val smsFilter = IntentFilter("dev.abhattacharyea.safety.sendsms")
        registerReceiver(smsreceiver, smsFilter)

        val callFilter = IntentFilter("dev.abhattacharyea.safety.call")
        registerReceiver(callReceiver, callFilter)
        
        val emergencyFilter = IntentFilter("dev.abhattacharyea.safety.emergency")
        registerReceiver(emergencyReceiver, emergencyFilter)


        val intent = Intent(this, CallingDialog::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
	
	    val intentSms = Intent("dev.abhattacharyea.safety.sendsms")
	    val intentCall = Intent("dev.abhattacharyea.safety.call")
	    val intentEmergency = Intent("dev.abhattacharyea.safety.emergency")
	
	    val pendingintentCall = PendingIntent.getBroadcast(this, 100, intentCall, 0)
	    val pendingintentSms = PendingIntent.getBroadcast(this, 1, intentSms, 0)
	    val pendingintentEmergency = PendingIntent.getBroadcast(this, 1, intentEmergency, 0)
        

        val builder = NotificationCompat.Builder(this, "lock_screen")
            .setSmallIcon(android.R.drawable.btn_plus)
	        .addAction(R.drawable.notification_call, "Call", pendingintentCall)
	        .addAction(R.drawable.notification_sms, "Sms", pendingintentSms)
	        .addAction(R.drawable.notification_emergency, "Emergency", pendingintentEmergency)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
            .setContentTitle("Safety")
            .setContentText("Use the buttons to call, message, or send SOS")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        startForeground(100,builder.build())
    }


    override fun onDestroy() {
        unregisterReceiver(screenStateReceiver)
        unregisterReceiver(smsreceiver)
        unregisterReceiver(callReceiver)
        unregisterReceiver(emergencyReceiver)
        super.onDestroy()
    }


    private val smsreceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val keyGuardManager = context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if(keyGuardManager.isDeviceLocked) {
                database.use {
                    select("Contacts", "number").orderBy("priority").limit(1).exec {
                        moveToFirst()
                        val number = getString(getColumnIndex("number"))
                        val smsManager = SmsManager.getDefault()
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            lastLocation = location
        
                            var msg =
                                PreferenceManager.getDefaultSharedPreferences(this@LockScreenService)
                                    .getString(
                                        "preference_emergency_message",
                                        "I might be in danger"
                                    )
                            if(location == null) {
                                msg += "\n My location could not be found"
                            } else {
                                msg += "\n My location is: ${location.latitude}, ${location.longitude}"
                            }
        
                            smsManager.sendTextMessage(number, null, msg, null, null)
        
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        1000,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            } else {
                                vibrator.vibrate(1000)
                            }
                        }
    
    
                    }
                }

            } else {
                startActivity(intentFor<CallingDialog>("call" to false).newTask())
                context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            }


        }
    }

    private val callReceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val keyGuardManager = context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if(keyGuardManager.isDeviceLocked) {
                database.use {
                    select("Contacts", "number").orderBy("priority").limit(1).exec {
                        moveToFirst()
                        val number = getString(getColumnIndex("number"))
                        val i = Intent(Intent.ACTION_CALL)
                        i.data = Uri.parse("tel:$number")
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        Log.d("SERVICE", "CAll")
                        startActivity(i)
                    }
                }

            } else {
                startActivity(intentFor<CallingDialog>("call" to true).newTask())
                context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            }

        }
    }

    private val emergencyReceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            database.use {
                select("Contacts").orderBy("priority").exec {
                    
                    val parser = classParser<Contact>()
                    val contacts = parseList(parser)
                    contacts.forEach {
                        val number = it.number
                        val smsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage(number, null, "Test", null, null)
    
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            vibrator.vibrate(1000)
                        }
                    }
                }
            }
        }
    }
    
    
    private val screenStateReceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("SERVICE", "Hi")
            intent?.let {
//                if(it.action == Intent.ACTION_SCREEN_ON) {
//
//                }
            }
        }
    }
}
package dev.abhattacharyea.safety

import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.github.piasy.rxandroidaudio.AudioRecorder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dev.abhattacharyea.safety.ui.CallingDialog
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import java.io.File
import java.util.*

class LockScreenService : Service() {
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private var lastLocation: Location? = null
	private var audioRecorder = AudioRecorder.getInstance()
	private var isRecording = false
	private var audioFileName: String? = null
	private lateinit var audioNotificationBuilder: NotificationCompat.Builder
	private lateinit var db: FirebaseFirestore
	private lateinit var storage: FirebaseStorage
	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
	
	override fun onCreate() {
		super.onCreate()
		
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		db = FirebaseFirestore.getInstance()
		storage = FirebaseStorage.getInstance()
		
		val smsFilter = IntentFilter("dev.abhattacharyea.safety.sendsms")
		registerReceiver(smsreceiver, smsFilter)
		
		val callFilter = IntentFilter("dev.abhattacharyea.safety.call")
		registerReceiver(callReceiver, callFilter)
		
		val emergencyFilter = IntentFilter("dev.abhattacharyea.safety.emergency")
		registerReceiver(emergencyReceiver, emergencyFilter)
		
		val audioRecordFilter = IntentFilter("dev.abhattacharyea.safety.record_audio")
		registerReceiver(audioReceiver, audioRecordFilter)
		
		val intent = Intent(this, CallingDialog::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK
		}
		
		val intentSms = Intent("dev.abhattacharyea.safety.sendsms")
		val intentCall = Intent("dev.abhattacharyea.safety.call")
		val intentEmergency = Intent("dev.abhattacharyea.safety.emergency")
		val intentRecordAudio = Intent("dev.abhattacharyea.safety.record_audio")
		
		val pendingintentCall = PendingIntent.getBroadcast(this, 100, intentCall, 0)
		val pendingintentSms = PendingIntent.getBroadcast(this, 101, intentSms, 0)
		val pendingintentEmergency = PendingIntent.getBroadcast(this, 102, intentEmergency, 0)
		val pendingintentAudio = PendingIntent.getBroadcast(this, 103, intentRecordAudio, 0)
		
		
		val builder = NotificationCompat.Builder(this, "lock_screen")
			.setSmallIcon(R.drawable.notification_emergency)
			.addAction(R.drawable.notification_call, "Call", pendingintentCall)
			.addAction(R.drawable.notification_sms, "Sms", pendingintentSms)
			.addAction(R.drawable.notification_emergency, "Emergency", pendingintentEmergency)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setStyle(
				androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(
					0,
					1,
					2
				)
			)
			.setContentTitle("Safety")
			.setContentText("Use the buttons to call, message, or send SOS")
			.setPriority(NotificationCompat.PRIORITY_MAX)
		
		audioNotificationBuilder = NotificationCompat.Builder(this, "lock_screen")
			.setSmallIcon(R.drawable.ic_audio)
			.addAction(R.drawable.ic_audio, "Record Audio", pendingintentAudio)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setStyle(
				androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(
					0
				)
			)
			.setContentTitle("Safety - Audio Record")
			.setContentText("Use the button to record the Audio and send a link to your contacts")
			.setPriority(NotificationCompat.PRIORITY_MAX)
			.setOngoing(true)
		
		val defaultPref = defaultSharedPreferences
		val showMainNotification = defaultPref.getBoolean("enable_contact_notification", true)
		val showAudioNotification = defaultPref.getBoolean("enable_audio_notification", true)
//		toast(showMainNotification.toString())
		if(showMainNotification) {
			startForeground(100, builder.build())
			if(showAudioNotification) {
				with(NotificationManagerCompat.from(this)) {
					notify(200, audioNotificationBuilder.build())
				}
			}
		} else {
			if(showAudioNotification)
				startForeground(100, audioNotificationBuilder.build())
			
		}
		
	}
	
	
	override fun onDestroy() {
		unregisterReceiver(smsreceiver)
		unregisterReceiver(callReceiver)
		unregisterReceiver(emergencyReceiver)
		with(NotificationManagerCompat.from(this)) {
			cancel(200)
		}
		stopForeground(true)
		super.onDestroy()
	}
	
	
	private val audioReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {

			
			if(isRecording) {
				val audioFile = File("$filesDir/$audioFileName")
				audioRecorder.stopRecord()
				audioNotificationBuilder
					.setContentText("Use the button to record the Audio and send a link to your contacts")
					.mActions[0].icon = R.drawable.ic_audio
				with(NotificationManagerCompat.from(this@LockScreenService)) {
					notify(200, audioNotificationBuilder.build())
				}
				
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
				audioFileName?.let {
					val uid = FirebaseAuth.getInstance().currentUser?.uid
					db.collection("audio_files")
						.add(
							hashMapOf(
								"uid" to uid,
								"filename" to audioFileName,
								"upload_time" to Calendar.getInstance().timeInMillis
							)
						).addOnSuccessListener { documentReference ->
							val fileId = documentReference.id
							Log.d(TAG, "Uploading file to $fileId")
							val storageRef =
								storage.reference.child("audio/$uid/$fileId-$audioFileName")
							val filePath = Uri.fromFile(audioFile)
							val uploadTask = storageRef.putFile(filePath)
							
							uploadTask.addOnSuccessListener {
								val downloadUri =
									"https://us-central1-safety-12.cloudfunctions.net/downloadFile?ref_id=$fileId"
								val intentEmergency =
									Intent("dev.abhattacharyea.safety.emergency").apply {
										putExtra(
											"extra_message",
											"\n I have sent an audio record which can be downloaded " +
													"at: ${downloadUri}. Keep this link safe as anyone with access to this link can " +
													"download. This link will expire after 1 hour"
										)
									}
								context?.let {
									Log.d(TAG, "Sending message with audio")
									it.sendBroadcast(intentEmergency)
									audioFile.delete()
								}
							}
						}.addOnFailureListener { exception ->
							exception.printStackTrace()
						}
				}
				isRecording = false
			} else {
				audioFileName = "Audio-${System.nanoTime()}.m4a"
				val audioFile = File("$filesDir/$audioFileName")
				audioRecorder.prepareRecord(
					MediaRecorder.AudioSource.MIC,
					MediaRecorder.OutputFormat.MPEG_4,
					MediaRecorder.AudioEncoder.AAC,
					192000,
					192000,
					audioFile
				)
				audioRecorder.startRecord()
				audioNotificationBuilder
					.setContentText("Recording Audio")
					.mActions[0].icon = R.drawable.ic_audio_off
				with(NotificationManagerCompat.from(this@LockScreenService)) {
					notify(200, audioNotificationBuilder.build())
				}
				with(NotificationManagerCompat.from(this@LockScreenService)) {
				
				}
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
				isRecording = true
			}
			
		}
	}
	
	private val smsreceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			
			val keyGuardManager =
				context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
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
							msg += if(location == null) {
								"\n My location could not be found"
							} else {
								"\n My location is: ${location.latitude}, ${location.longitude}"
							}
							Log.i(TAG, "Sending SMS to $number")
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
	
	private val callReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			
			val keyGuardManager =
				context?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
			
			if(keyGuardManager.isDeviceLocked) {
				database.use {
					select("Contacts", "number").orderBy("priority").limit(1).exec {
						moveToFirst()
						val number = getString(getColumnIndex("number"))
						val i = Intent(Intent.ACTION_CALL)
						i.data = Uri.parse("tel:$number")
						i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
						Log.i(TAG, "Calling $number")
						if(ContextCompat.checkSelfPermission(
								this@LockScreenService,
								android.Manifest.permission.CALL_PHONE
							)
							== PackageManager.PERMISSION_GRANTED
						)
							startActivity(i)
						else Log.e(TAG, "Permission not granted")
					}
				}
				
			} else {
				startActivity(intentFor<CallingDialog>("call" to true).newTask())
				context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
			}
			
		}
	}
	
	private val emergencyReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			database.use {
				select("Contacts").orderBy("priority").exec {
					
					val extraMessage = intent?.getStringExtra("extra_message")
					
					val parser = classParser<Contact>()
					val contacts = parseList(parser)
					contacts.forEach {
						val number = it.number
						val smsManager = SmsManager.getDefault()
						
						fusedLocationClient.lastLocation.addOnSuccessListener { location ->
							lastLocation = location
							
							var msg =
								PreferenceManager.getDefaultSharedPreferences(this@LockScreenService)
									.getString(
										"preference_emergency_message",
										"I might be in danger"
									)
							msg += if(location == null) {
								"\n My location could not be found"
							} else {
								"\n My location is: ${location.latitude}, ${location.longitude}"
							}
							
							extraMessage?.let { extra ->
								msg += extra
							}
							
							val msgList = smsManager.divideMessage(msg)
							Log.i(TAG, "Sending emergency message to $number")
							
							smsManager.sendMultipartTextMessage(number, null, msgList, null, null)
							
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
			}
		}
	}
	
	
	companion object {
		val TAG = LockScreenService::class.java.simpleName
	}
}
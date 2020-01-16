package dev.abhattacharyea.safety

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPage
import org.jetbrains.anko.startActivity

class MainIntroActivity : AppIntro2() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val sliderPage1 = SliderPage().apply {
			title = "Your safety matters"
			description = "Stay safe, wherever you are"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorAccent
			
		}
		
		val sliderPage2 = SliderPage().apply {
			title = "Permissions needed"
			description = "Location - for alerting your contacts about where you are"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorPrimary
		}
		val sliderPage3 = SliderPage().apply {
			title = "Permissions needed"
			description = "Contacts - for making sure your alert gets delivered to the ones you trust"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorAccent
		}
		val sliderPage4 = SliderPage().apply {
			title = "Permissions needed"
			description = "Phone & SMS - for sending out alert when you need it"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorPrimary
		}
		
		val sliderPage5 = SliderPage().apply {
			title = "Features"
			description =
				"Select a few of your trusted contacts and call them or send SOS with one click"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorAccent
		}
		
		val sliderPage6 = SliderPage().apply {
			title = "Features"
			description =
				"The very first contact is a \"Super contact\". When the screen is turned off, " +
						"the call or SMS will be sent only to this contact directly. The SOS will always be" +
						" sent to every trusted contact"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorPrimary
		}
		
		val sliderPage7 = SliderPage().apply {
			title = "How we handle user data"
			description = "In order to provide the users with helpful service, the app requires" +
					" the users to log in with email or Google account. " +
					"This data is stored securely and never disclosed to a 3rd party. " +
					"This app does not collect or upload your contacts, or location"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorAccent
		}
		
		
		addSlide(AppIntro2Fragment.newInstance(sliderPage1))
		addSlide(AppIntro2Fragment.newInstance(sliderPage2))
		addSlide(AppIntro2Fragment.newInstance(sliderPage3))
		addSlide(AppIntro2Fragment.newInstance(sliderPage4))
//		addSlide(AppIntro2Fragment.newInstance(sliderPage5))
//		addSlide(AppIntro2Fragment.newInstance(sliderPage6))
//		addSlide(AppIntro2Fragment.newInstance(sliderPage7))
		
		askForPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
		askForPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 3)
		askForPermissions(
			arrayOf(
				Manifest.permission.CALL_PHONE,
				Manifest.permission.SEND_SMS,
				Manifest.permission.READ_SMS
			), 4
		)


//		askForPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
//			Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS), 4)
	}
	
	
	override fun onDonePressed(currentFragment: Fragment?) {
		super.onDonePressed(currentFragment)
		getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("firstrun", false).apply()
		startActivity<MainActivity>()
		finish()
	}
}
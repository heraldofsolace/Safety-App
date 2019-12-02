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
		val sliderPage = SliderPage().apply {
			title = "Your safety matters"
			description = "Features"
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
			bgColor = R.color.colorPrimary
		}
		val sliderPage4 = SliderPage().apply {
			title = "Permissions needed"
			description = "Phone & SMS - for sending out alert when you need it"
			imageDrawable = R.mipmap.ic_launcher
			bgColor = R.color.colorPrimary
		}
		addSlide(AppIntro2Fragment.newInstance(sliderPage))
		addSlide(AppIntro2Fragment.newInstance(sliderPage2))
		addSlide(AppIntro2Fragment.newInstance(sliderPage3))
		addSlide(AppIntro2Fragment.newInstance(sliderPage4))
		askForPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS), 1)
	}
	
	
	override fun onDonePressed(currentFragment: Fragment?) {
		super.onDonePressed(currentFragment)
		getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("firstrun", false).apply()
		startActivity<MainActivity>()
		finish()
	}
}
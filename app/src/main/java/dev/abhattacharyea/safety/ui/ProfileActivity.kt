package dev.abhattacharyea.safety.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cesarferreira.faker.loadFromUrl
import cesarferreira.faker.loadRandomImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dev.abhattacharyea.safety.R
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
	var user: FirebaseUser? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_profile)
		
		user = FirebaseAuth.getInstance().currentUser
		user?.let {
			profile_name.text = it.displayName ?: "Tap to set name"
			profile_email.text = it.email ?: "Email not found!!"
			
			profile_avatar.apply {
				if(it.photoUrl == null) loadRandomImage()
				else loadFromUrl(it.photoUrl.toString())
			}
		}
	}
}
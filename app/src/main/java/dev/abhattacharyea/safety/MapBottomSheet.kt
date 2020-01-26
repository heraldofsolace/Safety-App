package dev.abhattacharyea.safety

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapBottomSheet(
	val marker: Marker,
	val userId: String?,
	var photoReference: String? = null,
	var openNow: Boolean? = false
) : BottomSheetDialogFragment() {
	
	lateinit var getDirectionsButton: FloatingActionButton
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		
		val v = LayoutInflater.from(context).inflate(R.layout.map_bottom_sheet, container, false)
		val handler = Handler()
		v.findViewById<TextView>(R.id.place_name).text = marker.title
		val placesImage = v.findViewById<ImageView>(R.id.places_image)
		getDirectionsButton = v.findViewById(R.id.map_sheet_get_direction)
		getDirectionsButton.setOnClickListener {
			
			val i = Intent("dev.abhattacharyea.safety.showDirections")
			context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(i) }
			dismiss()
		}
		Log.d("BOTTOM", photoReference.toString())
		
		openNow?.let {
			v.findViewById<TextView>(R.id.place_open_now)?.text = if(it) "Open Now" else "Closed"
			
		}
		
		photoReference?.let {
			"https://us-central1-safety-12.cloudfunctions.net/photo?maxwidth=4000&photoreference=${it}"
				.httpGet()
				.header("Authorization", "Bearer $userId")
				.response { _, _, result ->
					when(result) {
						is Result.Failure -> {
							result.getException().printStackTrace()
						}
						
						is Result.Success -> {
							val data = result.get()
							val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
							handler.post {
								placesImage.setImageBitmap(bmp)
								
							}
							
						}
					}
				}
			
		}
		
		return v
	}
	
	
}
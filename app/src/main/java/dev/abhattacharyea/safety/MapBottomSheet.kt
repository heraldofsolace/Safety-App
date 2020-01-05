package dev.abhattacharyea.safety

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.abhattacharyea.safety.api.RetrofitClient
import dev.abhattacharyea.safety.model.Directions

import dev.abhattacharyea.safety.model.Route
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapBottomSheet(
	val marker: Marker,
	val currentLocation: Location,
	val mapsController: MapsController
) : BottomSheetDialogFragment() {
	
	lateinit var getDirectionsButton: FloatingActionButton
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val v = inflater.inflate(R.layout.map_bottom_sheet, container, false)
		
		v.findViewById<TextView>(R.id.place_name).text = marker.title
		getDirectionsButton = v.findViewById(R.id.map_sheet_get_direction)
		getDirectionsButton.setOnClickListener {
			
			
			val directionsCall =
				RetrofitClient.googleMethods().getDirections(
					"${currentLocation.latitude},${currentLocation.longitude}",
					"${marker.position?.latitude},${marker.position?.longitude}",
					"AIzaSyAGl2-QS5tQjmvi2NvzOLZUpq3Yuon506A"
				)
			directionsCall.enqueue(object : Callback<Directions> {
				override fun onResponse(call: Call<Directions>, response: Response<Directions>) {
					val directions = response.body()!!
					
					if(directions.status == "OK") {
						val legs = directions.routes[0].legs[0]
						val route = Route(
							"Current location",
							marker.title,
							legs.startLocation.lat,
							legs.startLocation.lng,
							legs.endLocation.lat,
							legs.endLocation.lng,
							directions.routes[0].overviewPolyline.points
						)
						mapsController.clearMarkers()
						mapsController.setMarkersAndRoute(route)
					} else {
						toast(directions.status)
						
					}
					
					
				}
				
				override fun onFailure(call: Call<Directions>, t: Throwable) {
					toast(t.toString())
					
				}
			})
		}
		return v
	}
	
	
}
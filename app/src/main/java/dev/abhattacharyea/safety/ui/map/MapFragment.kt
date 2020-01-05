package dev.abhattacharyea.safety.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.net.PlacesClient
import dev.abhattacharyea.safety.MapBottomSheet
import dev.abhattacharyea.safety.MapsController
import dev.abhattacharyea.safety.R
import dev.abhattacharyea.safety.Utility
import dev.abhattacharyea.safety.api.RetrofitClient
import dev.abhattacharyea.safety.model.NearbySearch
import dev.abhattacharyea.safety.model.Spot
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//TODO: Need to improve map and add nearby places

class MapFragment : OnMapReadyCallback, CompoundButton.OnCheckedChangeListener,
    GoogleMap.OnMarkerClickListener, Fragment() {
 
 
	override fun onStop() {
		client.removeLocationUpdates(locationCallback)
		super.onStop()
	}
    
    lateinit var map: GoogleMap
    lateinit var mapsController: MapsController
    private lateinit var client: FusedLocationProviderClient
    var location: Location? = null
    private var firstLocation = true
    lateinit var placesClient: PlacesClient
    lateinit var policeToggleButton: ToggleButton
    lateinit var atmToggleButton: ToggleButton
    lateinit var hospitalToggleButton: ToggleButton
    
    var markersList = ArrayList<Marker>()
    var currentMarker: Marker? = null
    lateinit var bottomSheet: MapBottomSheet
    
    
    private fun searchForPlace(type: String, toggleButton: ToggleButton) {
        val position =
            map.cameraPosition.target.latitude.toString() + "," + map.cameraPosition.target.longitude.toString()
        Log.d("LOCCCC", position)
        val placesCall = RetrofitClient.googleMethods()
            .getNearbySearch(position, "1000", type, "AIzaSyAGl2-QS5tQjmvi2NvzOLZUpq3Yuon506A")
        placesCall.enqueue(object : Callback<NearbySearch> {
            override fun onResponse(call: Call<NearbySearch>, response: Response<NearbySearch>) {
                val nearbySearch = response.body()!!
                
                if(nearbySearch.status == "OK") {
                    val spotList = ArrayList<Spot>()
                    
                    for(resultItem in nearbySearch.results!!) {
                        val spot = Spot(
                            resultItem.name,
                            resultItem.geometry.location?.lat,
                            resultItem.geometry.location?.lng,
                            resultItem.icon
                        )
                        spotList.add(spot)
                    }
    
                    markersList = mapsController.setMarkersAndZoom(spotList)
                } else {
                    toast(nearbySearch.status)
                    Log.d("LOCCCC", nearbySearch.toString())
                    toggleButton.isChecked = false
                }
                
                
            }
            
            override fun onFailure(call: Call<NearbySearch>, t: Throwable) {
                toast(t.toString())
                Log.d("LOCCCC", t.toString())
                toggleButton.isChecked = false
            }
        })
    }
    
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerClickListener(this)
    
        mapsController = MapsController(context!!, map)
        if(location != null) {
            if(firstLocation)
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location!!.latitude,
                        location!!.longitude
                    ), 5F
                )
            )
            else {
            }
//                map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
//                    location!!.latitude,
//                    location!!.longitude
//                )))
        }
    
        policeToggleButton.setOnCheckedChangeListener(this)
    
        atmToggleButton.setOnCheckedChangeListener(this)
    
        hospitalToggleButton.setOnCheckedChangeListener(this)
    
    
        // map.addMarker(MarkerOptions().position(kolkata).title("Location"))

        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            toast("Location permission not granted. Can't proceed")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        requestLocationUpdates()
        val app = context!!.packageManager.getApplicationInfo(context!!.packageName, PackageManager.GET_META_DATA)
        val bundle = app.metaData
        policeToggleButton = root.findViewById(R.id.policeToggleButton)
        atmToggleButton = root.findViewById(R.id.atmToggleButton)
        hospitalToggleButton = root.findViewById(R.id.hospitalToggleButton)

//        Places.initialize(context!!, bundle.getString("com.google.android.geo.API_KEY")!!)
//        placesClient = Places.createClient(context!!)
        return root
    }

    private fun requestLocationUpdates() {


        val request = LocationRequest()


        request.interval = 1000 * 20


        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        client = LocationServices.getFusedLocationProviderClient(context!!)


        val permission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )


        if (permission == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(request, locationCallback, null)
        }
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            Log.d("LOC", locationResult!!.lastLocation.latitude.toString())
            Log.d("LOC", "Location")

            location = locationResult.lastLocation
            if(location != null) {
                if(firstLocation)
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location!!.latitude,
                                location!!.longitude
                            ), 100F
                        )
                    )
//                else
//                    map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
//                        location!!.latitude,
//                        location!!.longitude
//                    )))
                firstLocation = false

            }
        }
    }
    
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) {
            val type = when(buttonView?.id) {
                R.id.policeToggleButton -> Utility.TYPE_POLICE
                R.id.atmToggleButton -> Utility.TYPE_ATM
                R.id.hospitalToggleButton -> Utility.TYPE_HOSPITAL
                else -> Utility.TYPE_POLICE
            }
            searchForPlace(type, buttonView as ToggleButton)
        } else {
            mapsController.clearMarkers()
        }
    }
    
    override fun onMarkerClick(p0: Marker?): Boolean {
        p0?.let {
            bottomSheet = MapBottomSheet(p0, location!!, mapsController)
        
            currentMarker = p0
        
            bottomSheet.show(fragmentManager!!, "Bottom")
        }

//        bottomSheet.onDismiss(object :DialogInterface {
//            override fun dismiss() {
//                toast("Dismiss")
//            }
//
//            override fun cancel() {
//                toast("Cancel")
//            }
//        })
        return true
    }
    
    
}
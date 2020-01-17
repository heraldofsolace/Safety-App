package dev.abhattacharyea.safety.ui.map

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import dev.abhattacharyea.safety.*
import dev.abhattacharyea.safety.R
import dev.abhattacharyea.safety.api.RetrofitClient
import dev.abhattacharyea.safety.model.Directions
import dev.abhattacharyea.safety.model.NearbySearch
import dev.abhattacharyea.safety.model.Route
import dev.abhattacharyea.safety.model.Spot
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
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
    lateinit var policeToggleButton: ToggleButton
    lateinit var atmToggleButton: ToggleButton
    lateinit var hospitalToggleButton: ToggleButton
    
    private lateinit var spotList: ArrayList<Spot>
    var markersList = ArrayList<Marker>()
    var currentMarker: Marker? = null
    lateinit var bottomSheet: MapBottomSheet
    
    
    private fun searchForPlace(type: String, toggleButton: ToggleButton) {
        val position =
            map.cameraPosition.target.latitude.toString() + "," + map.cameraPosition.target.longitude.toString()
        Log.d(TAG, position)
        val placesCall = RetrofitClient.googleMethods()
            .getNearbySearch(position, "1000", type, Constants.API_KEY)
        placesCall.enqueue(object : Callback<NearbySearch> {
            override fun onResponse(call: Call<NearbySearch>, response: Response<NearbySearch>) {
                val nearbySearch = response.body()!!
                
                if(nearbySearch.status == "OK") {
                    spotList = ArrayList()
                    
                    for(resultItem in nearbySearch.results!!) {
                        val spot = Spot(
                            resultItem.name,
                            resultItem.geometry.location?.lat,
                            resultItem.geometry.location?.lng,
                            resultItem.icon,
                            resultItem.photos?.get(0)?.photoReference
                        )
                        Log.d("PHOTOS", resultItem.photos.toString())
                        spotList.add(spot)
                    }
    
                    markersList = mapsController.setMarkersAndZoom(spotList)
                } else {
                    toast(nearbySearch.status)
                    Log.d(TAG, nearbySearch.toString())
                    toggleButton.isChecked = false
                }
                
                
            }
            
            override fun onFailure(call: Call<NearbySearch>, t: Throwable) {
                toast(t.toString())
                Log.d(TAG, t.toString())
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
                    ), 1F
                )
            )
    
        }
    
        policeToggleButton.setOnCheckedChangeListener(this)
    
        atmToggleButton.setOnCheckedChangeListener(this)
    
        hospitalToggleButton.setOnCheckedChangeListener(this)
    
        LocalBroadcastManager.getInstance(context!!).registerReceiver(
            directionsReceiver, IntentFilter("dev.abhattacharyea.safety.showDirections")
        )
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
            Log.d(TAG, locationResult!!.lastLocation.latitude.toString())
            Log.d(TAG, "Location")

            location = locationResult.lastLocation
            if(location != null) {
                if(firstLocation)
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location!!.latitude,
                                location!!.longitude
                            ), 20F
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
        mapsController.clearMarkers()
        mapsController.clearMarkersAndRoute()
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
            mapsController.clearMarkersAndRoute()
        }
    }
    
    override fun onMarkerClick(p0: Marker?): Boolean {
        p0?.let {
            val spot = spotList.find {
                it.lat == p0.position.latitude && it.lng == p0.position.longitude
            }
            bottomSheet = MapBottomSheet(p0, location!!, mapsController, spot?.photoReference)
        
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
    
    
    private val directionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            hospitalToggleButton.isChecked = false
            policeToggleButton.isChecked = false
            atmToggleButton.isChecked = false
            val directionsCall =
                RetrofitClient.googleMethods().getDirections(
                    "${location?.latitude},${location?.longitude}",
                    "${currentMarker?.position?.latitude},${currentMarker?.position?.longitude}",
                    Constants.API_KEY
                )
            directionsCall.enqueue(object : Callback<Directions> {
                override fun onResponse(call: Call<Directions>, response: Response<Directions>) {
                    val directions = response.body()!!
                    
                    if(directions.status == "OK") {
                        val legs = directions.routes[0].legs[0]
                        val route = Route(
                            "Current location",
                            currentMarker?.title.toString(),
                            legs.startLocation.lat,
                            legs.startLocation.lng,
                            legs.endLocation.lat,
                            legs.endLocation.lng,
                            directions.routes[0].overviewPolyline.points
                        )
                        mapsController.clearMarkers()
                        mapsController.setMarkersAndRoute(route)
                    } else {
                        context?.toast(directions.status)
                        
                    }
                    
                }
                
                override fun onFailure(call: Call<Directions>, t: Throwable) {
                    toast(t.toString())
                    
                }
            })
        }
    }
    
    companion object {
        val TAG = MapFragment::class.java.simpleName
    }
}
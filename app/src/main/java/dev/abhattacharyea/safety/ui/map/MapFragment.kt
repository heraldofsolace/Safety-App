package dev.abhattacharyea.safety.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dev.abhattacharyea.safety.R
import org.jetbrains.anko.support.v4.toast

class MapFragment : OnMapReadyCallback, Fragment() {

    lateinit var map: GoogleMap
    private lateinit var client: FusedLocationProviderClient
    var location: Location? = null
    private var firstLocation = true
    lateinit var placesClient: PlacesClient
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if(location != null) {
            if(firstLocation)
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location!!.latitude,
                        location!!.longitude
                    ), 10F
                )
            )
            else
                map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
                    location!!.latitude,
                    location!!.longitude
                )))
        }

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
        Places.initialize(context!!, bundle.getString("com.google.android.geo.API_KEY")!!)
        placesClient = Places.createClient(context!!)
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
                else
                    map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
                        location!!.latitude,
                        location!!.longitude
                    )))
                firstLocation = false

            }
        }
    }

}
package com.raywenderlich.placebook.ui

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest                                                                             //Use instead of import java.util.jar.Manifest
import android.graphics.Bitmap
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
//pg 284
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {                                      //Inherits AppCompatActivity and onMapReadyCallback interfaces

    private lateinit var map: GoogleMap
    //pg 284
    private lateinit var placesClient: PlacesClient
    //pg 262
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //pg 265
    companion object {
        private const val REQUEST_LOCATION = 1                                                      //Request code that'll be passed to requestPermissions(). Identifies the specific permission request when returned by Android
        private const val TAG = "MapsActivity"                                                      //Passed to Log.e method
    }

    //pg 332 (pdf)
    private val mapsViewModel by viewModels<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)                                                      //Load activity_maps.xml which contains container for SupportMapFragment
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment                                   //Find the map Fragment from layout
        mapFragment.getMapAsync(this)                                                               //Use it to initialize map
        //pg 262
        setupLocationClient()
        setupPlacesClient()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {                                                 //Part of OnMapReadyCallback interface. Will be called by SupportMapFragment when getMapAsync() has finished
        map = googleMap
        setupMapListeners()
        getCurrentLocation()
        createBookMarkerObserver()
    }

    //pg 284
    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key));                 //Create a Places client
        placesClient = Places.createClient(this)
    }

    //pg 262
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    //pg 264
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,                                             //Pass the current activity as the context.
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
        )                    //Pass an array of requested permissions and a request code to identify the request
    }

    //pg 265
    private fun getCurrentLocation() {                                                              //Gets the user's location and moves the map so it centers the location
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {       //Check to see if the ACCESS_FINE_LOCATION permission was granted before moving forward
            requestLocationPermissions()                                                            //If it wasn't, request permission again
        } else {
            //pg 275
            map.isMyLocationEnabled = true                                                          //See text for details on .isMyLocationEnabled property
            //pg 265
            fusedLocationClient.lastLocation.addOnCompleteListener {                                //Request a notification when the location is ready using addOnCompleteListener
                val location = it.result                                                            //Represents a Location object containing last known location
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)                      //Create a LatLng object used for storing latitude and longitude
                    map.addMarker(MarkerOptions().position(latLng).title("You are here!"))
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)                   //Use .newLatLngZoom property to update camera Target and Zoom
                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")                                            //If location == null log an error code
                }
            }
        }
    }

    //pg 267
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location Permission Denied")
            }
        }
    }

    //pg 285
    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId =
            pointOfInterest.placeId                                                                 //Retrieve placeId which uniquely identifies a POI

        val placeFields = listOf(
            Place.Field.ID,                                                                         //Create a field mask with the attributes of a place
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields)
            .build()                                                                                //Create a fetch request using placeId and  placeFields

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->                                                     //use placesClient to fetch place details
                val place =
                    response.place                                                                  //Success listener called if the response is successfully receives
                displayPoiGetPhotoStep(place)
            }
            .addOnFailureListener { exception ->                                                    //Failure listener in case the request fails which includes exception handling
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG, "Place not found: " +
                                exception.message + ", " + "statusCode: " + statusCode
                    )
                }
            }
    }

    //pg 288
    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.getPhotoMetadatas()?.get(0)                                          //Get the retrieved PhotoMetaData object for the selected place
        if (photoMetadata == null) {                                                                //If there is no PhotoMetaData object skip to the next step
            displayPoiDisplayStep(place, null)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)                                 //Set up the builder for the photoMetaData
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))              //Pass width
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))                  //Pass height
            .build()

        placesClient.fetchPhoto(photoRequest)                                                       //
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                displayPoiDisplayStep(place, bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                    "Place not found: " + exception.message +
                            ", " + "statusCode: " + statusCode)
                }
            }
    }

    //pg 292
    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {

        //pg 298
        val marker = map.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber)
        )
        //pg 333 (pdf)
        marker?.tag = PlaceInfo(place, photo)                                                       //Marker tag holds
    }

    //pg 332 (pdf)
    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener{ displayPoi(it) }

        //pg 334 (pdf)
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }                              //Set a listener to call handleInfoWindowClick() whenever user taps info window
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)

    //pg 334 (pdf)
    private fun handleInfoWindowClick(marker: Marker) {                                             //Handles taps on a place info window.
        val placeInfo = (marker.tag as PlaceInfo)                                                   //Get the placeInfo from the marker.tag
        if (placeInfo.place != null) {
            GlobalScope.launch {
                mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
            }
        }
        marker.remove()                                                                             //Remove marker from the map
    }

    //pg 340 (pdf)
    private fun addPlaceMarker(                                                                     //Hlper method that adds a single blue marker to the map based on a BookmarkMarkerView
        bookmark: MapsViewModel.BookmarkMarkerView): Marker? {

        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .alpha(0.8f))

        marker.tag = bookmark

        return marker
    }

    //pg 340 (pdf)
    private fun displayAllBookmarks(                                                                //Walks through a list of BookmarkMarkerView objects and calls addPlaceMarker() for each one
        bookmarks: List<MapsViewModel.BookmarkMarkerView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    //pg 341 (pdf)
    private fun createBookMarkerObserver() {                                                        //Observes the changes to the BookmarkMarkerView objects from the MapsViewModel and updates the View when they change
        mapsViewModel.getBookmarkMarkerViews()?.observe(
            this, Observer<List<MapsViewModel.BookmarkMarkerView>> {                         //
                map.clear()                                                                         //Once update data is received clear existing markers on the map
                it?.let {                                                                           //
                    displayAllBookmarks(it)
                }
            }
        )
    }
}
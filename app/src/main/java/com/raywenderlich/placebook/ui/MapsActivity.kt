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
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
//pg 284
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.adapter.BookmarkListAdapter
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import dagger.internal.ProviderOfLazy
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.drawer_view_maps.*
import kotlinx.android.synthetic.main.main_view_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {                                      //Inherits AppCompatActivity and onMapReadyCallback interfaces

    private lateinit var map: GoogleMap
    //pg 284
    private lateinit var placesClient: PlacesClient
    //pg 262
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //pg 390 (pdf)
    private lateinit var bookmarkListAdapter: BookmarkListAdapter
    //pg 381 (pdf)
    private var markers = HashMap<Long, Marker>()                                                   //HashMap used to map a bookmark ID to a marker

    companion object {
        //pg 366(pdf)
        const val EXTRA_BOOKMARK_ID = "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"               //Defines a key for storing the bookmark ID in the intent extras
        //pg 265
        private const val REQUEST_LOCATION = 1                                                      //Request code that'll be passed to requestPermissions(). Identifies the specific permission request when returned by Android
        private const val TAG = "MapsActivity"                                                      //Passed to Log.e method
        //pg 425 (pdf)
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
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
        setupToolbar()
        setupNavigationDrawer()
    }

    override fun onMapReady(googleMap: GoogleMap) {                                                 //Part of OnMapReadyCallback interface. Will be called by SupportMapFragment when getMapAsync() has finished
        map = googleMap
        setupMapListeners()
        getCurrentLocation()
        createBookmarkObserver()
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
        )                                                                                           //Pass an array of requested permissions and a request code to identify the request
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
        showProgress()
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
            Place.Field.LAT_LNG,
            Place.Field.TYPES
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
                    hideProgress()
                }
            }
    }

    //pg 288
    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.getPhotoMetadatas()?.get(0)                                       //Get the retrieved PhotoMetaData object for the selected place
        if (photoMetadata == null) {                                                                //If there is no PhotoMetaData object skip to the next step
            displayPoiDisplayStep(place, null)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)                                 //Set up the builder for the photoMetaData
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))              //Pass width
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))                  //Pass height
            .build()

        placesClient.fetchPhoto(photoRequest)
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
        hideProgress()
    }

    //pg 292
    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {

        hideProgress()
        //pg 298
        val marker = map.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber)
        )
        //pg 333 (pdf)
        marker?.tag = PlaceInfo(place, photo)                                                       //Marker tag holds

        //pg 349 (pdf)
        marker?.showInfoWindow()                                                                    //Instructs the map to display the Info window for the marker
    }

    //pg 332 (pdf)
    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener{ displayPoi(it) }

        //pg 334 (pdf)
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }                              //Set a listener to call handleInfoWindowClick() whenever user taps info window
        //pg 428 (pdf)
        fab.setOnClickListener {
            searchAtCurrentLocation()
        }

        //pg 429 (pdf)
        map.setOnMapLongClickListener { latLng ->                                                   //Listens for  long click on the map at a given location
            newBookmark(latLng)
        }
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)

    //pg 334 (pdf)
    private fun handleInfoWindowClick(marker: Marker) {                                             //Handles taps on a place info window.
        when (marker.tag) {
            is MapsActivity.PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
                    }
                }
                marker.remove();
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkMarkerView = (marker.tag as MapsViewModel.BookmarkView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    //pg 340 (pdf)
    private fun addPlaceMarker(                                                                     //Hlper method that adds a single blue marker to the map based on a BookmarkMarkerView
        bookmark: MapsViewModel.BookmarkView): Marker? {

        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .title(bookmark.name)
            .snippet(bookmark.phone)
            .icon(bookmark.categoryResourceId?.let {
                BitmapDescriptorFactory.fromResource(it)
            })
            .alpha(0.8f))

        marker.tag = bookmark
        bookmark.id?.let { markers.put(it, marker) }                                                //Adds a new entry to markers when a new marker is added to the map

        return marker
    }

    //pg 340 (pdf)
    private fun displayAllBookmarks(                                                                //Walks through a list of BookmarkMarkerView objects and calls addPlaceMarker() for each one
        bookmarks: List<MapsViewModel.BookmarkView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    //pg 341 (pdf)
    private fun createBookmarkObserver() {                                                        //Observes the changes to the BookmarkMarkerView objects from the MapsViewModel and updates the View when they change
        mapsViewModel.getBookmarkViews()?.observe(
            this, Observer<List<MapsViewModel.BookmarkView>> {
                map.clear()                                                                         //Once update data is received clear existing markers on the map
                markers.clear()                                                                     //Clears markers when bookmark data changes
                it?.let {
                    displayAllBookmarks(it)
                    //pg 390 (pdf)
                    bookmarkListAdapter.setBookmarkData(it)                                         //Sets new list of BookmarkView items on the recycler whenever the bookmark data changes
                }
            }
        )
    }

    //pg 363 (pdf)
    private fun startBookmarkDetails(bookmarkId: Long) {                                            //Used to start the BookmarkDetailsActivity using an explicit intent. Will be called when the user taps on an info window
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)                                              //Adds bookmarkId as an extra parameter on the Intent
        startActivity(intent)
    }

    //pg 384 (pdf)
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        //pg 385 (pdf)
        val toggle = ActionBarDrawerToggle(                                                         //Manages the drawerLayout and Toolbar functionality
            this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        toggle.syncState()
    }

    //pg 390 (pdf)
    private fun setupNavigationDrawer() {                                                           //Sets up the adapter for the bookmark recycler view
        val layoutManager = LinearLayoutManager(this)
        bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    //pg 392 (pdf)
    private fun updateMapToLocation(location: Location) {                                           //Pans and zooms the camera to center over a selected location
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    //pg 392 (pdf)
    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView) {
        drawerLayout.closeDrawer(drawerView)                                                        //Navigation drawer is closed before zooming
        val marker = markers[bookmark.id]                                                           //Markers HashMap is used to look up the Marker
        marker?.showInfoWindow()                                                                    //If the marker is found, the info window is shown
        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    //PG 425 (pdf)
    private fun searchAtCurrentLocation() {

        val placeFields = listOf(                                                                   //Define fields which inform Autocomplete widget what attributes to return fo each search
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.TYPES)

        val bounds = RectangularBounds.newInstance(map.projection.visibleRegion.latLngBounds)       //Compute the bounds of the currently visible region
        try {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)  //IntentBuilder used o build Actvity's intent. .OVERLAY allows Activity to overlay current Activity
                .setLocationBias(bounds)                                                            //Tells search widget to prioritize places in the current window
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)                               //Start Activity
        } catch (e: GooglePlayServicesRepairableException) {
            //Handle exception
        } catch (e: GooglePlayServicesNotAvailableException) {
            //handle exception
        }
    }

    //pg 426 (pdf)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {               //Called by Android when the user completes a search

        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK && data != null) {                             //Make sure the request code is valid
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?: 0.0
                    location.longitude = place.latLng?.longitude ?:0.0
                    updateMapToLocation(location)
                    showProgress()
                    displayPoiGetPhotoStep(place)
                }
        }
    }

    //pg 429 (pdf)
    private fun newBookmark(latLng: LatLng) {                                                       //Creates a nnew bookmark from a location and starts the bookmarkDetailsActivity to allow user editing
        GlobalScope.launch {
            val bookmarkId = mapsViewModel.addBookmark(latLng)
            bookmarkId?.let {
                startBookmarkDetails(it)
            }
        }
    }

    //pg 440 (pdf)
    private fun disableUserInteraction() {                                                          //Sets a flag on the main window to prevent user touches
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun enableUserInteraction() {                                                           //Clears the flag set by disableUserInteraction()
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgress() {
        progressBar.visibility = ProgressBar.VISIBLE
        disableUserInteraction()
    }

    private fun hideProgress() {
        progressBar.visibility = ProgressBar.GONE
        enableUserInteraction()
    }
}
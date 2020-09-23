package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

//pg 316
// ViewModel layer is used to interact between View and the data provided by BookMarkRepo.

class MapsViewModel(application: Application) : AndroidViewModel(application) {                     //When creating a ViewModel, must inherit from ViewModel or AndroidViewModel

    private val TAG = "MapsViewModel"
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())                         //Create a BookmarkRepo object

    //pg 338 (pdf)
    private var bookmarks: LiveData<List<BookmarkView>>? = null                                     //LiveData object that wraps a list of BookmarkMarkerView objects

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {                                        //Takes a Google Place and a Bitmap image to create a bookmark
        val bookmark = bookmarkRepo.createBookmark()                                                //Used to create a empty Bookmark object. Fill the object with Place data
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()
        bookmark.category = getPlaceCategory(place)                                                 //Assigns a category to the new bookmark

        val newId = bookmarkRepo.addBookmark(bookmark)                                              //Save to repository

        //pg 349 (pdf)
        image?.let { bookmark.setImage(it, getApplication()) }                                      //Calls setImage() if the image != null

        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    //pg 338 (pdf)
    data class BookmarkView(                                                                        //Holds info by the View to plot a marker for a single bookmark
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0),
        var name: String = "",
        var phone: String = "",
        val categoryResourceId: Int? = null)
    //pg 351 (pdf)
    {
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFilename(it))
            }
            return null
        }
    }

    //pg 338 (pdf)
    private fun bookmarkToBookmarkView(bookmark: Bookmark) : MapsViewModel.BookmarkView {           //Helper method that converts a Bookmark object from the repo into a BookMarjMarjerView
        return MapsViewModel.BookmarkView(bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone,
            bookmarkRepo.getCategoryResourceId(bookmark.category))
    }

    //pg 338 (pdf)
    private fun mapBookmarksToBookmarkView() {
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks)
        { repoBookmarks ->
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    //pg 339 (pdf)
    fun getBookmarkViews() : LiveData<List<BookmarkView>>? {                                        //Returns LiveData object that will be observed by MapsActivity
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    //pg 416 (pdf)
    private fun getPlaceCategory(place: Place): String {                                            //Converts a place type to a bookmark category
        var category = "Other"                                                                      //Use default "Other" if no type is assigned to the place
        val placeTypes = place.types

        placeTypes?.let { placeTypes ->
            if (placeTypes.size > 0) {                                                              //Check to see if the placeTypes list is populated
                val placeType = placeTypes[0]                                                       //Extract the first type from the list
                category = bookmarkRepo.placeTypeToCategory(placeType)                              //Call placeTypeToCategory() to make the conversion
            }
        }
        return category
    }

    //pg 429 (pdf)
    fun addBookmark(latLng: LatLng): Long? {                                                        //Creates an untitled bookmark at a given location
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.name = "Untitled"
        bookmark.longitude = latLng.longitude
        bookmark.latitude = latLng.latitude
        bookmark.category = "Other"
        return bookmarkRepo.addBookmark(bookmark)
    }
}
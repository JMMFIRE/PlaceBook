package com.jacobmassotto.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import com.jacobmassotto.placebook.R
import com.jacobmassotto.placebook.db.BookmarkDao
import com.jacobmassotto.placebook.db.PlaceBookDatabase
import com.jacobmassotto.placebook.model.Bookmark

// Used to manage bookmarks. Uses BookmarkDao from PlaceBookDatabase
// to access the underlying bookmarks in the db.
// Defines basic methods for saving and loading bookmarks.
class BookmarkRepo(private val context: Context) {

    private var db = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()
    //pg 414 (pdf)
    private var categoryMap: HashMap<Place.Type, String> = buildCategoryMap()
    //pg 416 (pdf)
    private var allCategories: HashMap<String, Int> = buildCategories()                             //Holds the mapping of category names to resource files
    //pg 421 (pdf)
    val categories: List<String> get() = ArrayList(allCategories.keys)                              //Defines get() accessor on categories that takes al the HashMap keys. Returns them as an ArrayList of Strings

    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    val allBookmarks: LiveData<List<Bookmark>>
    get() {
        return bookmarkDao.loadAll()
    }

    //pg 366 (pdf)
    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> {                                     //Returns a live bookmark from the bookmark DAO
        val bookmark = bookmarkDao.loadLiveBookmark(bookmarkId)
        return bookmark
    }

    //pf 374 (pdf)
    fun updateBookmark(bookmark: Bookmark) {                                                        //Takes a Bookmark object and saves it using yhe DAO
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long): Bookmark {                                                   //Takes a bookmarkId and loads the corresponding bookmark using the DAO
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    //pg 414 (pdf)
    private fun buildCategoryMap() : HashMap<Place.Type, String> {                                  //Builds a HashMap that narrows Place.Type to a category name
        return hashMapOf(
            Place.Type.BAKERY to "Restaurant",
            Place.Type.BAR to "Restaurant",
            Place.Type.CAFE to "Restaurant",
            Place.Type.FOOD to "Restaurant",
            Place.Type.RESTAURANT to "Restaurant",
            Place.Type.MEAL_DELIVERY to "Restaurant",
            Place.Type.MEAL_TAKEAWAY to "Restaurant",
            Place.Type.GAS_STATION to "Gas",
            Place.Type.CLOTHING_STORE to "Shopping",
            Place.Type.DEPARTMENT_STORE to "Shopping",
            Place.Type.FURNITURE_STORE to "Shopping",
            Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
            Place.Type.HARDWARE_STORE to "Shopping",
            Place.Type.HOME_GOODS_STORE to "Shopping",
            Place.Type.JEWELRY_STORE to "Shopping",
            Place.Type.SHOE_STORE to "Shopping",
            Place.Type.SHOPPING_MALL to "Shopping",
            Place.Type.STORE to "Shopping",
            Place.Type.LODGING to "Lodging",
            Place.Type.ROOM to "Lodging"
            )
    }

    //pg 414 (pdf)
    fun placeTypeToCategory(placeType: Place.Type): String {                                        //Method takes in a Place.Type and converts it to a valid category contained in buildCategoryMap()
        var category = "Other"
        if (categoryMap.containsKey(placeType)) {
            category = categoryMap[placeType].toString()
        }
        return category
    }

    //pg 415 (pdf)
    private fun buildCategories() : HashMap<String, Int> {                                          //Builds a HashMap that relates a category to a resource ID
        return hashMapOf(
            "Gas" to R.drawable.ic_gas,
            "Lodging" to R.drawable.ic_lodging,
            "Other" to R.drawable.ic_other,
            "Restaurant" to R.drawable.ic_restaurant,
            "Shopping" to R.drawable.ic_shopping
        )
    }

    //pg 416 (pdf)
    fun getCategoryResourceId(placeCategory: String): Int? {
        return allCategories[placeCategory]
    }

    //pg 431 (pdf)
    fun deleteBookmark(bookmark: Bookmark) {                                                        //Deletes bookmark image and bookmark from database
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }
}

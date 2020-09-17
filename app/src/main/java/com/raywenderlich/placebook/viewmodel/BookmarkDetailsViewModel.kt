package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Inet4Address

//pg 366 (pdf)
class BookmarkDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private var bookmarkRepo: BookmarkRepo = BookmarkRepo((getApplication()))
    //pg 369 (pdf)
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null                          //Holds the LiveData<BookmarkDetailsView> so the View can stay up to date

    //pg 337 (pdf)
    data class BookmarkDetailsView(                                                                 //Defines the data needed by BookmarkDetailsActivity
        var id: Long? = null,
        var name: String = "",
        var phone: String = "",
        var address: String = "",
        var notes: String = ""
    ) {

        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFilename(it))   //Loads image associated with the bookmark
            }
            return null
        }
    }

    //pg 369 (pdf)
    private fun bookmarkToBookmarkView(bookmark: Bookmark) : BookmarkDetailsView {
        return BookmarkDetailsView(
            bookmark.id,
            bookmark.name,
            bookmark.phone,
            bookmark.address,
            bookmark.notes
        )
    }

    private fun mapBookmarkToBookmarkView(bookmarkId: Long) {
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark)
        { repoBookmark ->
                bookmarkToBookmarkView(repoBookmark)
        }
    }

    fun getBookmark(bookmarkId: Long): LiveData<BookmarkDetailsView>? {                             //Returns a live bookmark View based on a bookmark ID
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookmarkView(bookmarkId)
        }
        return bookmarkDetailsView
    }

    //pg 374 (pdf)
    private fun bookmarkViewToBookmark(bookmarkView: BookmarkDetailsView): Bookmark? {              //Takes BookmarkDetailsView and returns a Bookmark with the updated parameters from the BookmarkDetailsView
        val bookmark = bookmarkView.id?.let {
            bookmarkRepo.getBookmark(it)
        }
        if (bookmark != null) {
            bookmark.id = bookmarkView.id
            bookmark.name = bookmarkView.name
            bookmark.phone = bookmarkView.phone
            bookmark.address = bookmarkView.address
            bookmark.notes = bookmarkView.notes
        }
        return bookmark
    }

    //pg 375 (pdf)
    fun updateBookmark(bookmarkView: BookmarkDetailsView) {
        GlobalScope.launch {                                                                        //Coroutine used to run method in the background
            val bookmark = bookmarkViewToBookmark(bookmarkView)                                     //BookmarkDetailsView is converted to a Bookmark
            bookmark?.let { bookmarkRepo.updateBookmark(it) }                                       //If the Bookmark isn't null, it's updated through the repo
        }
    }
}
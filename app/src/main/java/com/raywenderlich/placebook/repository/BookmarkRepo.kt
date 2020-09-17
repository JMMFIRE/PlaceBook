package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

// Used to manage bookmarks. Uses BookmarkDao from PlaceBookDatabase
// to access the underlying bookmarks in the db.
// Defines basic methods for saving and loading bookmarks.
class BookmarkRepo(context: Context) {

    private var db = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()

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
}

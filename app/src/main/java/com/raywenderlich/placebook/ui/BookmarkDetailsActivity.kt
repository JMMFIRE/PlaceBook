package com.raywenderlich.placebook.ui

import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import androidx.lifecycle.Observer

//pg 361 (pdf)
class BookmarkDetailsActivity : AppCompatActivity() {

    //pg 370 (pdf)
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null


    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    //pg 370 (pdf)
    private fun populateFields() {                                                                  //Populates the fields in the View
        bookmarkDetailsView?.let { bookmarkView ->
            editTextName.setText(bookmarkView.name)
            editTextPhone.setText(bookmarkView.phone)
            editTextNotes.setText(bookmarkView.notes)
            editTextAddress.setText(bookmarkView.address)
        }
    }

    //pg 371 (pdf)
    private fun populateImageView() {                                                               //Loads the image from the bookmarkView and uses it to set the imageViewPlace
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                imageViewPlace.setImageBitmap(placeImage)
            }
        }
    }

    //pg 371 (pdf)
    private fun getIntentData() {
        val bookmarkId = intent.getLongExtra(                                                       //Pull bookmarkId from the Intent data
            MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this,                      //Retrieve BookmarkDetailsView from BookmarkDetailsViewModel and observe for changes
        Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
            it?.let {                                                                               //Whenever BookmarkDetailsView is loaded or changes assign bookmarkDetailsView property to it
                bookmarkDetailsView = it
                //Populate fields from bookmark
                populateFields()
                populateImageView()
            }
        })
    }

    //pg 374 (pdf)
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {                            //Overrid onCreateOptionsMenu and provide items for the Toolbar
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    //pg 375 (pdf)
    private fun saveChanges() {                                                                     //Takes the current changes from the textFields and updates the Bookmark
        val name = editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = editTextName.text.toString()
            bookmarkView.notes = editTextNotes.text.toString()
            bookmarkView.address = editTextAddress.text.toString()
            bookmarkView.phone = editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {                                   //Called when user selects a Toolbar checkmark item
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}

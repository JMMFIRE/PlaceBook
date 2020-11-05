package com.jacobmassotto.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.jacobmassotto.placebook.R
import com.jacobmassotto.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import androidx.lifecycle.Observer
import com.jacobmassotto.placebook.util.ImageUtils
import java.io.File
import java.net.URLEncoder

//pg 361 (pdf)
class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {

    //pg 370 (pdf)
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    //pg 400 (pdf)
    private var photoFile: File? = null                                                             //Used to keep track of the image File

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
        setupFab()
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
        //pg 398 (pdf)
        imageViewPlace.setOnClickListener {
            replaceImage()
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
                populateCategoryList()
            }
        })
    }

    //pg 374 (pdf)
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {                            //Override onCreateOptionsMenu and provide items for the Toolbar
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
            bookmarkView.category = spinnerCategory.selectedItem as String
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
            R.id.action_delete -> {
                deleteBookmark()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    //pg 397 (pdf)
    override fun onCaptureClick() {
        //pg 400 (pdf)
        photoFile = null                                                                            //Clear any previously assigned photoFile
        try {
            photoFile = ImageUtils.createUniqueImageFile(this)                              //Create a uniquely named image file and assign it to photoFile
        } catch (ex: java.io.IOException) {
            return
        }

        photoFile?.let { photoFile ->                                                               //Make sure the photoFile isn't null before continuing (?.)
            val photoUri = FileProvider.getUriForFile(this,                                 //Get a Uri for the temporary photo file
                "com.raywenderlich.placebook.fileprovider", photoFile)
            val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)            //Create a new Intent with the ACTION_IMAGE_CAPTURE. Used to display camera viewfinder
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }
                .forEach{ grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }//Temporary write permissions given to Intent
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }

    //pg 409 (pdf)
    override fun onPickClick() {
        val pickIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    //pg 398 (pdf)
    private fun replaceImage() {                                                                    //When user taps on a bookmark image, call replaceImage()
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    //pg 400 (pdf)
    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 420                                               //Defines the request code to use when processing camera capture intent
        //pg 409 (pdf)
        private const val   REQUEST_GALLERY_IMAGE = 2
    }

    //pg 405 (pdf)
    private fun updateImage(image: Bitmap) {                                                        //Assigns an image to the imageViewPlace and saves it to the bookmark image file
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    private fun getImageWithPath(filePath: String): Bitmap? {                                       //Use decodeFileSize to load a downsampled image and return it
        return ImageUtils.decodeFileToSize(filePath,
        resources.getDimensionPixelSize(R.dimen.default_image_width),
        resources.getDimensionPixelSize(R.dimen.default_image_height))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == android.app.Activity.RESULT_OK) {                                         //Result code is checked to make sure the user didn't cancel the capture process
            when (requestCode) {                                                                    //
                REQUEST_CAPTURE_IMAGE -> {                                                          //If the requestCode matches REQUEST_CAPTURE_IMAGE (image was captured) continue processing
                    val photoFile = photoFile ?: return                                             //Return early if no photoFile was found
                    val uri = FileProvider.getUriForFile(this,
                        "com.raywenderlich.placebook.fileprovider", photoFile)
                    revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val image = getImageWithPath(photoFile.absolutePath)                            //Get image from the new path
                    image?.let { updateImage(it) }                                                  //Update bookmark image
                }
                //pg 409 (pdf)
                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null) {
                    val imageUri = data.data as Uri
                    val image = getImageWithAuthority(imageUri)
                    image?.let { updateImage(it) }
                }
            }
        }
    }

    //pg 409 (pdf)
    private fun getImageWithAuthority(uri: Uri): Bitmap? {                                          //Loads downsampled image and returns it
        return ImageUtils.decodeUriStreamToSize(uri,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height), this)
    }

    //pg 422(pdf)
    private fun populateCategoryList() {
        val bookmarkView = bookmarkDetailsView ?: return                                            //Returns immediately if bookmarkDetailsView is null
        val resourceId = bookmarkDetailsViewModel                                                   //Retrieve the category icon resourceId from view model
            .getCategoryResourceId(bookmarkView.category)
        resourceId?.let { imageViewCategory.setImageResource(it) }                                  //If resourceId isn't null update imageViewCategory
        val categories = bookmarkDetailsViewModel.getCategories()                                   //Retrieve the list of categories from View model
        val adapter = ArrayAdapter(this,                                                    //Populate Spinner control
            android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        val placeCategory = bookmarkView.category
        spinnerCategory.setSelection(adapter.getPosition(placeCategory))                            //Update spinnerCategory to reflect category selection

        //pg 423 (pdf)
        spinnerCategory.post {
            spinnerCategory.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long) {

                    val category = parent.getItemAtPosition(position) as String
                    val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(category)
                    resourceId?.let {
                        imageViewCategory.setImageResource(it)
                    }
                }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        //Method required but not used

                }
            }
        }
    }

    //pg 432 (pdf)
    private fun deleteBookmark() {
        val bookmarkView = bookmarkDetailsView ?: return

        AlertDialog.Builder(this)
            .setMessage("Delete?")
            .setPositiveButton("OK") { _, _ ->
                bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create().show()
    }

    //pg 434 (pdf)
    private fun sharePlace() {
        val bookmarkView = bookmarkDetailsView ?: return
        var mapUrl = ""
        if (bookmarkView.placeId == null) {
            val location = URLEncoder.encode(
                "${bookmarkView.latitude},"
                        + "${bookmarkView.longitude}", "utf-8"
            )
            mapUrl = "https://www.google.com/maps/dir/?api=1&destination=$location"
        } else {
            val name = URLEncoder.encode(bookmarkView.name, "utf-8")
            mapUrl =  "https://www.google.com/maps/dir/?api=1" +
                    "&destination=$name&destination_place_id=" +
                    "${bookmarkView.placeId}"

        }
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out ${bookmarkView.name} at:\n$mapUrl")
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing ${bookmarkView.name}")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    //pg 437 (pdf)
    private fun setupFab() {
        fab.setOnClickListener { sharePlace() }
    }
}

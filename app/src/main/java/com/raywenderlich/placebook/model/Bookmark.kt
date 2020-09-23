package com.raywenderlich.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raywenderlich.placebook.util.FileUtils
import com.raywenderlich.placebook.util.ImageUtils

@Entity                                                                                             //Tells Room that this is a database entity class
data class Bookmark(                                                                                //Define default values. This will allow us to construct a Bookmark entity with only partial values later
    @PrimaryKey(autoGenerate = true) var id: Long? = null,                                          //Defines a primary key for the class
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var notes: String = "",
    var category: String = ""
)

//pg 348 (pdf)
{
    fun setImage(image: Bitmap, context: Context) {                                                 //setImage() provides the public interface for saving image for a Bookmark
        id?.let {                                                                                   //If the Bookmark has an id the image gets saved t a file
            ImageUtils.saveBitmapToFile(context, image, generateImageFilename(it))
        }
    }

    companion object {                                                                              //Placed in a companion object so its available at the class level. Allows anothher object to load an image without having to load th bookmark from the db
        fun generateImageFilename(id: Long): String {
            return "bookmark$id.png"
        }
    }

    //pg 431 (pdf)
    fun deleteImage(context: Context) {                                                             //Uses FileUtils.deleteFile() to delete image file associated with current bookmark
        id?.let {
            FileUtils.deleteFile(context, generateImageFilename(it))
        }
    }
}

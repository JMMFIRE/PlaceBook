package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

//pg 346 (pdf)
object ImageUtils {                                                                                 //Declared as an object and behaves as a singleton. Lets you directly calls the methods without creating a ImageUtils object
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {                      //Takes in a Context, Bitmap, and String and saves the Bitmap to permanent storage
        val stream = ByteArrayOutputStream()                                                        //Created to hold image data
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)                             //Write the image bitmap to the stream object using PNG format
        val bytes = stream.toByteArray()                                                            //String is converted to an array of bytes
        ImageUtils.saveBytesToFile(context, bytes, filename)                                        //Writes bytes to a file
    }

    private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {             //Takes in a Context, ByteArray, and String object  and saves the bytes to a file
        val outputStream: FileOutputStream
        try {                                                                                       //
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)                   //Used to open a FileOutputStream using the given filename. Context.MODE_PRIVATE flag causes the file to be written where only PlaceBook can access
            outputStream.write(bytes)                                                               //Bytes are written and OutputStream is closed
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //pg 350 (pdf)
    fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {                           //Passes a context and filename and return Bitmap by loading an image from the filename
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

}
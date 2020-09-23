package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

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

    //pg 399 (pdf)
    @Throws(IOException::class)                                                                     //Accounts for File.createTempFile() throwing an IO exception
    fun createUniqueImageFile(context: Context) : File {                                            //Returns an empty File in the app's private pictures folder
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val filename = "Placebook_" + timeStamp + "_"
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(filename, ".jpg", filesDir)
    }

    //pg 403 (pdf)
    private fun calculateInSampleSize(                                                              //Used to calculate the optimum inSampleSize
        width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {

        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return  inSampleSize
    }

    //pg 404 (pdf)
    fun decodeFileToSize(filePath: String, width: Int, height: Int): Bitmap {                       //Used to decode file. Will be called when image needs to be downsized
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true                                                           //Tells BitmapFactory to not load the actual image, just its size
        BitmapFactory.decodeFile(filePath, options)

        options.inSampleSize = calculateInSampleSize(                                               //Calls image with the image width and height and the requested width and height
            options.outWidth, options.outHeight, width, height)
        options.inJustDecodeBounds = false                                                          //Set to false to finally load the whole image
        return BitmapFactory.decodeFile(filePath, options)                                          //Loads the downsampled image from the file and returns it
    }

    //pg 408 (pdf)
    fun decodeUriStreamToSize(uri: Uri, width: Int, height: Int, context: Context): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options: BitmapFactory.Options
            inputStream = context.contentResolver.openInputStream(uri)                              //Open the inputStream for the Uri
            if (inputStream != null) {
                options = BitmapFactory.Options()                                                   //Determine image size
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()                                                                 //Close and open the inputStream and check for null
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    options.inSampleSize = calculateInSampleSize(                                   //Image is loaded from the stream
                        options.outWidth, options.outHeight, width, height)
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(
                        inputStream, null, options)
                    inputStream.close()
                    return bitmap                                                                   //Return image to caller
                }
            }
            return null
        } catch (e: Exception) {
            return null
        } finally {
            inputStream?.close()
        }
    }
}
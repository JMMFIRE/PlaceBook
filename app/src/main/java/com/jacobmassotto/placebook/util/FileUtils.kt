package com.jacobmassotto.placebook.util

import android.content.Context
import java.io.File

object FileUtils {                                                                                  //Deletes a file in the app's main fies directory. Used for deleting image associated with bookmark
    fun deleteFile(context: Context, fileName: String) {
        val dir = context.filesDir
        val file = File(dir, fileName)
        file.delete()
    }
}
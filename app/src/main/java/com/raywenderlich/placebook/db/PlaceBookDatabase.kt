package com.raywenderlich.placebook.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark

//pg 312
@Database(entities = arrayOf(Bookmark::class), version = 1)                                         //Identify a Database class to Room
abstract class PlaceBookDatabase : RoomDatabase() {                                                 //Room requires Database class to be abstract and inherit from RoomDatabase

    abstract fun bookmarkDao(): BookmarkDao                                                         //Defined to return a DAO interface

    companion object {
        private var instance: PlaceBookDatabase? = null                                             //Define the only instance variable
        fun getInstance(context: Context):PlaceBookDatabase {                                       //Take a Context and return a single PlaceBookDatabase
            if (instance == null) {                                                                 //If first time calling getInstance create a single PlaceBookDatabase instamce.
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceBookDatabase::class.java, "PlaceBook").build()
            }
            return instance as PlaceBookDatabase
        }
    }
}
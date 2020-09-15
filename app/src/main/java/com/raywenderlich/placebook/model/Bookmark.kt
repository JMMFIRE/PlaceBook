package com.raywenderlich.placebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity                                                                                             //Tells Room that this is a database entity class
data class Bookmark(                                                                                //Define default values. This will allow us to construct a Bookmark entity with only partial values later
    @PrimaryKey(autoGenerate = true) var id: Long? = null,                                          //Defines a primary key for the class
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = ""
)

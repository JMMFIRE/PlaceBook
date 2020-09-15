package com.raywenderlich.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.ui.MapsActivity

class BookmarkInfoWindowAdapter (context: Activity) : GoogleMap.InfoWindowAdapter {                 //Declare BookmarkWindowAdapter to take single parameter representing hosting activity and implement GoogleMap.InfoWindowAdapter

    //pg 296
    private val contents: View = context.layoutInflater.inflate(                                    //Inflate content_bookmark_info and save to contents
        R.layout.content_bookmark_info, null)                                                 //Use to hold contents view

    override fun getInfoWindow(marker: Marker): View? {                                             //Override getInfoWindow and return null to indicate we wont be taking over entire window
        //This function is required but can return null if not replacing the entire info window
        return null
    }

    override fun getInfoContents(marker: Marker): View? {                                           //Override getInfoContents and fill with titleView and phoneView widgets
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = marker.title ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker.snippet ?: ""

        //pg 298
        val imageView = contents.findViewById<ImageView>(R.id.photo)

        //pg 333 (pdf)
        imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)

        return contents
    }
}
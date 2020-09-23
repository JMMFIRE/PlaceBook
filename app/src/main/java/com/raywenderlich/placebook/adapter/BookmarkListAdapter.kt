package com.raywenderlich.placebook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.bookmark_item.view.*
import java.text.FieldPosition

//pg 388 (pdf)
class BookmarkListAdapter(                                                                          //Adapter takes two arguments: a list of BookmarkView items and a reference to the MapsActivity
    private var bookmarkData: List<MapsViewModel.BookmarkView>?,
    private val mapsActivity: MapsActivity) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    class ViewHolder(v: View,                                                                       //ViewHolder class used to hold the widgets
    private val mapsActivity: MapsActivity) :
            RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.bookmarkNameTextView
        val categoryImageView: ImageView = v.bookmarkIcon

        //pg 393 (pdf)
        init {
            v.setOnClickListener {
                val bookmarkView = itemView.tag as MapsViewModel.BookmarkView
                mapsActivity.moveToBookmark(bookmarkView)
            }
        }
    }

    fun setBookmarkData(bookmarks: List<MapsViewModel.BookmarkView>) {                              //Called when the bookmark data changes
        this.bookmarkData = bookmarks                                                               //Assigns bookmarks to the BookmarkView List
        notifyDataSetChanged()                                                                      //Used to refresh the RecyclerView when changes are made
    }

    override fun onCreateViewHolder(                                                                //Inflates bookmark_item layout to create ViewHolder
        parent: ViewGroup, viewType: Int): BookmarkListAdapter.ViewHolder {
        val vh = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.bookmark_item, parent, false), mapsActivity)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkData = bookmarkData ?: return                                                   //bookmarkData is assigned to bookmarkData is it's not null. If null, return early
        val bookmarkViewData = bookmarkData[position]                                               //bookmarkData is assigned to the current item position
        holder.itemView.tag = bookmarkViewData                                                      //
        holder.nameTextView.text = bookmarkViewData.name
        bookmarkViewData.categoryResourceId?.let {
            holder.categoryImageView.setImageResource(it)                                           //Checks to se if the categoryResourceId is set. If o, it sets the image resourve to the categoryResource Id
        }
    }

    override fun getItemCount(): Int {                                                              //Returns the number of items in the bookmarkDataList
        return  bookmarkData?.size ?: 0
    }
}
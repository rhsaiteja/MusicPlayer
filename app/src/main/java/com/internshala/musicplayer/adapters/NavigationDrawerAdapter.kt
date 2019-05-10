package com.internshala.musicplayer.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.musicplayer.R
import com.internshala.musicplayer.activities.MainActivity
import com.internshala.musicplayer.fragments.AboutUsFragment
import com.internshala.musicplayer.fragments.FavouriteFragment
import com.internshala.musicplayer.fragments.MainScreenFragment
import com.internshala.musicplayer.fragments.SettingsFragment

class NavigationDrawerAdapter(_contentList: ArrayList<String>,_getImages: IntArray,_context: Context):RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>(){
    var contentList:ArrayList<String>? = null
    var getImages:IntArray?=null
    var mcontext: Context?=null
    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mcontext = _context
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NavViewHolder {

        var itemview = LayoutInflater.from(p0.context)
                .inflate(R.layout.row_custom_navigationdrawer,p0, false)
        val returnThis = NavViewHolder(itemview)
        return returnThis
    }

    override fun getItemCount(): Int {

        return (contentList as ArrayList).size
    }

    override fun onBindViewHolder(p0: NavViewHolder, p1: Int) {
        p0?.icon_GET?.setBackgroundResource(getImages?.get(p1) as Int)
        p0?.text_GET?.setText(contentList?.get(p1))
        p0?.contentHolder?.setOnClickListener({
            if(p1 == 0){
                val mainScreenFragment = MainScreenFragment()
                (mcontext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,mainScreenFragment)
                        .commit()
            }else if(p1 == 1){
                val favouriteFragment = FavouriteFragment()
                (mcontext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,favouriteFragment)
                        .commit()
            }else if(p1 == 2){
                val settingsFragment = SettingsFragment()
                (mcontext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,settingsFragment)
                        .commit()
            }else if(p1 == 3){
                val aboutUsFragment = AboutUsFragment()
                (mcontext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,aboutUsFragment)
                        .commit()
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        })
    }


    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon_GET : ImageView? = null
        var text_GET : TextView? = null
        var contentHolder : RelativeLayout? = null
        init {
            icon_GET = itemView?.findViewById(R.id.icon_navdrawer)
            text_GET = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.navdrawer_item_content_holder)
        }
    }

}
package com.internshala.musicplayer.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.musicplayer.R
import com.internshala.musicplayer.Song
import com.internshala.musicplayer.fragments.SongPlayingFragment

class FavoriteAdapter(_songDetails: ArrayList<Song>, _context: Context): RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>(){

    var songDetails: ArrayList<Song>? = null
    var mcontext : Context? = null
    init {
        songDetails = _songDetails
        mcontext = _context
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        var itemView = LayoutInflater.from(p0.context)
                .inflate(R.layout.row_custom_mainscreen_adapter,p0,false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(songDetails == null)
            return 0
        else
            return (songDetails as ArrayList).size
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        val songObject = songDetails?.get(p1)
        p0.trackTitle?.text = songObject?.songTitle
        p0.trackArtist?.text = songObject?.artist
        p0.contentHolder?.setOnClickListener({
            if(SongPlayingFragment.Statified.mediaPlayer!=null && SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.stop()
            }
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist",songObject?.artist)
            args.putString("songTitle",songObject?.songTitle)
            args.putString("path",songObject?.songData)
            args.putInt("songId",songObject?.songId?.toInt() as Int)
            args.putInt("songPosition",p1)
            args.putParcelableArrayList("songData",songDetails)
            songPlayingFragment.arguments = args
            (mcontext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment,songPlayingFragment)
                    .addToBackStack("SongPlayingFragmentFavorite")
                    .commit()

        })
    }

    class MyViewHolder(view : View): RecyclerView.ViewHolder(view){
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null
        init {
            trackArtist = view?.findViewById<TextView>(R.id.trackArtist)
            trackTitle = view?.findViewById<TextView>(R.id.trackTitle)
            contentHolder = view?.findViewById<RelativeLayout>(R.id.contentRow)
        }
    }

}
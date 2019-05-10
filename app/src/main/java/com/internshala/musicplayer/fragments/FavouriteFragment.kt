package com.internshala.musicplayer.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import com.internshala.musicplayer.R
import com.internshala.musicplayer.Song
import com.internshala.musicplayer.adapters.FavoriteAdapter
import com.internshala.musicplayer.databases.EchoDatabase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class FavouriteFragment : Fragment() {

    var noFavorites: TextView? = null
    var nowPlayingBottomBar:RelativeLayout? = null
    var playPauseButton: ImageView? = null
    var songTitle:TextView? = null
    var recyclerView:RecyclerView? = null

    var trackPosition:Int = 0
    var favoriteContent:EchoDatabase? = null
    var refreshList:ArrayList<Song>? = null
    var getListFromDatabase:ArrayList<Song>? = null
    object Statified{
        var myActivity: Activity? = null
        var mediaPlayer:MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        activity?.title = "Favorites"
        setHasOptionsMenu(true)
        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButtonFavScreen)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        recyclerView = view?.findViewById(R.id.favoriteRecycler)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent = EchoDatabase(Statified.myActivity)
        display_favorites_by_searching()
        bottomBarSetup()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item2 : MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    fun getSongsFromPhone(): ArrayList<Song> {

        var songsList = ArrayList<Song>()
        var contentResolver = Statified.myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri,null,null,null,null)
        if(songCursor!= null && songCursor.moveToFirst()){
            var songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            var songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            var songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            var songDate = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while(songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(songDate)
                songsList.add(Song(currentId,currentTitle,currentArtist,currentData,currentDate))
            }
        }
        return songsList
    }

    fun bottomBarSetup(){
        try {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener( {
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()
            })
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }else{
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        }catch (e:Exception){

        }
    }

    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist",SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("songTitle",SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putString("path",SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putInt("songId",SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition",SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData",SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar","success")
            songPlayingFragment.arguments = args
            fragmentManager!!.beginTransaction().replace(R.id.details_fragment,songPlayingFragment).addToBackStack("SongPlayingFragment").commit()
        })
        playPauseButton?.setOnClickListener({
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    fun display_favorites_by_searching(){
        if(favoriteContent?.checkSize() as Int>0){
            refreshList = ArrayList<Song>()
            getListFromDatabase = favoriteContent?.queryDBList()
            var fetchListFromDevice = getSongsFromPhone()
            if(fetchListFromDevice!=null){
                for(i in 0..fetchListFromDevice?.size-1){
                    for(j in 0..getListFromDatabase?.size as Int -1){
                        if((getListFromDatabase?.get(j)?.songId) as Long == (fetchListFromDevice?.get(i)?.songId) as Long){
                            refreshList?.add(getListFromDatabase?.get(j) as Song)
                        }
                    }
                }
            }else{
                //NO SONGS IN PHONE
            }

            if(refreshList == null){
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            }else{
                var favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Song>,Statified.myActivity as Context)
                recyclerView?.layoutManager = LinearLayoutManager(activity)
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }
    }
}

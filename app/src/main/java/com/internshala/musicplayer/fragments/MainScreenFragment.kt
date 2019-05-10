package com.internshala.musicplayer.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.musicplayer.R
import com.internshala.musicplayer.Song
import com.internshala.musicplayer.adapters.MainScreenAdapter
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class MainScreenFragment : Fragment() {

    var songsList: ArrayList<Song>? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageView? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var _mainScreenAdapter:MainScreenAdapter? = null
    var trackPosition:Int =0

    object Statified{
        var mediaPlayer: MediaPlayer? =null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_main_screen,container,false)
        setHasOptionsMenu(true)
        activity?.title = "All Songs"
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarMainScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        songTitle = view?.findViewById(R.id.songTitleMainScreen)
        visibleLayout = view?.findViewById(R.id.visibleLayout)
        noSongs = view?.findViewById(R.id.noSongs)
        recyclerView = view?.findViewById(R.id.contentMain)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        songsList = getSongsFromPhone()
        val prefs = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending","false")
        val action_sort_recent = prefs?.getString("action_sort_recent","false")
        if(songsList == null){
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }else {
            _mainScreenAdapter = MainScreenAdapter(songsList as ArrayList<Song>, myActivity as Context)
            recyclerView?.layoutManager = LinearLayoutManager(myActivity)
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
        }
            if (songsList != null) {
                if (action_sort_ascending!!.equals("true", true)) {
                    Collections.sort(songsList, Song.Statified.nameComparator)
                    _mainScreenAdapter?.notifyDataSetChanged()
                } else if (action_sort_recent!!.equals("true", true)) {
                    Collections.sort(songsList, Song.Statified.dateComparator)
                    _mainScreenAdapter?.notifyDataSetChanged()
                }
            }
            bottomBarSetup()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main,menu)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        if(switcher == R.id.action_sort_ascending){
            val editor = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending","true")
            editor?.putString("action_sort_recent","false")
            editor?.apply()
            if(songsList!=null) {
                Collections.sort(songsList, Song.Statified.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }else if(switcher == R.id.action_sort_recent){
            val editor = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending","false")
            editor?.putString("action_sort_recent","true")
            editor?.apply()
            if(songsList!=null) {
                Collections.sort(songsList, Song.Statified.dateComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item :MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = false
        val item2 :MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = true

    }

    fun getSongsFromPhone(): ArrayList<Song>{
        var songsList = ArrayList<Song>()
        var contentResolver = myActivity?.contentResolver
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
            args.putString("MainScreenBottomBar","success")
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

}

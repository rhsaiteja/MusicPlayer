package com.internshala.musicplayer.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.internshala.musicplayer.CurrentSongHelper
import com.internshala.musicplayer.R
import com.internshala.musicplayer.Song
import com.internshala.musicplayer.activities.MainActivity
import com.internshala.musicplayer.databases.EchoDatabase
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    object  Statified{
        var myActivity: Activity? = null
        var mediaPlayer:MediaPlayer? = null

        var startTimeText:TextView? = null
        var endTimeText:TextView? = null
        var songTitleView:TextView? = null
        var songArtistView:TextView? = null
        var seekBar: SeekBar? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton:ImageButton? = null
        var nextImageButton:ImageButton? = null
        var loopImageButton:ImageButton? = null
        var shuffleImageButton:ImageButton? = null
        var fab:ImageButton? = null

        var currentPosition:Int = 0
        var fetchSongs:ArrayList<Song>? = null
        var currentSongHelper  : CurrentSongHelper? = null
        var audioVisualization: AudioVisualization? = null
        var glView:GLAudioVisualizationView? = null
        var favoriteContent:EchoDatabase? = null
        var mSensorManager:SensorManager? = null
        var mSensorListener:SensorEventListener? = null

        var updateSongTime = object: Runnable{
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition
                val mins = TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long)
                val secs = TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) - (mins*60)
                startTimeText?.setText(String.format("%2d:%2d",mins,secs))
                seekBar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this,1000)
            }
        } //update start time under the seek bar

        var seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2){
                    mediaPlayer?.seekTo(p1)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        } //update song when user interacts with seek bar

    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    object Staticated{
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete() {
            if(Statified.currentSongHelper?.isLoop as Boolean){
                var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                Statified.currentSongHelper?.songId = nextSong?.songId as Long
                Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                Statified.currentSongHelper?.songArtist = nextSong?.artist
                Statified.currentSongHelper?.songPath = nextSong?.songData
                Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                Statified.mediaPlayer?.reset()
                try {
                    Statified.mediaPlayer?.setDataSource(Statified.myActivity,Uri.parse(Statified.currentSongHelper?.songPath))
                    Statified.mediaPlayer?.prepare()
                    Statified.mediaPlayer?.start()
                    processInformation(Statified.mediaPlayer as MediaPlayer)
                }catch(e:Exception){
                    e.printStackTrace()
                }
            }else{
                playNext(Statified.currentSongHelper?.isShuffle as Boolean)
            }
            Statified.currentSongHelper?.isPlaying = true
            updateSongInfo()
            if(Statified.favoriteContent?.checkIfIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_off))
            }
        }

        fun updateSongInfo(){
            Statified.songTitleView?.setText(Statified.currentSongHelper?.songTitle)
            Statified.songArtistView?.setText(Statified.currentSongHelper?.songArtist)
            if(Statified.currentSongHelper?.songArtist.equals("<unknown>",true) as Boolean){
                Statified.songArtistView?.setText("Unknown")
            }
            if(Statified.currentSongHelper?.songTitle.equals("<unknown>",true) as Boolean){
                Statified.songTitleView?.setText("Unknown")
            }
        } // update song title and artist

        fun processInformation(mediaPlayer: MediaPlayer) {

            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekBar?.max = finalTime
            Statified.startTimeText?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
            )
            Statified.endTimeText?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )

            Statified.seekBar?.setProgress(startTime)

            Handler().postDelayed(Statified.updateSongTime, 1000)
        } //to update start time and end time of song for once

        fun playNext(shuffle: Boolean){
            if(!shuffle){
                Statified.currentPosition = (Statified.currentPosition+1)%(Statified.fetchSongs?.size as Int)
            }else{
                Statified.currentPosition = Random().nextInt(Statified.fetchSongs?.size as Int)
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songId = nextSong?.songId as Long
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            updateSongInfo()
            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity,Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            }catch (e:Exception){
                e.printStackTrace()
            }
            if(Statified.favoriteContent?.checkIfIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_off))
            }
        }

    }  //PREFS_FOR_SHUFFLE, PREFS_FOR_LOOP declarartion for shared preferences,functions


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"
        Statified.seekBar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)

        Statified.playPauseImageButton = view?.findViewById<ImageButton>(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.playNextButton)
        Statified.previousImageButton = view?.findViewById(R.id.playPreviousButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.fab = view?.findViewById(R.id.favoriteIcon)
        Statified.fab?.alpha = 0.8f

        Statified.glView = view?.findViewById(R.id.visualizer_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    } // audiovisualization initialisation

    override fun onPause() {
        Statified.audioVisualization?.onPause()
        super.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }
    override fun onResume(){
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener,Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onDestroyView() {
        Statified.audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item :MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2 :MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect ->{
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favoriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false
        var path: String? = null
        var _songArtist:String? = null
        var _songTitle:String? = null
        var songId:Long = 0

        try {
            path = arguments?.getString("path")
            _songArtist = arguments?.getString("songArtist")
            _songTitle = arguments?.getString("songTitle")
            songId = arguments?.getInt("songId")!!.toLong()
            Statified.currentPosition = arguments?.getInt("songPosition") as Int
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            Staticated.updateSongInfo()

        }catch (e:Exception){
            e.printStackTrace()
        }
        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        var fromMainScreenBottomBar = arguments?.get("MainScreenBottomBar") as? String
        if(fromMainScreenBottomBar != null)
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaPlayer
        else if(fromFavBottomBar!= null)
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        else{
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity,Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            }catch (e: Exception){
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if(Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context,0)
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature",false)
        if(isShuffleAllowed as Boolean){
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }else{
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature",false)
        if(isLoopAllowed as Boolean){
            Statified.currentSongHelper?.isLoop = true
            Statified.currentSongHelper?.isShuffle = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }else{
            Statified.currentSongHelper?.isLoop= false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if(Statified.favoriteContent?.checkIfIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_off))
        }
    }

    fun clickHandler() {

        Statified.fab?.setOnClickListener({
            if(Statified.favoriteContent?.checkIfIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.favoriteContent?.deleteFavorite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_off))
                Toast.makeText(Statified.myActivity,"Removed from Favorites",Toast.LENGTH_SHORT).show()
            }else{
                Statified.favoriteContent?.storeAsFavorite(Statified.currentSongHelper?.songId?.toInt() as Int,Statified.currentSongHelper?.songArtist,Statified.currentSongHelper?.songTitle,Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity,"Added to Favorites",Toast.LENGTH_SHORT).show()
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_on))
            }
        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()

            if(Statified.currentSongHelper?.isShuffle as Boolean){
                Statified.currentSongHelper?.isShuffle = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
            }else{
                Statified.currentSongHelper?.isShuffle = true
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature",true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",false)
                editorShuffle?.apply()
            }
        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()

            if(Statified.currentSongHelper?.isLoop as Boolean){
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()

            }else{
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",true)
                editorLoop?.apply()
            }
        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            Staticated.playNext(Statified.currentSongHelper?.isShuffle as Boolean)
        })
        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if(Statified.currentSongHelper?.isLoop as Boolean)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            playPrevious()
        })
        Statified.playPauseImageButton?.setOnClickListener({
            if(Statified.mediaPlayer?.isPlaying as Boolean){
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource((R.drawable.pause_icon))
            }

        })
        Statified.seekBar?.setOnSeekBarChangeListener(Statified.seekBarChangeListener)
    }


    fun playPrevious(){
        if(Statified.currentPosition>0)
            Statified.currentPosition -= 1
        if(Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        val nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songArtist = nextSong?.artist
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.songId = nextSong?.songId as Long
        Statified.currentSongHelper?.currentPosition = Statified.currentPosition
        Staticated.updateSongInfo()
        Statified.mediaPlayer?.reset()
        try {
            Statified.mediaPlayer?.setDataSource(Statified.myActivity,Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        }catch(e:Exception){
            e.printStackTrace()
        }
        if(Statified.favoriteContent?.checkIfIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_off))
        }
    }

    fun bindShakeListener(){
        Statified.mSensorListener = object :SensorEventListener{
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt((x*x + y*y + z*z).toDouble()).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration*0.9f + delta
                if(mAcceleration>12){
                    var prefs = Statified.myActivity?.getSharedPreferences(SettingsFragment.Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)
                    var isAllowed = prefs?.getBoolean("feature",false)
                    if(isAllowed as Boolean){
                        Staticated.playNext(false)
                    }
                }
            }

        }
    }


}
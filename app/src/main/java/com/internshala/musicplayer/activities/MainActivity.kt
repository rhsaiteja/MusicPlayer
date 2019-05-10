package com.internshala.musicplayer.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.internshala.musicplayer.R
import com.internshala.musicplayer.adapters.NavigationDrawerAdapter
import com.internshala.musicplayer.fragments.MainScreenFragment
import com.internshala.musicplayer.fragments.SongPlayingFragment
import kotlinx.android.synthetic.main.app_bar_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var navDrawerTextList : ArrayList<String> = arrayListOf("All Songs","Favorites","Settings","About Us")
    var navDrawerIconList : IntArray = intArrayOf(R.drawable.navigation_allsongs,R.drawable.navigation_favorites,R.drawable.navigation_settings,R.drawable.navigation_aboutus)

    object Statified{
        var drawerLayout: DrawerLayout?=null
        var notificationManager:NotificationManager? = null
    }
    var trackNotifiactionBuilder: Notification?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this@MainActivity,MainActivity.Statified.drawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        MainActivity.Statified.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
                .beginTransaction()
                .add(R.id.details_fragment,mainScreenFragment,"MainScreenFragment")
                .commit()
        var _navDrawerAdapter = NavigationDrawerAdapter(navDrawerTextList,navDrawerIconList,this)
        _navDrawerAdapter.notifyDataSetChanged()

        var navigation_recycler_view = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager = LinearLayoutManager(this)
        navigation_recycler_view.itemAnimator = DefaultItemAnimator()
        navigation_recycler_view.adapter = _navDrawerAdapter
        navigation_recycler_view.setHasFixedSize(true)

        val intent = Intent(this@MainActivity,MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this@MainActivity,System.currentTimeMillis().toInt(),intent,0)
        trackNotifiactionBuilder = Notification.Builder(this)
                .setContentTitle("A track is playing in the background")
                .setSmallIcon(R.drawable.echo_logo)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .build()

        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStart(){
        super.onStart()
        try {
            Statified.notificationManager?.cancel(1)
        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    override fun onStop() {

        super.onStop()
        try {
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                Statified.notificationManager?.notify(1, trackNotifiactionBuilder)
            }
        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Statified.notificationManager?.cancel(1)
        }catch(e:Exception){
            e.printStackTrace()
        }
    }
}

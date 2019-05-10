package com.internshala.musicplayer.utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.internshala.musicplayer.R
import com.internshala.musicplayer.activities.MainActivity
import com.internshala.musicplayer.fragments.SongPlayingFragment

class CaptureBroadcast:BroadcastReceiver(){
    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p1?.action == Intent.ACTION_NEW_OUTGOING_CALL){
            try {
                MainActivity.Statified.notificationManager?.cancel(1)
            }catch(e: java.lang.Exception){
                e.printStackTrace()
            }
            try {
                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }else{
            val tm :TelephonyManager = p0?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            when(tm?.callState){
                TelephonyManager.CALL_STATE_RINGING -> {
                    try {
                        MainActivity.Statified.notificationManager?.cancel(1)
                    }catch(e: java.lang.Exception){
                        e.printStackTrace()
                    }
                    try {
                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                else -> {}
            }
        }
    }

}
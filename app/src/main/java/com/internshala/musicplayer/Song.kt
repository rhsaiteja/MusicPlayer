package com.internshala.musicplayer

import android.os.Parcel
import android.os.Parcelable

class Song(var songId: Long, var songTitle: String, var artist: String, var songData: String, var dateAddded: Long):Parcelable{
    override fun writeToParcel(p0: Parcel?, p1: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }
    object Statified{
        var nameComparator:Comparator<Song> = Comparator<Song>{song1 ,song2 ->
                val songOne = song1.songTitle.toUpperCase()
                val songTwo = song2.songTitle.toUpperCase()
                songOne.compareTo(songTwo)

        }
        var dateComparator:Comparator<Song> = Comparator<Song>{song1,song2->
            val songOne = song1.dateAddded.toDouble()
            val songTwo = song2.dateAddded.toDouble()
            songTwo.compareTo(songOne)
        }
    }
}
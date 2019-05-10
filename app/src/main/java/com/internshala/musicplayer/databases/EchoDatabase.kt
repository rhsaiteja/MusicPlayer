package com.internshala.musicplayer.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.internshala.musicplayer.Song
import com.internshala.musicplayer.fragments.FavouriteFragment

class EchoDatabase: SQLiteOpenHelper{

    var _songList = ArrayList<Song>()

    object Staticated{
        val DB_NAME = "FavoriteDatabse"
        var DB_VERSION = 1
        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"

    }
    override fun onCreate(sqliteDatabase: SQLiteDatabase?) {
        sqliteDatabase?.execSQL("CREATE TABLE "+Staticated.TABLE_NAME+"("+Staticated.COLUMN_ID+" INTEGER,"+Staticated.COLUMN_SONG_ARTIST+" STRING,"+Staticated.COLUMN_SONG_TITLE+" STRING,"+Staticated.COLUMN_SONG_PATH+" STRING);")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version){}
    constructor(context: Context?) : super(context, Staticated.DB_NAME, null,Staticated.DB_VERSION){}

    fun storeAsFavorite(id: Int?,artist: String?,songTitle:String?,path:String?){
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Staticated.COLUMN_ID,id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST,artist)
        contentValues.put(Staticated.COLUMN_SONG_TITLE,songTitle)
        contentValues.put(Staticated.COLUMN_SONG_PATH,path)
        db.insert(Staticated.TABLE_NAME,null,contentValues)
        db.close()
    }

    fun queryDBList(): ArrayList<Song>?{

        try {
            val db = this.readableDatabase
            val query_params = "SELECT * FROM "+Staticated.TABLE_NAME+";"
            var cSor = db.rawQuery(query_params,null)
            if(cSor.moveToFirst()){
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_PATH))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_TITLE))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_ARTIST))
                    _songList.add(Song(_id.toLong() as Long,_title,_artist,_songPath,0))
                }while (cSor.moveToNext())
            }else{
                return null
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return _songList
    }

    fun checkIfIdExists(id:Int):Boolean{
        val db = this.readableDatabase
        val query_params = "SELECT * FROM "+Staticated.TABLE_NAME+" WHERE "+Staticated.COLUMN_ID+"="+id+";"
        var cSor = db.rawQuery(query_params,null)
        if(cSor.moveToFirst())
            return true
        else
            return false
    }

    fun deleteFavorite(id:Int){
        val db = this.writableDatabase
        db.delete(Staticated.TABLE_NAME,Staticated.COLUMN_ID+"="+id,null)
        db.close()
    }

    fun checkSize():Int{
        var counter:Int=0

        val db = this.readableDatabase
        val query_params = "SELECT * FROM "+Staticated.TABLE_NAME+";"
        var cSor = db.rawQuery(query_params,null)
        if(cSor.moveToFirst()){
            do {
                counter++
            }while (cSor.moveToNext())
        }else{
            return 0
        }
        return counter
    }
}
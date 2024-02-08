package com.example.news.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.news.models.Article

@Database(entities = [Article::class], version = 1)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase(){
    abstract fun articleDao() : ArticleDao
    companion object{
        @Volatile
        private var INSTANCE : ArticleDatabase? = null
        fun getDatabase(context: Context) : ArticleDatabase{
            if (INSTANCE == null){
                synchronized(this){
                    INSTANCE = Room.databaseBuilder(
                        context,
                        ArticleDatabase::class.java,
                        "article_db.db"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}
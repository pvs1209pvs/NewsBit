package com.param.newsbit.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.param.newsbit.entity.News

@Dao
interface NewsDao {

    //

    @Query("SELECT * FROM news_table")
    fun selectAll() : LiveData<List<News>>

    //


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(newsList: List<News>)

    @Query("SELECT COUNT(*) FROM news_table WHERE genre = :genre AND pubDate = :dateString")
    suspend fun localCount(genre: String, dateString: String): Int

    @Query("SELECT content FROM news_table WHERE url = :url")
    suspend fun selectContent(url: String) : String

    @Query("SELECT * FROM news_table WHERE genre = :genre AND pubDate >= DATE(:today, '-1 day')")
    fun selectByGenre(genre: String, today: String): LiveData<List<News>>

    @Query("SELECT * FROM news_table WHERE isBookmarked = 1")
    fun selectBookmarked(): LiveData<List<News>>

    @Query("SELECT isBookmarked FROM news_table WHERE url = :url")
    fun selectBookmark(url: String): LiveData<Int>

    @Query("SELECT summary FROM news_table WHERE url = :url")
    suspend fun selectSummary(url: String): String?

    @Query("SELECT summary FROM news_table WHERE url = :url")
    fun selectSummaryLD(url: String): LiveData<String?>

    @Query("SELECT content FROM news_table WHERE url = :url")
    fun selectBody(url: String) : LiveData<String>

    @Query("UPDATE news_table SET summary = :newSummary WHERE url = :url")
    suspend fun updateSummary(url: String, newSummary: String?)

    @Query("UPDATE news_table SET isBookmarked = :value WHERE url = :url")
    suspend fun toggleBookmark(url: String, value: Boolean)

    // 604800 seconds in a week
    @Query("DELETE FROM news_table WHERE (unixepoch(:today)-unixepoch(pubDate) >= 604800)")
    suspend fun deleteOlderThanWeek(today:String)

}
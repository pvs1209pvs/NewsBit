package com.param.newsbit.model.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.param.newsbit.model.entity.News

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(newsList: List<News>)

    @Query("SELECT COUNT(*) FROM news_table WHERE genre = :genre AND pubDate = :dateString")
    suspend fun localCount(genre: String, dateString: String): Int

    @Query("SELECT * FROM news_table WHERE genre = :genre AND pubDate = :dateString")
    fun selectByGenre(genre: String, dateString: String): LiveData<List<News>>

    @Query("SELECT * FROM news_table WHERE isBookmarked = 1")
    fun selectBookmarked(): LiveData<List<News>>

    @Query("SELECT isBookmarked FROM news_table WHERE url = :url")
    fun selectBookmark(url: String): LiveData<Int>


    @Query("SELECT summary FROM news_table WHERE url = :url")
    suspend fun selectSummary(url: String): String?

    @Query("SELECT summary FROM news_table WHERE url = :url")
    fun selectSummaryLD(url: String): LiveData<String?>

    @Query("UPDATE news_table SET summary = :newSummary WHERE url = :url")
    suspend fun updateSummary(url: String, newSummary: String)

    @Query("UPDATE news_table SET isBookmarked = :value WHERE url = :url")
    suspend fun toggleBookmark(url: String, value: Boolean)


}
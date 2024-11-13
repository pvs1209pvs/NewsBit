package com.param.newsbit.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.param.newsbit.entity.News

@Dao
interface NewsDao {

    @Query("SELECT * FROM news_table " +
            "WHERE title LIKE '%' || :title || '%' ")
    fun selectByTitle(title:String) : PagingSource<Int, News>

    @Query("SELECT * FROM news_table " +
            "WHERE title LIKE '%' || :title || '%' AND genre = :genre " +
            "ORDER BY pubDate DESC")
    fun selectByTitleGenre(title:String, genre :String) : PagingSource<Int, News>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newsList: List<News>)

    @Query("SELECT * FROM news_table " +
            "WHERE genre = :genre AND pubDate >= DATE(:today, '-7 day') " +
            "ORDER BY pubDate DESC")
    fun selectByGenre(genre: String, today: String): LiveData<List<News>>

    @Query("SELECT * FROM news_table " +
            "WHERE genre = :genre AND pubDate >= DATE(:today, '-7 day') " +
            "ORDER BY pubDate DESC")
    fun selectAll(genre: String, today: String): PagingSource<Int, News>

    @Query("SELECT * FROM news_table " +
            "WHERE genre = :genre AND pubDate = :date")
    fun selectByDate(genre: String, date:String) : PagingSource<Int, News>

    @Query("SELECT content FROM news_table " +
            "WHERE url = :url")
    suspend fun selectContent(url: String): String

    @Query("SELECT summary FROM news_table " +
            "WHERE url = :url")
    suspend fun selectSummary(url: String): String

    @Query("SELECT summary FROM news_table " +
            "WHERE url = :url")
    fun selectSummaryLD(url: String): LiveData<String>

    @Query("UPDATE news_table " +
            "SET summary = :newSummary " +
            "WHERE url = :url")
    suspend fun updateSummary(url: String, newSummary: String)

    @Query("UPDATE news_table " +
            "SET isBookmarked = :value " +
            "WHERE url = :url")
    suspend fun updateBookmark(url: String, value: Boolean)

    @Query("SELECT * FROM news_table " +
            "WHERE isBookmarked = 1")
    fun selectBookmarked(): LiveData<List<News>>

    @Query("SELECT isBookmarked FROM news_table " +
            "WHERE url = :url")
    fun selectBookmark(url: String): LiveData<Int>

    // TODO: do not delete bookmarked news
    // 604800 seconds in a week
    @Query("DELETE FROM news_table " +
            "WHERE (UNIXEPOCH(:today)-UNIXEPOCH(pubDate) >= 604800)")
    suspend fun deleteOlderThanWeek(today: String)

}

package com.param.newsbit.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
@Entity(tableName = "news_table")
data class News(
    @PrimaryKey var url: String,
    var title: String,
    var genre: String,
    var pubDate: LocalDate,
    var content: String,
    var summary: String = "",
    var imageUrl: String? = null,
    var isBookmarked: Boolean = false
) : Parcelable


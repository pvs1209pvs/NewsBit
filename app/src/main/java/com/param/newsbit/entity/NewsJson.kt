package com.param.newsbit.entity

// Article List
data class NewsJson(
    val rows: List<Row>,
)

// Article
data class Row(
    val content: List<String>,
    val title: String,
    val url: String,
    val preview: Preview,
    val starttime: StartTime
)

// Image
data class Preview(
    val url: String
)

// Time
data class StartTime(
    val utc : String
)
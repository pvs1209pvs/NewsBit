package com.param.newsbit.entity

import java.time.LocalDate

data class NewsFilter(
    val genre: String,
    val searchQuery: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
package com.param.newsbit.api

import com.param.newsbit.entity.NewsJson
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TStarAPI {

    @GET("/search/?f=json&t=article")
    suspend fun downloadNews(
        @Query("c") c: String?,
        @Query("l") l: Int,
    ): Response<NewsJson>

}

package com.param.newsbit.model.parser

import android.util.Log
import org.jsoup.Jsoup

object ArticleDownloader {

    private val TAG = javaClass.simpleName

    fun getNewsBody(articleUrl: String): String {

        Log.d(TAG, "getNewsBody: $articleUrl")

        return Jsoup
            .connect(articleUrl)
            .timeout(10_000)
            .get() // TODO org.jsoup.HttpStatusException: HTTP error fetching URL. Status=404,
            .select("div#article-body")
            .select("p")
            .joinToString("") { it.text() }

    }

}
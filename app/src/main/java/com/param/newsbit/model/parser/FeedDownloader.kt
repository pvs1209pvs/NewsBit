package com.param.newsbit.model.parser

import android.util.Log
import com.param.newsbit.entity.News
import org.w3c.dom.Element
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

object FeedDownloader {

    private val TAG = javaClass.simpleName

    fun getRSSFeed(genre: String): List<News> {

        Log.d(TAG, "Parsing genre = $genre")

        val url = FeedURL.genre[genre]!!

        Log.d(TAG, "Parsing url = $url")

        try {

            val feedConnection = URL(url).openConnection() as HttpURLConnection
            feedConnection.connectTimeout = 10000

            if (feedConnection.responseCode == HttpURLConnection.HTTP_OK) {

                val feed = URL(url).openConnection().getInputStream()!!

                val itemTags = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(feed)
                    .documentElement
                    .getElementsByTagName("item")

                feed.close()
                feedConnection.disconnect()

                Log.d(TAG, "${itemTags.length} items downloaded from the feed")

                val newsFeedList = mutableListOf<News>()

                for (i in 0 until itemTags.length) {

                    val item = itemTags.item(i) as Element

                    val linkTag = item.getElementsByTagName("link")
                    val itemUrl = if (linkTag.length > 0) linkTag.item(0).textContent else continue

                    val pubDateTag = item.getElementsByTagName("pubDate")
                    val pubDate =
                        if (pubDateTag.length > 0) pubDateTag.item(0).textContent else continue

                    val titleTag = item.getElementsByTagName("title")
                    val title = if (titleTag.length > 0) titleTag.item(0).textContent else "null"

                    val mediaTag = item.getElementsByTagName("enclosure")
                    val media = if (mediaTag.length > 0) mediaTag.item(0) as Element else null
                    val imageUrl = if (media != null) media.getAttribute("url") else "null"

                    newsFeedList += News(
                        title = title,
                        summary = "",
                        url = itemUrl,
                        genre = genre,
                        imageUrl = imageUrl,
                        pubDate = dateParser(pubDate),
                        isBookmarked = false
                    )

                }

                Log.d(TAG, "getRSSFeed: ${newsFeedList.size} items parsed for $genre")

                return newsFeedList
            } else {
                Log.e(
                    TAG,
                    "Error downloadign feed ${feedConnection.responseCode} ${feedConnection.responseMessage}"
                )
                return emptyList()
            }

        } catch (e: SocketTimeoutException) {
            Log.e(TAG, e.message.toString())
            return emptyList()
        } catch (e: MalformedURLException) {
            Log.e(TAG, e.message.toString())
            return emptyList()
        }

    }

    private fun dateParser(dateString: String): LocalDate {

        val pattern = Pattern.compile("\\b(\\d{1,2}) (\\w{3}) (\\d{4})\\b")
        val match = pattern.matcher(dateString)
        match.find()

        val day = match.group(1)!!.toInt()
        val monthShortName = match.group(2)!!
        val monthValue = DateFormatSymbols().shortMonths.indexOf(monthShortName) + 1
        val year = match.group(3)!!.toInt()

        return LocalDate.of(year, monthValue, day)

    }

}
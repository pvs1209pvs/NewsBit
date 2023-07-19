package com.param.newsbit.model.parser

import android.util.Log
import com.param.newsbit.model.entity.News
import org.jsoup.Jsoup
import org.w3c.dom.Element
import java.io.InputStream
import java.net.URL
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

object RSSFeedParser {

    fun getRSSFeed(genre: String): List<News> {

        val feedUrl = FeedURL.genre[genre]!!

        Log.d(javaClass.simpleName + " getRSSFeed", "Genre URL $feedUrl")

        val list = feedParser(feedUrl, genre)

        Log.d(javaClass.simpleName + " getRSSFeed", "Downloading ${list.size} items from RSS feed")

        return list

    }

    private fun tstarFeedParser(feed: InputStream, genre: String): List<News> {

        Log.d("tstar parse", "start")

        val newsFeedList = mutableListOf<News>()

        val root = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(feed)
            .documentElement

        val itemTags = root.getElementsByTagName("item")

        for (i in 0 until itemTags.length) {

            val item = itemTags.item(i) as Element

            val url = item.getElementsByTagName("link").item(0).textContent
            val title = item.getElementsByTagName("title").item(0).textContent
            val pubDate = item.getElementsByTagName("pubDate").item(0).textContent

            val media = item.getElementsByTagName("media:content").item(0) as Element?
            val imageUrl = media?.getAttribute("url")

            newsFeedList += News(
                title = title,
                summary = null,
                url = url,
                genre = genre,
                imageUrl = imageUrl,
                pubDate = dateParser(pubDate),
                isBookmarked = false
            )

        }

        Log.d("$genre feed items parsed", newsFeedList.size.toString())

        return newsFeedList

    }

    private fun feedParser(url: String, genre: String): List<News> {
        val feed = downloadRSSFeed(url)
        return tstarFeedParser(feed, genre)
    }

    private fun downloadRSSFeed(url: String) = URL(url).openConnection().getInputStream()!!

    fun getTStarBody(url: String): String {

        Log.d(javaClass.simpleName + "/getTStarBody", url)

        return Jsoup
            .connect(url)
            .get() // TODO org.jsoup.HttpStatusException: HTTP error fetching URL. Status=404,
            .select("p.text-block-container")
            .joinToString("") { it.text() }
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
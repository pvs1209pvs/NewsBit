package com.param.newsbit.model.parser

object FeedURL {

    val genre = mapOf(

        "Top Stories" to "https://www.thestar.com/search/?f=rss&t=article&bl=2827101&l=20",

        "Business" to "https://www.thestar.com/search/?f=rss&t=article&c=business*&l=50&s=start_time&sd=desc",

        "Real Estate" to "https://www.thestar.com/search/?f=rss&t=article&c=real-estate*&l=50&s=start_time&sd=desc",

        "Opinion" to "https://www.thestar.com/search/?f=rss&t=article&c=opinion*&l=50&s=start_time&sd=desc",

        "Politics" to "https://www.thestar.com/search/?f=rss&t=article&c=politics*&l=50&s=start_time&sd=desc",

        "Life" to "https://www.thestar.com/search/?f=rss&t=article&c=life*&l=50&s=start_time&sd=desc",
        )

}
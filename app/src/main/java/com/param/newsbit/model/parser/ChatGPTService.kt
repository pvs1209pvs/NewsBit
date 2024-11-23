package com.param.newsbit.model.parser

interface ChatGPTService {

    fun summarize(newsBody: String): String

}
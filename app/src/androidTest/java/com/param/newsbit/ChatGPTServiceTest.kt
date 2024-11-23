package com.param.newsbit

import com.param.newsbit.model.parser.ChatGPTService

object ChatGPTServiceTest : ChatGPTService {

    override fun summarize(newsBody: String) = "Growing economy article."

}
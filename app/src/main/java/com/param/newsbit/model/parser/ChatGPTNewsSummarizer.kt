package com.param.newsbit.model.parser

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object ChatGPTNewsSummarizer {

    private fun buildChatGPTRequestBody(newsBody: String): JSONObject {

        val jsonObject = JSONObject()
        jsonObject.put("model", "gpt-3.5-turbo")

        val messagesArray = JSONArray()

        val systemMessage = JSONObject()
        systemMessage.put("role", "system")
        systemMessage.put("content", "You are a helpful assistant.")
        messagesArray.put(systemMessage)

        val userMessage = JSONObject()
        userMessage.put("role", "user")
        userMessage.put("content", "summarize the following text: $newsBody")
        messagesArray.put(userMessage)

        jsonObject.put("messages", messagesArray)

        return jsonObject

    }

    fun summarize(message: String): String {

        Log.d(javaClass.simpleName + "/summarize","Summarize with chat gpt")

        val apiKey = "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"
        val url = "https://api.openai.com/v1/chat/completions"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val requestBody = buildChatGPTRequestBody(message).toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val result = OkHttpClient().newCall(request).execute().use {
            it.body?.string() ?: ""
        }

        return JSONObject(result)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

    }
}
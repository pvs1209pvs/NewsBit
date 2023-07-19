package com.param.newsbit.model.database

import androidx.room.TypeConverter
import org.json.JSONObject
import java.time.LocalDate

class Converter {

    @TypeConverter
    fun fromLocalDate(localDate: LocalDate): String {

        val jsonObj = JSONObject().apply {
            put("year", localDate.year)
            put("month", localDate.monthValue)
            put("day", localDate.dayOfMonth)
        }

        return jsonObj.toString()

    }

    @TypeConverter
    fun toLocalDate(string: String): LocalDate {

        val jsonObj = JSONObject(string)

        val year = jsonObj.getInt("year")
        val month = jsonObj.getInt("month")
        val day = jsonObj.getInt("day")

        return LocalDate.of(year, month, day)

    }

}
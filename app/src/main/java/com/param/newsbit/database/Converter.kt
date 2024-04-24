package com.param.newsbit.database

import androidx.room.TypeConverter
import org.json.JSONObject
import java.time.LocalDate

class Converter {

    @TypeConverter
    fun fromLocalDate(localDate: LocalDate) = localDate.toString()

    @TypeConverter
    fun toLocalDate(string: String) : LocalDate = LocalDate.parse(string)
}
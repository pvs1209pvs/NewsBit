package com.param.newsbit.ui.validator

import com.google.android.material.datepicker.CalendarConstraints
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.ZoneOffset

@Parcelize
class CustomDateValidator : CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {

        val pastWeek = LocalDate.now()
            .minusWeeks(1)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

        val currentDay = LocalDate.now()
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

        return date in (pastWeek + 1)..currentDay

    }

}
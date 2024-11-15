package com.param.newsbit.ui.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.time.LocalDate


const val SHARED_PREFERENCE_NAME = "date_picker"

class DatePickerFragment(
    private val onDateSetCallback: (LocalDate) -> Unit
) : DialogFragment(), DatePickerDialog.OnDateSetListener {


    private val TAG = javaClass.simpleName


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val currentDate = LocalDate.now()

        val sharedPreferences = requireActivity()
            .getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        val storedYear = sharedPreferences.getInt("year", currentDate.year)
        val storedMonth = sharedPreferences.getInt("month", currentDate.monthValue - 1)
        val storedDay = sharedPreferences.getInt("day", currentDate.dayOfMonth)

        Log.i(TAG, "onCreateDialog: sharedPref: $storedYear $storedMonth $storedDay")

        return DatePickerDialog(
            requireContext(),
            this,
            storedYear,
            storedMonth,
            storedDay
        )

    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        onDateSetCallback(LocalDate.of(year, month + 1, day))

        requireActivity()
            .getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putInt("year", year)
                putInt("month", month)
                putInt("day", day)
                apply()
            }

    }

}

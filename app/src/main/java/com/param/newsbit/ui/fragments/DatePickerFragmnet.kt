package com.param.newsbit.ui.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.time.LocalDate
import java.util.Calendar

class DatePickerFragment(
    private val onDateSelectedCallback: (LocalDate) -> Unit
) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private val TAG = javaClass.simpleName

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val date = LocalDate.now()
        return DatePickerDialog(requireContext(), this, date.year, date.monthValue-1, date.dayOfMonth)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        onDateSelectedCallback(LocalDate.of(year, month+1, day))
    }

}

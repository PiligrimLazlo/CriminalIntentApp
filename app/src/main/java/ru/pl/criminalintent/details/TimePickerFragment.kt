package ru.pl.criminalintent.details

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.util.*

class TimePickerFragment : DialogFragment() {

    private val args: TimePickerFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        calendar.time = args.crimeDate
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val initialHour = calendar.get(Calendar.HOUR)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val timeListener =
            TimePickerDialog.OnTimeSetListener { tp: TimePicker, hours: Int, minute: Int ->
                val resultDateTime =
                    GregorianCalendar(initialYear, initialMonth, initialDay, hours, minute).time
                setFragmentResult(
                    REQUEST_KEY_DATE_TIME,
                    bundleOf(BUNDLE_KEY_DATE_TIME to resultDateTime)
                )
            }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinute,
            true
        )
    }

    companion object {
        const val REQUEST_KEY_DATE_TIME = "REQUEST_KEY_DATE_TIME"
        const val BUNDLE_KEY_DATE_TIME = "BUNDLE_KEY_DATE_TIME"
    }

}
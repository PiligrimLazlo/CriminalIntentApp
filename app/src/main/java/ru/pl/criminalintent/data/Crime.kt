package ru.pl.criminalintent.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class Crime(
    @PrimaryKey val id: UUID,
    val title: String,
    val date: Date,
    val isSolved: Boolean,
    val suspect: String = "",
    val suspectPhoneNumber: String = "",
    val photoFileName: String? = null
) {

    fun isRequiresPolice(): Int {
        val random = (0..10).random()
        return if (random > 3)
            NOT_REQUIRES_POLICE
        else
            REQUIRES_POLICE
    }

    fun getFormattedStringCrimeDate(): String {
        val pattern = if (Locale.getDefault() == Locale("en")) {
            "MMM dd, yyyy"
        } else {
            "dd.MM.yy"
        }
        val sdf = SimpleDateFormat(pattern, Locale("default"))
        return sdf.format(date)
    }

    fun getFormattedStringCrimeTime(): String {
        val sdf = SimpleDateFormat("hh:mm", Locale("default"))
        return sdf.format(date)
    }

    companion object {
        const val NOT_REQUIRES_POLICE = 0
        const val REQUIRES_POLICE = 1
    }
}
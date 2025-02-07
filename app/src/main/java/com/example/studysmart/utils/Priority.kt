package com.example.studysmart.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.example.studysmart.ui.theme.Green
import com.example.studysmart.ui.theme.Orange
import com.example.studysmart.ui.theme.Red
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.hours


enum class Priority(
    val title:String,
    val color: Color,
    val value:Int

) {
    LOW(title = "Low", color = Green, value = 0),
    MEDIUM(title = "Medium", color = Orange, value = 1),
    HIGH(title = "High", color = Red, value = 2);

    companion object {
        fun fromIntToPriority(value: Int): Priority {
            return when (value) {
                0 -> LOW
                1 -> MEDIUM
                2 -> HIGH
                else -> MEDIUM
            }
        }
    }
}

fun Long?.changeMillisToString():String{
    val date:LocalDate=this?.let{
        Instant
            .ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }?:LocalDate.now()   //if fails to convert return today date
    return date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
}

fun Long.toHours():Float{
    val hrs=this.toFloat()/3600f
    return "%.2f".format(hrs).toFloat()
}

//for timerService
fun Int.toStringWithPad():String{
    return this.toString().padStart(length = 2, padChar = '0')  //if less than 2 digit then append 0 in start
}

sealed class SnackBarEvent{
    data class ShowSnackbar(
        val msg:String,
        val snackBarDuration:SnackbarDuration=SnackbarDuration.Short
    ):SnackBarEvent()

    data object navigateUp:SnackBarEvent()
}
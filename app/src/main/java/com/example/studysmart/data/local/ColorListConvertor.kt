package com.example.studysmart.data.local

import androidx.room.TypeConverter

// Used in Room to convert to String -- TypeConvertors

class ColorListConvertor {

    @TypeConverter
    fun colorToString(colorList:List<Int>):String{
        return colorList.joinToString(
            ","
        ) {
            it.toString()
        }
    }

    @TypeConverter
    fun toColorList(colorListString:String):List<Int>{
        return colorListString.split(",").map { it.toInt() }
    }

}
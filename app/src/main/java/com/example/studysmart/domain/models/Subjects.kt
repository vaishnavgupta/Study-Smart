package com.example.studysmart.domain.models

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.studysmart.ui.theme.gradient1
import com.example.studysmart.ui.theme.gradient2
import com.example.studysmart.ui.theme.gradient3
import com.example.studysmart.ui.theme.gradient4
import com.example.studysmart.ui.theme.gradient5

@Entity
data class Subjects(
    val name:String,
    val goalHrs:Float,
    val colors:List<Int>,          //Room is not able to store Colors in Db (hence TypeConvertors are required)
    @PrimaryKey(autoGenerate = true)
    val subjectId:Int?=null
){
    companion object{
        val subjectCardColors= listOf(gradient1, gradient2, gradient3, gradient4, gradient5)
    }
}

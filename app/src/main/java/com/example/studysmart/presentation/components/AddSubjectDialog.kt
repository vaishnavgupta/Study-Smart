package com.example.studysmart.presentation.components



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studysmart.domain.models.Subjects


@Composable
fun addSubjDialog(
    isOpen:Boolean,
    title:String="Add/Update Subject",
    subjectName:String,
    onSubjectNameChange:(String)->Unit,
    goalHrs:String,
    onGoalHrsChange:(String)->Unit,
    selectedColor:List<Color>,
    onColourChange:(List<Color>)->Unit,
    onDismissRequest:()->Unit,
    onConfirmClick:()->Unit
) {

    var subjectNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var goalHrsError by rememberSaveable { mutableStateOf<String?>(null) }

    subjectNameError=when{
        subjectName.isBlank() -> "Please Enter Subject Name."
        subjectName.length<2  -> "Subject Name is too short."
        subjectName.length>12  -> "Subject Name is too long."
        else -> null
    }

    goalHrsError=when{
        goalHrs.isBlank() -> "Please Enter goal hours."
        goalHrs.toFloatOrNull()==null  -> "Invalid goal hours."
        goalHrs.toFloat()<1f  -> "Please set atleast 1 hour goal."
        goalHrs.toFloat()>500f  -> "Please set atmost 500 hours goal."
        else -> null
    }

    if(isOpen){
        AlertDialog(onDismissRequest=onDismissRequest,
            title = { Text(text = title) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Subjects.subjectCardColors.forEach { colour->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = if(colour==selectedColor) Color.Black
                                        else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .background(brush = Brush.verticalGradient(colour))
                                    .clickable { onColourChange(colour) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = onSubjectNameChange,
                        label = { Text(text = "Enter Subject Name") },
                        singleLine = true,
                        isError = (subjectName.isNotBlank() && subjectNameError!=null),
                        supportingText = { Text(text = subjectNameError.orEmpty()) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = goalHrs,
                        onValueChange = onGoalHrsChange,
                        label = { Text(text = "Goal Study Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = (goalHrs.isNotBlank() && goalHrsError!=null),
                        supportingText = { Text(text = goalHrsError.orEmpty()) }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmClick,
                    enabled = (subjectNameError==null && goalHrsError==null)
                ) {
                    Text(text = "Save")
                }
            }
        )
    }
}
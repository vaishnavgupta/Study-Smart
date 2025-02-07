package com.example.studysmart.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePicker(
    state:DatePickerState,
    isOpen:Boolean,
    confirmBtnText:String="OK",
    cancelBtnText:String="Cancel",
    onDismissRequest:()->Unit,
    onConfirmBtnClick:()->Unit
) {
    if(isOpen){
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onConfirmBtnClick) {
                    Text(text = confirmBtnText)
                }
            },
            dismissButton = {
                TextButton(onClick = onConfirmBtnClick) {
                    Text(text = cancelBtnText)
                }
            },
            content = { DatePicker(
                state=state
            ) }
        )
    }
}
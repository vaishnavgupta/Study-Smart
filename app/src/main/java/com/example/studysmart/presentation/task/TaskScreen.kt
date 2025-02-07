package com.example.studysmart.presentation.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.presentation.components.SubjectListBottomSheet
import com.example.studysmart.presentation.components.TaskDatePicker
import com.example.studysmart.presentation.components.deleteDialog
import com.example.studysmart.presentation.components.taskCheckBox
import com.example.studysmart.utils.Priority
import com.example.studysmart.utils.SnackBarEvent
import com.example.studysmart.utils.changeMillisToString
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class TaskScreenNavArgs(
    val subjectId:Int?,
    val taskId:Int?         //nullable because sometimes subid is provided(clicked on float btn) and sometimes task id(clicked on edit task)
)

@Destination(navArgsDelegate = TaskScreenNavArgs::class)
@Composable
fun TaskScreenRoute(
    navigator: DestinationsNavigator
) {
    val viewModel:TaskScreenViewModel= hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TaskScreen(
        state = state,
        onEvent = viewModel::manageEventChanges,
        snackBarEvent = viewModel.snackbarEventFlow,
        onBackBtnClick = {
            navigator.navigateUp()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreen(
    onBackBtnClick:()->Unit,
    state: TaskState,
    snackBarEvent: SharedFlow<SnackBarEvent>,
    onEvent:(TaskEvent)->Unit
) {
    var taskTitleError by rememberSaveable { mutableStateOf<String?>(null) }
    taskTitleError =when{
        state.taskTitle.isBlank() -> "Please enter the title."
        state.taskTitle.length<4 -> "Title is too short."
        state.taskTitle.length>30 -> "Title is too long."
        else -> null
    }
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDatePickerDialogOpen by rememberSaveable { mutableStateOf(false) }
    val datePickerState= rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )
    var isSubjectBottomSheetOpen by remember { mutableStateOf(false) }   //rememberSaveable not because not want it to open in configuration change
    val subBottomSheetState= rememberModalBottomSheetState()
    val scope= rememberCoroutineScope()    //for dismissing the bottom sheet when subject is clicked
    val snackBarHostState= remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        snackBarEvent.collectLatest { event->
            when(event){
                is SnackBarEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        message = event.msg,
                        duration = event.snackBarDuration
                    )
                }

                SnackBarEvent.navigateUp -> {
                    onBackBtnClick()
                }
            }

        }
    }

    deleteDialog(
        isOpen = isDeleteDialogOpen,
        title = "Delete Task",
        bodyText = "Are you sure, you want to delete this task? Your defined tasks will get reduced.\n\nThis action can't be undone.",
        onDismissRequest = { isDeleteDialogOpen=false },
        onConfirmClick =  {
            onEvent(TaskEvent.deleteTask)
            isDeleteDialogOpen=false
        }
    )
    TaskDatePicker(
        state = datePickerState,
        isOpen = isDatePickerDialogOpen,
        onDismissRequest = { isDatePickerDialogOpen = false },
        onConfirmBtnClick = {
            onEvent(TaskEvent.onDateChange(millis = datePickerState.selectedDateMillis))
            isDatePickerDialogOpen = false
        }
    )
    SubjectListBottomSheet(
        sheetState = subBottomSheetState,
        isOpen = isSubjectBottomSheetOpen,
        subjectList = state.subjects,
        onSubjectClicked = {
            //aq to documentation
            scope.launch { subBottomSheetState.hide() }.invokeOnCompletion {
                if(!subBottomSheetState.isVisible) isSubjectBottomSheetOpen=false
            }
            onEvent(TaskEvent.onRelatedToSubjectChange(subject = it))
        },
        onDismissRequest = {isSubjectBottomSheetOpen=false}
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            TaskScreenTopBar(
                isTaskExist = state.currentTaskId!=null,
                isComplete = state.isTaskComplete,
                checkBoxBorderColor = state.priority.color,
                onBackButtonClick = onBackBtnClick,
                onDeleteButtonClick = {isDeleteDialogOpen=true},
                onCheckBoxClick = { onEvent(TaskEvent.onIsCompleteChange) }
            ) 
        }
    ) { paddingValues ->
        Column(
            modifier= Modifier
                .verticalScroll(state = rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.taskTitle,
                label = { Text(text = "Title") },
                onValueChange = { onEvent(TaskEvent.onTitleChange(it)) },
                singleLine = true,
                isError = taskTitleError!=null && state.taskTitle.isNotBlank(),
                supportingText = { Text(text = taskTitleError.orEmpty()) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.taskDescription,
                label = { Text(text = "Description") },
                onValueChange = { onEvent(TaskEvent.onDescChange(it)) },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Due Date", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.dueDate.changeMillisToString(),  //extension function to convert millis to string
                    style = MaterialTheme.typography.bodyLarge,
                )
                IconButton(onClick = {isDatePickerDialogOpen=true}) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date of Completion"
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Priority", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Priority.entries.forEach{ priority->
                    priorityButton(
                        modifier = Modifier.weight(1f),
                        backgroundColor = priority.color,
                        label = priority.title,
                        onClick = { onEvent(TaskEvent.onPriorityChange(priority)) },
                        borderColor = if(priority== state.priority ){
                            Color.White
                        }else Color.Transparent,
                        labelColor = if(priority==state.priority){
                            Color.White
                        }else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "Related to Subject", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val firstSubject=state.subjects.firstOrNull()?.name?:""
                Text(
                    text = state.relatedToSubject ?: firstSubject, style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = {isSubjectBottomSheetOpen=true}) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Subject"
                    )
                }
            }
            Button(
                enabled = taskTitleError==null,
                onClick = {
                    onEvent(TaskEvent.saveTask)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                ){
                Text(text = "Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreenTopBar(
    isTaskExist: Boolean,
    isComplete: Boolean,
    checkBoxBorderColor: Color,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onCheckBoxClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back"
                )
            }
        },
        title={ Text(text = "Task", style = MaterialTheme.typography.headlineSmall) },
        actions = {
            if(isTaskExist){
                taskCheckBox(
                    isComplete = isComplete,
                    borderColor = checkBoxBorderColor,
                    onCheckBoxClick = onCheckBoxClick
                )
                IconButton(onClick = onDeleteButtonClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "delete"
                    )
                }
            }
        }

    )
}

@Composable
private fun priorityButton(
    modifier: Modifier = Modifier,
    backgroundColor:Color,
    label:String,
    onClick:()->Unit,
    borderColor:Color,
    labelColor:Color
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(5.dp)
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ){
        Text(text = label, color = labelColor)
    }
}
package com.example.studysmart.presentation.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.presentation.components.CountCard
import com.example.studysmart.presentation.components.StudySessionList
import com.example.studysmart.presentation.components.TasksList
import com.example.studysmart.presentation.components.addSubjDialog
import com.example.studysmart.presentation.components.deleteDialog
import com.example.studysmart.presentation.destinations.TaskScreenRouteDestination
import com.example.studysmart.presentation.task.TaskScreenNavArgs
import com.example.studysmart.utils.SnackBarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

data class SubjectScreenNavArgs(
    val subjectId:Int
)

@Destination(navArgsDelegate = SubjectScreenNavArgs::class)
@Composable
fun SubjectScreenRoute(
    navigator: DestinationsNavigator
) {
    val viewModel:SubjectScreenViewModel= hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SubjectScreen(
        state = state,
        snackBarEvent = viewModel.snackbarEventFlow,
        onEvent = viewModel::manageEventChanges,
        onBackBtnClick = {
            navigator.navigateUp()    //back to screen where we had come
        },
        onAddTaskBtnClick = {
            val navArg=TaskScreenNavArgs(taskId = null, subjectId = state.currentSubjectId) //here no need of subId
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onTaskCardClick = { taskId->
            val navArg= TaskScreenNavArgs(taskId = taskId, subjectId = null) //here no need of subId
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreen(
    state: SubjectState,
    onEvent:(SubjectEvent)->Unit,
    snackBarEvent: SharedFlow<SnackBarEvent>,
    onBackBtnClick:()->Unit,
    onAddTaskBtnClick:()->Unit,
    onTaskCardClick:(Int?)->Unit
) {

    val listState= rememberLazyListState()
    val isFABExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex==0 }  //for fab to reduce when 0th idx is passed
    }
    val scrollBehaviour=TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var isAddSubDialogOpen by rememberSaveable { mutableStateOf(false) }    //also remember value during orientation change / dark mode
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogOpenSub by rememberSaveable { mutableStateOf(false) }

    //for snackbar and attaching it to Scaffold
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

    LaunchedEffect(
        key1 = state.studiedHours,
        key2 = state.goalStudyHours
    ) {
        onEvent(SubjectEvent.updateProgress)
    }

    addSubjDialog(
        isOpen = isAddSubDialogOpen,
        onDismissRequest = { isAddSubDialogOpen = false },
        subjectName = state.subjectName,
        onSubjectNameChange = { onEvent(SubjectEvent.onSubjectNameChange(it)) },
        goalHrs = state.goalStudyHours,
        onGoalHrsChange = { onEvent(SubjectEvent.onSubjectGoalHrsChange(it)) },
        selectedColor = state.subjectCardColor,
        onColourChange = {onEvent(SubjectEvent.subjectCardColorChange(it))},
        onConfirmClick = {
            onEvent(SubjectEvent.updateSubject)
            isAddSubDialogOpen = false
        }
    )
    deleteDialog(
        isOpen = isDeleteDialogOpen,
        title = "Delete Session",
        bodyText = "Are you sure, you want to delete this session? Your studied hours will be reduced by your session time.\n\nThis action can't be undone.",
        onDismissRequest = { isDeleteDialogOpen=false },
        onConfirmClick =  {
            onEvent(SubjectEvent.deleteSession)
            isDeleteDialogOpen=false
        }
    )
    deleteDialog(
        isOpen = isDeleteDialogOpenSub,
        title = "Delete Subject",
        bodyText = "Are you sure, you want to delete this subject? Your all progress will be lost.\n\nThis action can't be undone.",
        onDismissRequest = { isDeleteDialogOpenSub=false },
        onConfirmClick =  {
            onEvent(SubjectEvent.deleteSubject)
            isDeleteDialogOpenSub=false
        }
    )
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),  //for reducing topAppBar when scrolled
        topBar = {
            SubjectScreenTopBar(
                title = state.subjectName,
                onBackClick = onBackBtnClick,
                onDeleteSubClick = { isDeleteDialogOpenSub = true },
                onEditSubClick = {isAddSubDialogOpen=true},
                scrollBehavior = scrollBehaviour
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddTaskBtnClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add task") },
                text = { Text(text = "Add Task") },
                expanded = isFABExpanded
                )
        }
    ) { paddingValues->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { 
                SubjectOverviewSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    studiedHours = state.studiedHours.toString(),
                    goalHours = state.goalStudyHours,
                    progress = state.progress
                )
            }
            TasksList(
                sectiontitle = "UPCOMING TASKS",
                tasksList = state.upcomingTasks,
                onTaskClick = onTaskCardClick,
                onCheckBoxClicked = {
                    onEvent(SubjectEvent.onTaskIsCompleteChange(it))
                }
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            TasksList(
                sectiontitle = "COMPLETED TASKS",
                tasksList = state.completedTasks,
                emptyListMsg = "No Task Completed.\n" +
                        "Click the checkbox on completing the task.",
                onTaskClick = onTaskCardClick,
                onCheckBoxClicked = { onEvent(SubjectEvent.onTaskIsCompleteChange(it))}
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            StudySessionList(
                sectiontitle = "RECENT STUDY SESSIONS",
                sessionsList = state.recentSessions,
                onDeleteIconClick = {
                    onEvent(SubjectEvent.onDeleteSessionBtnClick(it))
                    isDeleteDialogOpen=true
                }
            )
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun SubjectOverviewSection(modifier: Modifier,studiedHours:String,goalHours:String,progress:Float) {
    val percentProgress= remember (progress){   //remember -> only calculated when changes
        (progress*100).toInt().coerceIn(0,100)
    }
    Row (
        modifier=modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ){
        CountCard(
            modifier = Modifier.weight(1f),
            headingtext = "Goal Study Hours",
            count = goalHours
        )
        Spacer(Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingtext = "Studied Hours",
            count = studiedHours
        )
        Spacer(Modifier.width(10.dp))
        //Progress Indicator
        Box(
            modifier=Modifier.size(75.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier=Modifier.fillMaxSize(),
                progress = progress,
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                modifier=Modifier.fillMaxSize(),
                progress = progress,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
            Text("$percentProgress%")
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreenTopBar(
    title:String,
    onBackClick:()->Unit,
    onDeleteSubClick:()->Unit,
    onEditSubClick:()->Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back"
                )
            }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            IconButton(onClick = onDeleteSubClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "delete"
                )
            }
            IconButton(onClick = onEditSubClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "editSubject"
                )
            }
        }
    )
}
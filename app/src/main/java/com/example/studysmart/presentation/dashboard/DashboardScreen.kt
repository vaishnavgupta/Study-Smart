package com.example.studysmart.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.R
import com.example.studysmart.domain.models.Session
import com.example.studysmart.domain.models.Subjects
import com.example.studysmart.domain.models.Task
import com.example.studysmart.presentation.components.CountCard
import com.example.studysmart.presentation.components.StudySessionList
import com.example.studysmart.presentation.components.SubjectCard
import com.example.studysmart.presentation.components.TasksList
import com.example.studysmart.presentation.components.addSubjDialog
import com.example.studysmart.presentation.components.deleteDialog
import com.example.studysmart.presentation.destinations.SessionScreenRouteDestination
import com.example.studysmart.presentation.destinations.SubjectScreenRouteDestination
import com.example.studysmart.presentation.destinations.TaskScreenRouteDestination
import com.example.studysmart.presentation.subject.SubjectScreenNavArgs
import com.example.studysmart.presentation.task.TaskScreenNavArgs
import com.example.studysmart.utils.SnackBarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@RootNavGraph(start = true)
@Destination()
@Composable
fun DashboardScreenRoute(
    navigator:DestinationsNavigator
) {
    val viewModel:DashboardScreenViewModel= hiltViewModel()
    //defining state var
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    DashboardScreen(
        state=state,
        onEvent = viewModel::manageEventChanges,
        tasks = tasks,
        sessions=sessions,
        snackBarEvent = viewModel.snackbarEventFlow,
        onSubjectCardClick = { subjectId->
            subjectId?.let {
                val navArg=SubjectScreenNavArgs(subjectId = subjectId)
                navigator.navigate(SubjectScreenRouteDestination(navArgs = navArg))
            }
        },
        onTaskCardClick = { taskId->
            val navArg=TaskScreenNavArgs(taskId = taskId, subjectId = null) //here no need of subId
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onStartSessionBtnClick ={
            navigator.navigate(SessionScreenRouteDestination())
        }
    )
}

@Composable
private fun DashboardScreen(
    state:DashboardState,
    onEvent:(DashboardEvent)->Unit,
    tasks:List<Task>,
    snackBarEvent:SharedFlow<SnackBarEvent>,
    sessions:List<Session>,
    onSubjectCardClick:(Int?)->Unit,
    onTaskCardClick:(Int?)->Unit,
    onStartSessionBtnClick:()->Unit
) {
    var isAddSubDialogOpen by rememberSaveable { mutableStateOf(false) }    //also remember value during orientation change / dark mode
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }

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
                }
            }

        }
    }

    addSubjDialog(
        isOpen = isAddSubDialogOpen,
        onDismissRequest = { isAddSubDialogOpen = false },
        onConfirmClick = {
            onEvent(DashboardEvent.saveSubject)
            isAddSubDialogOpen = false
                         },
        subjectName = state.subjectName,
        onSubjectNameChange = {onEvent(DashboardEvent.onSubjectNameChange(it))},
        goalHrs = state.goalStudyHours,
        onGoalHrsChange = { onEvent(DashboardEvent.onSubjectGoalHrsChange(it)) },
        selectedColor = state.subjectCardColors,
        onColourChange = { onEvent(DashboardEvent.subjectCardColorChange(it)) }
    )
    deleteDialog(
        isOpen = isDeleteDialogOpen,
        title = "Delete Session",
        bodyText = "Are you sure, you want to delete this session? Your studied hours will be reduced by your session time.\n\nThis action can't be undone.",
        onDismissRequest = { isDeleteDialogOpen=false },
        onConfirmClick =  {
            onEvent(DashboardEvent.deleteSession)
            isDeleteDialogOpen=false
        }
    )

    //Scaffold heplps to place floatingAction Top Navigation at correct position
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = { DashboardScreenTopBat() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                CountCardsSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    subCont = state.totalSubjectCount,
                    stdHrsCnt = state.totalStudiedHours.toString(),
                    goalHrsCnt = state.totalGoalStudyHours.toString(),
                )
            }
            item {
                SubjectCardsSection(
                    modifier = Modifier.fillMaxWidth(),
                    subsList = state.subjectList,
                    onAddSubClick = { isAddSubDialogOpen = true },
                    onSubCardClick = onSubjectCardClick
                )
            }
            item {
                Button(
                    onClick = onStartSessionBtnClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 12.dp)

                ) {
                    Text("Start Study Session")
                }
            }
            TasksList(
                sectiontitle = "UPCOMING TASKS",
                tasksList = tasks,
                onTaskClick = onTaskCardClick,
                onCheckBoxClicked = { onEvent(DashboardEvent.onTaskIsCompleteChange(it)) }
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            StudySessionList(
                sectiontitle = "RECENT STUDY SESSIONS",
                sessionsList = sessions,
                onDeleteIconClick = {
                    onEvent(DashboardEvent.onDeleteSessionBtnClick(it))
                    isDeleteDialogOpen=true
                }
            )
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenTopBat() {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "StudySmart", style = MaterialTheme.typography.headlineMedium)
        }
    )
}


@Composable
private fun CountCardsSection(
    modifier: Modifier,
    subCont: Int,
    stdHrsCnt: String,
    goalHrsCnt: String
) {
    Row {
        CountCard(
            headingtext = "Subject Count",
            count = subCont.toString(),
            modifier = Modifier.weight(1f) //gives equal weight to all children
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            headingtext = "Studied Hours",
            count = stdHrsCnt.toString(),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            headingtext = "Goal Study Hours",
            count = goalHrsCnt.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubjectCardsSection(
    modifier: Modifier,
    subsList: List<Subjects>,
    onAddSubClick:()->Unit,
    onSubCardClick:(Int?)->Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SUBJECTS",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
            IconButton(onClick = onAddSubClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject"
                )
            }
        }

        if (subsList.isEmpty()) {
            Image(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(R.drawable.img_books),
                contentDescription = ""
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "No Subjects Added.\nClick the + button to add subject.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
            ) {
                items(subsList) { subject ->
                    SubjectCard(
                        subName = subject.name,
                        gradientColors = subject.colors.map { Color(it) },   //converting int to Color
                        onClick = {onSubCardClick(subject.subjectId)}
                    )
                }

            }
        }
    }
}
package com.example.studysmart.presentation.sessions

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.presentation.components.StudySessionList
import com.example.studysmart.presentation.components.SubjectListBottomSheet
import com.example.studysmart.presentation.components.deleteDialog
import com.example.studysmart.ui.theme.Red
import com.example.studysmart.utils.Constant.ACTION_SERVICE_CANCEL
import com.example.studysmart.utils.Constant.ACTION_SERVICE_START
import com.example.studysmart.utils.Constant.ACTION_SERVICE_STOP
import com.example.studysmart.utils.SnackBarEvent
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit

@Destination(
    deepLinks = [
        DeepLink(
        action = Intent.ACTION_VIEW,
        uriPattern = "study_smart://sessionScreen"
    )
    ]
)
@Composable
fun SessionScreenRoute(
    navigator: DestinationsNavigator,
    timerService: SessionTimerService
) {
    val viewModel:SessionScreenViewModel= hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SessionScreen(
        onBackBtnClick = { navigator.navigateUp() },
        timerService = timerService,
        state = state,
        onEvent = viewModel::manageEventChanges,
        snackBarEvent = viewModel.snackbarEventFlow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionScreen(
    state:SessionState,
    snackBarEvent: SharedFlow<SnackBarEvent>,
    onEvent:(SessionEvent) -> Unit,
    onBackBtnClick:()->Unit,
    timerService: SessionTimerService
) {

    val hours by timerService.hours
    val minutes by timerService.minutes
    val seconds by timerService.seconds
    val timerState by timerService.currentTimerState

    var isSubjectBottomSheetOpen by remember { mutableStateOf(false) }   //rememberSaveable not because not want it to open in configuration change
    val subBottomSheetState= rememberModalBottomSheetState()
    val scope= rememberCoroutineScope()    //for dismissing the bottom sheet when subject is clicked
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current  //compose way to get context

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

    // when notification of session is clicked then Subject & RelatedToSubject is lost

    //when subjectList is changes then it is called
    LaunchedEffect(key1 = state.subjects) {
        val subjectId=timerService.subjectId.value
        //calling onEvent to change relatedToSubject
        onEvent(SessionEvent.UpdateSubjectAndRelatedSession(
            subjectId = subjectId,
            relatedToSubject = state.subjects.find { it.subjectId == subjectId }?.name
        ))
    }

    SubjectListBottomSheet(
        sheetState = subBottomSheetState,
        isOpen = isSubjectBottomSheetOpen,
        subjectList = state.subjects,
        onSubjectClicked = { subject->
            //aq to documentation
            scope.launch { subBottomSheetState.hide() }.invokeOnCompletion {
                if(!subBottomSheetState.isVisible) isSubjectBottomSheetOpen=false
            }
            onEvent(SessionEvent.OnRelatedSubjectChange(subject))
        },
        onDismissRequest = {isSubjectBottomSheetOpen=false}
    )
    deleteDialog(
        isOpen = isDeleteDialogOpen,
        title = "Delete Study Session",
        bodyText = "Are you sure, you want to delete this session? Your studied time will be reduced.\n\nThis action can't be undone.",
        onDismissRequest = { isDeleteDialogOpen=false },
        onConfirmClick =  {
            onEvent(SessionEvent.DeleteSession)
            isDeleteDialogOpen=false
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            SessionScreenTopBar(onBackPressed = onBackBtnClick)
        }
    ) {paddingValues ->
        LazyColumn (
            modifier= Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            item {
                TimerSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    hrs = hours,
                    min = minutes,
                    sec = seconds  //aspectRatio keeps height in relation with width (here in 1:1 ratio)
                )
            }
            item{
                RelatedToSubjectSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    relatedToSubject = state.relatedToSubject ?: "",
                    selectSubjectOnClick = { isSubjectBottomSheetOpen = true },
                    seconds = seconds
                ) 
            }
            item {
                ButtonSection(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    startButtonClick = {
                        if(state.subjectId!=null && state.relatedToSubject!=null){
                            SessionServiceHelper.triggerForegroundService(
                                context = context,
                                action = if(timerState==TimerState.STARTED) ACTION_SERVICE_STOP
                                else ACTION_SERVICE_START
                            )
                            timerService.subjectId.value = state.subjectId
                        }
                        else{
                            onEvent(SessionEvent.NotifyToUpdateSubId)
                        }
                    },
                    cancelButtonClick = {
                        SessionServiceHelper.triggerForegroundService(
                            context = context,
                            action = ACTION_SERVICE_CANCEL
                        )
                    },
                    finishButtonClick = {
                        //getting duration from service
                        val duration=timerService.duration.toLong(DurationUnit.SECONDS)  //convert it in secs
                        if(duration >= 36){
                            SessionServiceHelper.triggerForegroundService(
                                context = context,
                                action = ACTION_SERVICE_CANCEL
                            )
                        }
                        onEvent(SessionEvent.SaveSession(duration))
                    },
                    timerState = timerState,
                    seconds = seconds
                )
            }
            StudySessionList(
                sectiontitle = "STUDY SESSIONS HISTORY",
                sessionsList = state.recentSessions,
                onDeleteIconClick = { session->
                    isDeleteDialogOpen=true
                    onEvent(SessionEvent.OnDeleteSessionBtnClick(session))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreenTopBar(onBackPressed:()->Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back"
                )
            }
        },
        title = {
            Text(text = "Study Sessions", style = MaterialTheme.typography.headlineSmall)
        }
    )
}

@Composable
private fun TimerSection(
    modifier: Modifier = Modifier,
    hrs:String,
    min:String,
    sec:String
) {
    Box (  //to keep timer and text at center
        modifier=modifier,
        contentAlignment = Alignment.Center
    ){
        Box (  //for circular shape
            modifier= Modifier
                .size(250.dp)
                .border(5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )
        Row {
            // for animating the text
            AnimatedContent(
                targetState = hrs,
                label = "hours"
            ) { hrs->
                Text(
                    text = "$hrs:",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 46.sp)
                )
            }
            AnimatedContent(
                targetState = min,
                label = "minutes"
            ) { min->
                Text(
                    text = "$min:",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 46.sp)
                )
            }
            AnimatedContent(
                targetState = sec,
                label = "seconds"
            ) { sec->
                Text(
                    text = sec,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 46.sp)
                )
            }
        }
    }
}

@Composable
private fun RelatedToSubjectSection(
    modifier: Modifier = Modifier,
    relatedToSubject:String,
    selectSubjectOnClick:()->Unit,
    seconds : String
) {
    Column (
        modifier=modifier
    ){
        Text(text = "Related to Subject", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = relatedToSubject, style = MaterialTheme.typography.bodyLarge
            )
            IconButton(
                onClick = selectSubjectOnClick,
                enabled = seconds=="00" 
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Subject"
                )
            }
        }
    }
}

@Composable
private fun ButtonSection(
    modifier: Modifier = Modifier,
    startButtonClick:()->Unit,
    cancelButtonClick:()->Unit,
    finishButtonClick:()->Unit,
    timerState: TimerState,
    seconds:String
) {
    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Button(
            onClick = cancelButtonClick,
            enabled = seconds!="00" && timerState!=TimerState.STARTED
        ) {
            Text(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp), text = "Cancel")
        }
        Button(
            onClick = startButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if(timerState==TimerState.STARTED) Red
                else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
                text = when(timerState){
                    TimerState.STARTED -> "Stop"
                    TimerState.STOPPED -> "Resume"
                    else -> "Start"
                }
            )
        }
        Button(
            onClick = finishButtonClick,
            enabled = seconds!="00" && timerState!=TimerState.STARTED
        ) {
            Text(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp), text = "Finish")
        }
    }
}
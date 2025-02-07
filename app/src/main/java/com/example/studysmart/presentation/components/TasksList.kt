package com.example.studysmart.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studysmart.R
import com.example.studysmart.domain.models.Task
import com.example.studysmart.utils.Priority
import com.example.studysmart.utils.changeMillisToString


fun LazyListScope.TasksList(
    sectiontitle: String,
    tasksList: List<Task>,
    onTaskClick:(Int?)->Unit,
    emptyListMsg:String="No Task Added.\nClick the + button to add new task.",
    onCheckBoxClicked:(Task)->Unit
) {
    item {
        Text(
            text = sectiontitle,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
    if (tasksList.isEmpty()) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.size(120.dp),
                    painter = painterResource(R.drawable.img_tasks),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = emptyListMsg,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    else{
        items(tasksList){ tsk->
            eachTaskCard(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                task = tsk,
                onClick = {onTaskClick(tsk.taskId)},
                onCheckBoxClicked = { onCheckBoxClicked(tsk) }
            ) 
        }
    }
}

@Composable
fun eachTaskCard(
    modifier: Modifier = Modifier,
    task: Task,
    onClick: () -> Unit,
    onCheckBoxClicked: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            taskCheckBox(
                isComplete = task.isCompleted,
                borderColor = Priority.fromIntToPriority(task.priority).color,
                onCheckBoxClick = onCheckBoxClicked
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = task.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,             //3 dots if overflow
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.dueDate.changeMillisToString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
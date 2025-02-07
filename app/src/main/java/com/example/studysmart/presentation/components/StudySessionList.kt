package com.example.studysmart.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studysmart.R
import com.example.studysmart.domain.models.Session
import com.example.studysmart.utils.changeMillisToString
import com.example.studysmart.utils.toHours

fun LazyListScope.StudySessionList(
    sectiontitle: String,
    sessionsList: List<Session>,
    onDeleteIconClick:(Session)->Unit
) {
    item {
        Text(
            text = sectiontitle,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
    if (sessionsList.isEmpty()) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.size(120.dp),
                    painter = painterResource(R.drawable.img_lamp),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Study Session Added.\nClick the + button to start new session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    else{
        items(sessionsList){ session->
            eachSessionCard(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                session = session,
                onDeleteClick = {onDeleteIconClick(session)}
            )
        }
    }


}


@Composable
fun eachSessionCard(
    modifier: Modifier = Modifier,
    session: Session,
    onDeleteClick:()->Unit
) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = session.relatedToSub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,             //3 dots if overflow
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session.date.changeMillisToString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${session.duration.toHours()} hr",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = {onDeleteClick()}) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Session"
                )
            }
        }
    }
}

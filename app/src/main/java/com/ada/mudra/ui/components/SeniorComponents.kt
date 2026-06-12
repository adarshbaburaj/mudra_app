package com.ada.mudra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ada.mudra.core.Haptics
import java.io.File

/** Set once at the app root from caregiver settings; read by every senior tap. */
val LocalHapticsEnabled = staticCompositionLocalOf { true }

/** Wraps a click handler with the strong confirmation haptic. */
@Composable
fun rememberSeniorTap(onClick: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val hapticsEnabled = LocalHapticsEnabled.current
    return {
        if (hapticsEnabled) Haptics.strongTap(context)
        onClick()
    }
}

/** Full-width home tile, min 112dp tall, icon + always-visible text label. */
@Composable
fun GiantTile(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = rememberSeniorTap(onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 112.dp)
            .testTag("tile_$label"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(start = 20.dp)
                    .weight(1f),
            )
        }
    }
}

/** Primary action button, min 88dp tall. */
@Composable
fun GiantButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    outlined: Boolean = false,
) {
    Surface(
        onClick = rememberSeniorTap(onClick),
        shape = MaterialTheme.shapes.large,
        color = if (outlined) MaterialTheme.colorScheme.surface else containerColor,
        contentColor = if (outlined) MaterialTheme.colorScheme.onSurface else contentColor,
        border = if (outlined) BorderStroke(3.dp, MaterialTheme.colorScheme.outline) else null,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 88.dp)
            .testTag("button_$label"),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Contact photo if the caregiver added one, otherwise a big initial letter. */
@Composable
fun ContactAvatar(
    name: String,
    photoFile: File?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Int = 72,
) {
    if (photoFile != null && photoFile.exists()) {
        AsyncImage(
            model = photoFile,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
        )
    } else {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = modifier.size(size.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.trim().take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Big tappable card for one trusted contact: photo, name and relation. */
@Composable
fun ContactCard(
    name: String,
    relation: String,
    photoFile: File?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = rememberSeniorTap(onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 104.dp)
            .testTag("contact_$name"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp),
        ) {
            ContactAvatar(
                name = name,
                photoFile = photoFile,
                contentDescription = contentDescription,
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(start = 20.dp)
                    .weight(1f),
            ) {
                Text(text = name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = relation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

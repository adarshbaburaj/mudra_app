package com.ada.mudra.features.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.ui.components.GiantButton

/**
 * Read-only senior gallery. By design there is no share, forward, upload,
 * delete or story control anywhere on this screen.
 */
@Composable
fun GalleryScreen(
    viewModel: MudraViewModel,
    onGoHome: () -> Unit,
) {
    val photos by viewModel.photos.collectAsState()
    var index by remember { mutableIntStateOf(0) }

    val safeIndex = index.coerceIn(0, (photos.size - 1).coerceAtLeast(0))
    val current = photos.getOrNull(safeIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.gallery_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        if (current == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.gallery_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            AsyncImage(
                model = viewModel.photoFile(current),
                contentDescription = current.caption.ifBlank {
                    stringResource(R.string.cd_family_photo)
                },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            if (current.caption.isNotBlank()) {
                Text(
                    text = current.caption,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text = stringResource(R.string.gallery_counter, safeIndex + 1, photos.size),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (safeIndex > 0) {
                    GiantButton(
                        label = stringResource(R.string.previous_photo),
                        onClick = { index = safeIndex - 1 },
                        outlined = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (safeIndex < photos.size - 1) {
                    GiantButton(
                        label = stringResource(R.string.next_photo),
                        onClick = { index = safeIndex + 1 },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        GiantButton(
            label = stringResource(R.string.go_home),
            onClick = onGoHome,
            outlined = true,
        )
    }
}

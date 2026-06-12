package com.ada.mudra.features.caregiver

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.data.model.GalleryPhoto
import com.ada.mudra.ui.components.GiantButton

@Composable
fun PhotoManagerScreen(
    viewModel: MudraViewModel,
    onGoBack: () -> Unit,
) {
    val photos by viewModel.photos.collectAsState()
    var pendingDelete by remember { mutableStateOf<GalleryPhoto?>(null) }
    var captionTarget by remember { mutableStateOf<GalleryPhoto?>(null) }
    var captionDraft by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.addPhoto(it) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.caregiver_photos),
            style = MaterialTheme.typography.headlineMedium,
        )

        GiantButton(
            label = stringResource(R.string.photos_add),
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(photos, key = { it.id }) { photo ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        AsyncImage(
                            model = viewModel.photoFile(photo),
                            contentDescription = photo.caption.ifBlank {
                                stringResource(R.string.cd_family_photo)
                            },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(MaterialTheme.shapes.small),
                        )
                        Text(
                            text = photo.caption.ifBlank {
                                stringResource(R.string.photo_no_caption)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .weight(1f),
                        )
                        IconButton(
                            onClick = {
                                captionTarget = photo
                                captionDraft = photo.caption
                            },
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.photo_caption_edit),
                            )
                        }
                        IconButton(
                            onClick = { pendingDelete = photo },
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        GiantButton(
            label = stringResource(R.string.go_back),
            onClick = onGoBack,
            outlined = true,
        )
    }

    pendingDelete?.let { photo ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_photo_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePhoto(photo.id)
                        pendingDelete = null
                    },
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    captionTarget?.let { photo ->
        AlertDialog(
            onDismissRequest = { captionTarget = null },
            title = { Text(stringResource(R.string.photo_caption_edit)) },
            text = {
                OutlinedTextField(
                    value = captionDraft,
                    onValueChange = { captionDraft = it },
                    label = { Text(stringResource(R.string.photo_caption)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updatePhotoCaption(photo.id, captionDraft.trim())
                        captionTarget = null
                    },
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { captionTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

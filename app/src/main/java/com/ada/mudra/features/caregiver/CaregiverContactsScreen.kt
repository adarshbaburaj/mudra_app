package com.ada.mudra.features.caregiver

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.data.model.TrustedContact
import com.ada.mudra.ui.components.ContactAvatar
import com.ada.mudra.ui.components.GiantButton

@Composable
fun CaregiverContactsScreen(
    viewModel: MudraViewModel,
    onAddContact: () -> Unit,
    onEditContact: (TrustedContact) -> Unit,
    onGoBack: () -> Unit,
) {
    val contacts by viewModel.contacts.collectAsState()
    var pendingDelete by remember { mutableStateOf<TrustedContact?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.caregiver_contacts),
            style = MaterialTheme.typography.headlineMedium,
        )

        GiantButton(
            label = stringResource(R.string.contact_add),
            onClick = onAddContact,
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(contacts, key = { it.id }) { contact ->
                Surface(
                    onClick = { onEditContact(contact) },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        ContactAvatar(
                            name = contact.displayName,
                            photoFile = viewModel.avatarFile(contact),
                            contentDescription = stringResource(
                                R.string.cd_contact_photo, contact.displayName
                            ),
                            size = 56,
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f),
                        ) {
                            Text(
                                text = "${contact.displayName} · ${contact.relationLabel}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = contact.phoneE164,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = { pendingDelete = contact },
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

    pendingDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text(stringResource(R.string.delete_contact_confirm, contact.displayName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteContact(contact.id)
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
}

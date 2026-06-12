package com.ada.mudra.features.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.data.model.TrustedContact
import com.ada.mudra.ui.components.ContactCard
import com.ada.mudra.ui.components.GiantButton

enum class ContactListMode { CALL, WHATSAPP }

@Composable
fun ContactsScreen(
    viewModel: MudraViewModel,
    mode: ContactListMode,
    onContactChosen: (TrustedContact) -> Unit,
    onGoHome: () -> Unit,
) {
    val contacts by viewModel.contacts.collectAsState()
    val visibleContacts = contacts.filter { contact ->
        contact.isActive && when (mode) {
            ContactListMode.CALL -> contact.canPhoneCall
            ContactListMode.WHATSAPP -> contact.canWhatsApp
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(
                when (mode) {
                    ContactListMode.CALL -> R.string.contacts_call_title
                    ContactListMode.WHATSAPP -> R.string.contacts_whatsapp_title
                }
            ),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (visibleContacts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.contacts_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(visibleContacts, key = { it.id }) { contact ->
                    ContactCard(
                        name = contact.displayName,
                        relation = contact.relationLabel,
                        photoFile = viewModel.avatarFile(contact),
                        contentDescription = stringResource(
                            R.string.cd_contact_photo, contact.displayName
                        ),
                        onClick = { onContactChosen(contact) },
                    )
                }
            }
        }

        GiantButton(
            label = stringResource(R.string.go_home),
            onClick = onGoHome,
            outlined = true,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

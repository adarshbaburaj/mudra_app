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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.core.PhoneNumbers
import com.ada.mudra.data.model.TrustedContact
import com.ada.mudra.ui.components.ContactAvatar
import com.ada.mudra.ui.components.GiantButton
import java.util.UUID

@Composable
fun ContactEditScreen(
    viewModel: MudraViewModel,
    contactId: String?,
    onDone: () -> Unit,
) {
    val existing = viewModel.contactById(contactId)

    var name by remember(existing) { mutableStateOf(existing?.displayName ?: "") }
    var relation by remember(existing) { mutableStateOf(existing?.relationLabel ?: "") }
    var phone by remember(existing) { mutableStateOf(existing?.phoneE164 ?: "") }
    var whatsapp by remember(existing) { mutableStateOf(existing?.whatsappE164 ?: "") }
    var photoFileName by remember(existing) { mutableStateOf(existing?.photoFileName) }
    var canPhoneCall by remember(existing) { mutableStateOf(existing?.canPhoneCall ?: true) }
    var canWhatsApp by remember(existing) { mutableStateOf(existing?.canWhatsApp ?: true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.saveContactAvatar(it) { saved -> photoFileName = saved } }
    }

    val nameError = stringResource(R.string.error_name_required)
    val relationError = stringResource(R.string.error_relation_required)
    val phoneError = stringResource(R.string.error_phone_invalid)
    val whatsappError = stringResource(R.string.error_whatsapp_invalid)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(
                if (existing == null) R.string.contact_add else R.string.contact_edit
            ),
            style = MaterialTheme.typography.headlineMedium,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ContactAvatar(
                name = name.ifBlank { "?" },
                photoFile = photoFileName?.let { viewModel.avatarFileByName(it) },
                contentDescription = stringResource(R.string.cd_contact_photo, name),
                size = 80,
            )
            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            ) {
                Text(
                    text = stringResource(
                        if (photoFileName == null) R.string.contact_photo_add
                        else R.string.contact_photo_change
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.contact_name)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = relation,
            onValueChange = { relation = it },
            label = { Text(stringResource(R.string.contact_relation)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.contact_phone)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text(stringResource(R.string.contact_whatsapp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = canPhoneCall, onCheckedChange = { canPhoneCall = it })
            Text(
                text = stringResource(R.string.contact_can_call),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = canWhatsApp, onCheckedChange = { canWhatsApp = it })
            Text(
                text = stringResource(R.string.contact_can_whatsapp),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        errorText?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }

        GiantButton(
            label = stringResource(R.string.save),
            onClick = {
                val normalizedPhone = PhoneNumbers.normalize(phone)
                val normalizedWhatsApp = PhoneNumbers.normalize(whatsapp)
                errorText = when {
                    name.isBlank() -> nameError
                    relation.isBlank() -> relationError
                    !PhoneNumbers.isValidE164(normalizedPhone) -> phoneError
                    normalizedWhatsApp.isNotBlank() &&
                        !PhoneNumbers.isValidE164(normalizedWhatsApp) -> whatsappError
                    else -> null
                }
                if (errorText == null) {
                    viewModel.upsertContact(
                        TrustedContact(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            displayName = name.trim(),
                            relationLabel = relation.trim(),
                            phoneE164 = normalizedPhone,
                            whatsappE164 = normalizedWhatsApp.ifBlank { null },
                            photoFileName = photoFileName,
                            canPhoneCall = canPhoneCall,
                            canWhatsApp = canWhatsApp,
                            sortOrder = existing?.sortOrder ?: 0,
                        )
                    )
                    onDone()
                }
            },
        )
        GiantButton(
            label = stringResource(R.string.cancel),
            onClick = onDone,
            outlined = true,
        )
    }
}

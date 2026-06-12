package com.ada.mudra.features.seniorhome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ada.mudra.R
import com.ada.mudra.ui.components.GiantTile
import com.ada.mudra.ui.components.rememberSeniorTap
import com.ada.mudra.ui.theme.CallGreen
import com.ada.mudra.ui.theme.PhotoPurple
import com.ada.mudra.ui.theme.WhatsAppGreen

@Composable
fun SeniorHomeScreen(
    onCallFamily: () -> Unit,
    onWhatsAppFamily: () -> Unit,
    onFamilyPhotos: () -> Unit,
    onFamilySetup: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.home_question),
            style = MaterialTheme.typography.headlineMedium,
        )

        GiantTile(
            label = stringResource(R.string.tile_call_family),
            icon = Icons.Filled.Call,
            containerColor = CallGreen,
            onClick = onCallFamily,
            modifier = Modifier.weight(1f),
        )
        GiantTile(
            label = stringResource(R.string.tile_whatsapp_family),
            icon = Icons.Filled.Send,
            containerColor = WhatsAppGreen,
            onClick = onWhatsAppFamily,
            modifier = Modifier.weight(1f),
        )
        GiantTile(
            label = stringResource(R.string.tile_family_photos),
            icon = Icons.Filled.Favorite,
            containerColor = PhotoPurple,
            onClick = onFamilyPhotos,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.weight(0.05f))

        // Caregiver entrance: deliberately quiet, but still a 64dp+ target.
        OutlinedButton(
            onClick = rememberSeniorTap(onFamilySetup),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .defaultMinSize(minHeight = 64.dp)
                .testTag("button_family_setup"),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.family_setup),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.family_setup_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

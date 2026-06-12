package com.ada.mudra.features.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ada.mudra.R
import com.ada.mudra.ui.components.GiantButton

@Composable
fun CaregiverHomeScreen(
    onContacts: () -> Unit,
    onPhotos: () -> Unit,
    onSettings: () -> Unit,
    onAccount: () -> Unit,
    onBackToSenior: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.caregiver_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        GiantButton(
            label = stringResource(R.string.caregiver_contacts),
            onClick = onContacts,
        )
        GiantButton(
            label = stringResource(R.string.caregiver_photos),
            onClick = onPhotos,
        )
        GiantButton(
            label = stringResource(R.string.caregiver_settings),
            onClick = onSettings,
        )
        GiantButton(
            label = stringResource(R.string.account_title),
            onClick = onAccount,
        )

        Spacer(modifier = Modifier.weight(1f))

        GiantButton(
            label = stringResource(R.string.caregiver_back_senior),
            onClick = onBackToSenior,
            outlined = true,
        )
    }
}

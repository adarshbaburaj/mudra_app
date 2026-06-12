package com.ada.mudra.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ada.mudra.MudraViewModel
import com.ada.mudra.features.calling.CallConfirmScreen
import com.ada.mudra.features.caregiver.CaregiverHomeScreen
import com.ada.mudra.features.caregiver.CaregiverContactsScreen
import com.ada.mudra.features.caregiver.ContactEditScreen
import com.ada.mudra.features.caregiver.PhotoManagerScreen
import com.ada.mudra.features.caregiver.PinScreen
import com.ada.mudra.features.caregiver.SettingsScreen
import com.ada.mudra.features.contacts.ContactListMode
import com.ada.mudra.features.contacts.ContactsScreen
import com.ada.mudra.features.gallery.GalleryScreen
import com.ada.mudra.features.seniorhome.SeniorHomeScreen
import com.ada.mudra.features.whatsapp.WhatsAppConfirmScreen

object Routes {
    const val HOME = "home"
    const val CONTACTS_CALL = "contacts/call"
    const val CONTACTS_WHATSAPP = "contacts/whatsapp"
    const val CALL_CONFIRM = "confirm/call/{contactId}"
    const val WA_CONFIRM = "confirm/whatsapp/{contactId}"
    const val GALLERY = "gallery"
    const val PIN = "pin"
    const val CAREGIVER = "caregiver"
    const val CAREGIVER_CONTACTS = "caregiver/contacts"
    const val CONTACT_EDIT = "caregiver/contact?id={contactId}"
    const val CAREGIVER_PHOTOS = "caregiver/photos"
    const val CAREGIVER_SETTINGS = "caregiver/settings"

    fun callConfirm(contactId: String) = "confirm/call/$contactId"
    fun waConfirm(contactId: String) = "confirm/whatsapp/$contactId"
    fun contactEdit(contactId: String?) =
        if (contactId == null) "caregiver/contact" else "caregiver/contact?id=$contactId"
}

/** Pops everything back to the senior home screen — the always-safe escape. */
fun NavHostController.goHome() {
    popBackStack(Routes.HOME, inclusive = false)
}

@Composable
fun MudraNavHost(viewModel: MudraViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        composable(Routes.HOME) {
            SeniorHomeScreen(
                onCallFamily = { navController.navigate(Routes.CONTACTS_CALL) },
                onWhatsAppFamily = { navController.navigate(Routes.CONTACTS_WHATSAPP) },
                onFamilyPhotos = { navController.navigate(Routes.GALLERY) },
                onFamilySetup = { navController.navigate(Routes.PIN) },
            )
        }

        composable(Routes.CONTACTS_CALL) {
            ContactsScreen(
                viewModel = viewModel,
                mode = ContactListMode.CALL,
                onContactChosen = { navController.navigate(Routes.callConfirm(it.id)) },
                onGoHome = { navController.goHome() },
            )
        }

        composable(Routes.CONTACTS_WHATSAPP) {
            ContactsScreen(
                viewModel = viewModel,
                mode = ContactListMode.WHATSAPP,
                onContactChosen = { navController.navigate(Routes.waConfirm(it.id)) },
                onGoHome = { navController.goHome() },
            )
        }

        composable(Routes.CALL_CONFIRM) { backStackEntry ->
            CallConfirmScreen(
                viewModel = viewModel,
                contactId = backStackEntry.arguments?.getString("contactId"),
                onGoBack = { navController.popBackStack() },
                onGoHome = { navController.goHome() },
            )
        }

        composable(Routes.WA_CONFIRM) { backStackEntry ->
            WhatsAppConfirmScreen(
                viewModel = viewModel,
                contactId = backStackEntry.arguments?.getString("contactId"),
                onGoBack = { navController.popBackStack() },
                onGoHome = { navController.goHome() },
            )
        }

        composable(Routes.GALLERY) {
            GalleryScreen(
                viewModel = viewModel,
                onGoHome = { navController.goHome() },
            )
        }

        composable(Routes.PIN) {
            PinScreen(
                viewModel = viewModel,
                onPinAccepted = {
                    navController.navigate(Routes.CAREGIVER) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                },
                onGoBack = { navController.popBackStack() },
            )
        }

        composable(Routes.CAREGIVER) {
            CaregiverHomeScreen(
                onContacts = { navController.navigate(Routes.CAREGIVER_CONTACTS) },
                onPhotos = { navController.navigate(Routes.CAREGIVER_PHOTOS) },
                onSettings = { navController.navigate(Routes.CAREGIVER_SETTINGS) },
                onBackToSenior = { navController.goHome() },
            )
        }

        composable(Routes.CAREGIVER_CONTACTS) {
            CaregiverContactsScreen(
                viewModel = viewModel,
                onAddContact = { navController.navigate(Routes.contactEdit(null)) },
                onEditContact = { navController.navigate(Routes.contactEdit(it.id)) },
                onGoBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.CONTACT_EDIT,
            arguments = listOf(
                navArgument("contactId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
        ) { backStackEntry ->
            ContactEditScreen(
                viewModel = viewModel,
                contactId = backStackEntry.arguments?.getString("contactId"),
                onDone = { navController.popBackStack() },
            )
        }

        composable(Routes.CAREGIVER_PHOTOS) {
            PhotoManagerScreen(
                viewModel = viewModel,
                onGoBack = { navController.popBackStack() },
            )
        }

        composable(Routes.CAREGIVER_SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onGoBack = { navController.popBackStack() },
            )
        }
    }
}

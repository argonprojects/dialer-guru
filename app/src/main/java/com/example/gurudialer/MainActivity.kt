package com.example.gurudialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var contactsRepository: ContactsRepository
    private lateinit var soundAndSpeechHelper: SoundAndSpeechHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactsRepository = ContactsRepository(applicationContext)
        soundAndSpeechHelper = SoundAndSpeechHelper(applicationContext)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            var contacts by remember { mutableStateOf<List<GuruContact>>(emptyList()) }
            var isDefaultDialer by remember { mutableStateOf(checkIsDefaultDialer()) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions[Manifest.permission.READ_CONTACTS] == true) {
                    coroutineScope.launch {
                        contacts = contactsRepository.loadContacts()
                    }
                }
            }

            val roleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                isDefaultDialer = checkIsDefaultDialer()
            }

            LaunchedEffect(Unit) {
                val hasContactsPerm = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED

                val hasCallPerm = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED

                if (hasContactsPerm && hasCallPerm) {
                    contacts = contactsRepository.loadContacts()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.READ_PHONE_STATE
                        )
                    )
                }
            }

            DialerScreen(
                contacts = contacts,
                isDefaultDialer = isDefaultDialer,
                onRequestDefaultDialer = {
                    requestDefaultDialerRole(roleLauncher::launch)
                },
                onContactSelected = { contact ->
                    soundAndSpeechHelper.onContactPhotoTapped(contact.name)
                },
                onConfirmCall = { contact ->
                    soundAndSpeechHelper.onCallConfirmed(contact.name)
                    placeDirectCall(contact.phoneNumber)
                },
                onCancelSelection = {
                    soundAndSpeechHelper.onCallCancelled()
                }
            )
        }
    }

    private fun checkIsDefaultDialer(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.defaultDialerPackage == packageName
        }
    }

    private fun requestDefaultDialerRole(launchIntent: (Intent) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                launchIntent(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
            }
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
            launchIntent(intent)
        }
    }

    private fun placeDirectCall(phoneNumber: String) {
        val uri = Uri.fromParts("tel", phoneNumber, null)
        val hasCallPerm = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCallPerm) {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.placeCall(uri, Bundle())
        } else {
            startActivity(Intent(Intent.ACTION_DIAL, uri))
        }
    }

    override fun onDestroy() {
        soundAndSpeechHelper.shutdown()
        super.onDestroy()
    }
}

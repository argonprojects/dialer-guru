package com.example.gurudialer

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Represents a single contact card displayed in the 2-column classic photo grid.
 */
data class GuruContact(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val photoUri: Uri?,
    val isStarred: Boolean
)

/**
 * Automatically queries the Android Contacts provider for contacts with phone numbers,
 * sorting starred (favorite) contacts and contacts with photos to the top.
 */
class ContactsRepository(private val context: Context) {

    suspend fun loadContacts(): List<GuruContact> = withContext(Dispatchers.IO) {
        val contactsMap = LinkedHashMap<Long, GuruContact>()
        val resolver: ContentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )

        // Sort starred contacts first, then contacts with photos, then alphabetically
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.STARRED} DESC, " +
                "${ContactsContract.CommonDataKinds.Phone.PHOTO_URI} DESC, " +
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"

        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER} > 0",
            null,
            sortOrder
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val numberIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val starredIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.STARRED)

            while (cursor.moveToNext()) {
                val contactId = cursor.getLong(idIdx)
                // Deduplicate if a contact has multiple phone numbers
                if (contactsMap.containsKey(contactId)) continue

                val name = cursor.getString(nameIdx) ?: "Unknown"
                val rawNumber = cursor.getString(numberIdx) ?: continue
                val photoUriString = cursor.getString(photoIdx)
                val isStarred = cursor.getInt(starredIdx) == 1

                // Prefer high-res display photo URI via ContactsContract.Contacts
                val contactUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    contactId
                )
                val highResPhotoUri = photoUriString?.let { Uri.parse(it) }
                    ?: Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)

                contactsMap[contactId] = GuruContact(
                    id = contactId,
                    name = name,
                    phoneNumber = rawNumber.trim(),
                    photoUri = if (photoUriString != null) highResPhotoUri else null,
                    isStarred = isStarred
                )
            }
        }

        contactsMap.values.toList()
    }
}

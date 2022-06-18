package nl.marc.thecircle.data

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val userName = stringPreferencesKey("USER_NAME")

    val userId = stringPreferencesKey("USER_ID")

    val privateKey = stringPreferencesKey("PRIVATE_KEY")
}

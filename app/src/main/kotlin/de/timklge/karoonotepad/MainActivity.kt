package de.timklge.karoonotepad

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import de.timklge.karoonotepad.screens.MainScreen
import de.timklge.karoonotepad.theme.AppTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notes", corruptionHandler = ReplaceFileCorruptionHandler {
    Log.w(KarooNotepadExtension.TAG, "Error reading notes, using default values")
    emptyPreferences()
})

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                MainScreen() {
                    finish()
                }
            }
        }
    }
}

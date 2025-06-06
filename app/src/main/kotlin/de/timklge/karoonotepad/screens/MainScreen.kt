package de.timklge.karoonotepad.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.timklge.karoonotepad.R
import de.timklge.karoonotepad.dataStore
import de.timklge.karoonotepad.streamPreferences
import io.hammerhead.karooext.KarooSystemService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val preferencesKey = stringPreferencesKey("notes")

suspend fun saveNotes(context: Context, notes: MutableList<Note>) {
    context.dataStore.edit { t ->
        t[preferencesKey] = Json.encodeToString(notes)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onFinish: () -> Unit) {
    var karooConnected by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val karooSystem = remember { KarooSystemService(ctx) }
    var note by remember { mutableStateOf("") }
    var showWarnings by remember { mutableStateOf(false) }
    var coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        note = ctx.streamPreferences().firstOrNull()?.firstOrNull()?.text ?: ""
    }

    LaunchedEffect(Unit) {
        delay(1000L)
        showWarnings = true
    }

    LaunchedEffect(Unit) {
        karooSystem.connect { connected ->
            karooConnected = connected
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runBlocking {
                saveNotes(ctx, mutableListOf(Note(0, "Default", note)))
            }
            karooSystem.disconnect()
        }
    }

    var saveJob by remember { mutableStateOf<Job?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = {Text("Notepad")}) },
        content = {
            Box(Modifier.fillMaxSize().padding(it)){
                TextField(note, modifier = Modifier.padding(start = 10.dp, bottom = (10 + 54 + 10).dp, top = 10.dp, end = 10.dp).fillMaxSize(), onValueChange = {
                    note = it
                    if (saveJob?.isActive == true) {
                        saveJob?.cancel()
                    }
                    saveJob = coroutineScope.launch {
                        delay(2_000L) // Debounce to avoid too many writes
                        saveNotes(ctx, mutableListOf(Note(0, "Default", note)))
                    }
                })

                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 10.dp)
                        .size(54.dp)
                        .clickable {
                            onFinish()
                        }
                )

                /* Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 10.dp)
                        .size(54.dp)
                        .clickable {
                            // TODO Add
                        }
                ) */
            }
        }
    )
}

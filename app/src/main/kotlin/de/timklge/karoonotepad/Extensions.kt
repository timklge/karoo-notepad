package de.timklge.karoonotepad

import android.content.Context
import android.util.Log
import de.timklge.karoonotepad.KarooNotepadExtension.Companion.TAG
import de.timklge.karoonotepad.screens.Note
import de.timklge.karoonotepad.screens.defaultNotes
import de.timklge.karoonotepad.screens.preferencesKey
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.ActiveRideProfile
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

fun KarooSystemService.streamDataFlow(dataTypeId: String): Flow<StreamState> {
    return callbackFlow {
        val listenerId = addConsumer(OnStreamState.StartStreaming(dataTypeId)) { event: OnStreamState ->
            trySendBlocking(event.state)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}

fun KarooSystemService.streamRideState(): Flow<RideState> {
    return callbackFlow {
        val listenerId = addConsumer { rideState: RideState ->
            trySendBlocking(rideState)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}

fun KarooSystemService.streamActiveRideProfile(): Flow<ActiveRideProfile> {
    return callbackFlow {
        val listenerId = addConsumer { activeProfile: ActiveRideProfile ->
            trySendBlocking(activeProfile)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}

fun KarooSystemService.streamUserProfile(): Flow<UserProfile> {
    return callbackFlow {
        val listenerId = addConsumer { userProfile: UserProfile ->
            trySendBlocking(userProfile)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}

fun Context.streamPreferences(): Flow<MutableList<Note>> {
    return applicationContext.dataStore.data.map { notesJson ->
        try {
            Json.decodeFromString<MutableList<Note>>(
                notesJson[preferencesKey] ?: defaultNotes
            )
        } catch(e: Throwable){
            Log.e(TAG,"Failed to read notes", e)
            mutableListOf()
        }
    }
}
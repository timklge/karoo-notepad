package de.timklge.karoonotepad

import android.util.Log
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension

class KarooNotepadExtension : KarooExtension("karoo-notepad", BuildConfig.VERSION_NAME) {
    companion object {
        const val TAG = "karoo-notepad"
    }

    private lateinit var karooSystem: KarooSystemService

    override val types by lazy {
        listOf(
            NotepadDataType(karooSystem, applicationContext),
            NotepadButtonDataType(karooSystem, applicationContext),
        )
    }

    override fun onCreate() {
        super.onCreate()

        karooSystem = KarooSystemService(applicationContext)

        karooSystem.connect { connected ->
            if (connected) {
                Log.i(TAG, "Connected")
            }
        }
    }

    override fun onDestroy() {
        karooSystem.disconnect()
        super.onDestroy()
    }
}

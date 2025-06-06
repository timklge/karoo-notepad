package de.timklge.karoonotepad

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.glance.color.ColorProvider
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ShowCustomStreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class NotepadDataType(
    private val karooSystem: KarooSystemService,
    private val applicationContext: Context
) : DataTypeImpl("karoo-notepad", "notepad") {
    private val glance = GlanceRemoteViews()

    @OptIn(FlowPreview::class)
    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        Log.d(KarooNotepadExtension.TAG, "Starting notepad view with $emitter")

        val configJob = CoroutineScope(Dispatchers.IO).launch {
            emitter.onNext(UpdateGraphicConfig(showHeader = false))
            emitter.onNext(ShowCustomStreamState(message = "", color = null))
            awaitCancellation()
        }

        val viewJob = CoroutineScope(Dispatchers.IO).launch {
            context.streamPreferences().collect { notes ->
                val result = glance.compose(context, DpSize.Unspecified) {
                    Text(
                        notes.firstOrNull()?.text ?: "",
                        modifier = GlanceModifier.fillMaxSize().padding(10.dp),
                        style = TextStyle(
                            color = ColorProvider(Color.Black, Color.White),
                            fontSize = 20.sp)
                    )
                }

                emitter.updateView(result.remoteViews)
            }
        }

        emitter.setCancellable {
            Log.d(KarooNotepadExtension.TAG, "Stopping headwind view with $emitter")
            configJob.cancel()
            viewJob.cancel()
        }
    }
}
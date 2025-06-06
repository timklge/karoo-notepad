package de.timklge.karoonotepad

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ShowCustomStreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class NotepadButtonDataType(
    private val karooSystem: KarooSystemService,
    private val applicationContext: Context
) : DataTypeImpl("karoo-notepad", "notepadButton") {
    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        Log.d(KarooNotepadExtension.TAG, "Starting notepad button view with $emitter")

        val configJob = CoroutineScope(Dispatchers.IO).launch {
            emitter.onNext(UpdateGraphicConfig(showHeader = false))
            emitter.onNext(ShowCustomStreamState(message = "", color = null))
            awaitCancellation()
        }

        val viewJob = CoroutineScope(Dispatchers.IO).launch {
            val result = glance.compose(context, DpSize.Unspecified) {
                val modifier = GlanceModifier.fillMaxSize()

                Box(
                    modifier = if (config.preview) modifier else modifier.clickable(
                        actionStartActivity<MainActivity>()
                    )
                ) {
                    Image(
                        modifier = GlanceModifier.fillMaxSize().padding(10.dp),
                        provider = ImageProvider(R.drawable.notepad),
                        contentDescription = "Notepad",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(ColorProvider(Color.Black, Color.White))
                    )
                }
            }

            emitter.updateView(result.remoteViews)
        }

        emitter.setCancellable {
            Log.d(KarooNotepadExtension.TAG, "Stopping headwind view with $emitter")
            configJob.cancel()
            viewJob.cancel()
        }
    }
}
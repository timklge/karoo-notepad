package de.timklge.karoonotepad.screens

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Note(val id: Int, var name: String,
               var text: String)

val defaultNotes = Json.encodeToString(listOf(Note(0, "Default", "This is a note.\n\nReplace it with your own text!")))

package io.github.takenoko4096.bcl.brigadier.value.text

import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject

data class RawText(
    val rawtext: List<AbstractTextComponent>,
    val rawObject: CommandJsonObject
) : TranslateComponent.With()

package io.github.takenoko4096.bcl.brigadier.value.text

import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject

data class TextComponent(
    val text: WithSpan<String>,
    val rawValue: CommandJsonObject
) : AbstractTextComponent()

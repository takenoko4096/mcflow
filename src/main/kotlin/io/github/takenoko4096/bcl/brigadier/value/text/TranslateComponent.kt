package io.github.takenoko4096.bcl.brigadier.value.text

import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject

data class TranslateComponent(
    val translate: WithSpan<String>?,
    val with: With?,
    val rawValue: CommandJsonObject
) : AbstractTextComponent() {
    sealed class With

    data class Strings(
        val strings: List<WithSpan<String>>,
        val rawValue: CommandJsonArray
    ) : With()
}

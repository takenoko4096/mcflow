package io.github.takenoko4096.bcl.brigadier.value.text

import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject

sealed class AbstractTextComponent {
    data class Empty(val rawValue: CommandJsonObject) : AbstractTextComponent()
}

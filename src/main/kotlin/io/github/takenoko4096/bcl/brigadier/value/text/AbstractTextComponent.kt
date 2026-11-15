package io.github.takenoko4096.bcl.brigadier.value.text

import io.github.takenoko4096.bcl.brigadier.value.json.value.CommandJsonObject

sealed class AbstractTextComponent {
    data class Empty(val rawValue: CommandJsonObject) : AbstractTextComponent()
}

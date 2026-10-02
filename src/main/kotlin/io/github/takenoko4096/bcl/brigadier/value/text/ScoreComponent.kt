package io.github.takenoko4096.bcl.brigadier.value.text

import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject

data class ScoreComponent(
    val name: WithSpan<EntitySelector>,
    val objective: WithSpan<String>,
    val rawValue: CommandJsonObject
) : AbstractTextComponent()

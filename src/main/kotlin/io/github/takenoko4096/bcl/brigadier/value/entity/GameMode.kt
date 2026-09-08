package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType

enum class GameMode(private val strings: Set<String>, private val id: Int? = null) {
    SURVIVAL(setOf("survival", "s"), 0),
    CREATIVE(setOf("creative", "c"), 1),
    ADVENTURE(setOf("adventure", "a"), 2),
    SPECTATOR(setOf("spectator"), null);

    companion object {
        fun parse(token: Token): GameMode? {
            return when (token.type) {
                TokenType.INT -> entries.find { it.id == token.value.toInt() }
                TokenType.WORD -> entries.find { token.value in it.strings }
                else -> null
            }
        }
    }
}

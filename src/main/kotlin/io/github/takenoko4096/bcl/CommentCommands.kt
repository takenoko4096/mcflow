package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.Command

object CommentCommands {
    val UNVANILLA = Command.builder("unvanilla") {
        inline("behavior", "resource") {
            executes()
        }

        executes()
    }

    fun getAllCommentCommandBuilders() = listOf(
        UNVANILLA
    )
}

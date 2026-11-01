package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.Command
import io.github.takenoko4096.bcl.brigadier.CommandPermissionLevel
import io.github.takenoko4096.bcl.brigadier.argument.EnumArgumentType

object CommentCommands {
    val UNVANILLA = Command.builder("unvanilla") {
        inline("behavior", "resource") {
            executes()
        }

        executes()
    }

    val PERMISSION_LEVEL = Command.builder("permission-level") {
        "level"(EnumArgumentType.of<CommandPermissionLevel>()) {
            executes {
                val level = getArgument<CommandPermissionLevel>("level")
                sender.permissionLevel = level.value
            }
        }
    }

    fun getAllCommentCommandBuilders() = listOf(
        PERMISSION_LEVEL,
        UNVANILLA
    )
}

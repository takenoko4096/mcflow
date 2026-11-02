package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.BclSeverity
import io.github.takenoko4096.bcl.brigadier.node.Command
import io.github.takenoko4096.bcl.brigadier.value.misc.CommandPermissionLevel
import io.github.takenoko4096.bcl.brigadier.argument.type.EnumArgumentType
import io.github.takenoko4096.bcl.brigadier.value.misc.MinecraftProcessSide

object CommentCommands {
    val UNVANILLA = Command.builder("unvanilla") {
        "side"(EnumArgumentType.of<MinecraftProcessSide>()) {
            "severity"(EnumArgumentType.of<BclSeverity>()) {
                executes {
                    val severity = getValue<BclSeverity>("severity")
                    when (getValue<MinecraftProcessSide>("side")) {
                        MinecraftProcessSide.CLIENT -> {
                            sender.resourceUnvanillaReport = severity
                        }
                        MinecraftProcessSide.SERVER -> {
                            sender.behaviorUnvanillaReport = severity
                        }
                    }
                }
            }

            executes {
                when (getValue<MinecraftProcessSide>("side")) {
                    MinecraftProcessSide.CLIENT -> {
                        sender.resourceUnvanillaReport = null
                    }
                    MinecraftProcessSide.SERVER -> {
                        sender.behaviorUnvanillaReport = null
                    }
                }
            }
        }

        executes {
            sender.resourceUnvanillaReport = null
            sender.behaviorUnvanillaReport = null
        }
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

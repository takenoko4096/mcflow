package io.github.takenoko4096.bcl.brigadier

class CommandSender(
    val permissionLevel: CommandPermissionLevel = CommandPermissionLevel.GAME_DIRECTOR,
    val extension: Boolean = false,
    val beta: Boolean = false
)

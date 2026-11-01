package io.github.takenoko4096.bcl.brigadier

class CommandSender(
    var permissionLevel: CommandPermissionLevel = CommandPermissionLevel.GAME_DIRECTOR,
    var extension: Boolean = false,
    val beta: Boolean = false
)

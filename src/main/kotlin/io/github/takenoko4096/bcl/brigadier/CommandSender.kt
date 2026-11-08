package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.value.misc.CommandPermissionLevel

data class CommandSender(
    var permissionLevel: CommandPermissionLevel = CommandPermissionLevel.GAME_DIRECTOR,
    var behaviorUnvanillaReport: BclSeverity? = BclSeverity.ERROR,
    var resourceUnvanillaReport: BclSeverity? = BclSeverity.WARNING,
    var extension: Boolean = false,
    val beta: Boolean = false
)

package io.github.takenoko4096.bcl.brigadier

class CommandSender(
    var permissionLevel: CommandPermissionLevel = CommandPermissionLevel.GAME_DIRECTOR,
    var behaviorUnvanillaReport: BclDiagnostic.Severity? = BclDiagnostic.Severity.ERROR,
    var resourceUnvanillaReport: BclDiagnostic.Severity? = BclDiagnostic.Severity.WARNING,
    var extension: Boolean = false,
    val beta: Boolean = false
)

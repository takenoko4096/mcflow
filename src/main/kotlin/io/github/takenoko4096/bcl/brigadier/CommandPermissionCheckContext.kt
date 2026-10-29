package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandPermissionCheckContext(
    val sourceCode: String,
    val node: CommandNode,
    val sourceSpan: SourceSpan,
    val sender: CommandSender
) {
    sealed class Result
    object Successful : Result()
    data class Failure(val diagnostic: BclDiagnostic) : Result()

    fun successful() = Successful

    fun failure(message: String) = Failure(
        BclDiagnostic(sourceCode, message, sourceSpan)
    )

    fun requirement(flag: Boolean, message: String): Result {
        return if (flag) successful() else failure(message)
    }

    fun requirePermissionLevel(minimum: CommandPermissionLevel): Result {
        return requirement(
            sender.permissionLevel.integer >= minimum.integer,
            "権限レベルが不足しています: 権限レベル '${minimum.name}' (${minimum.integer}) が必要ですが、現在の権限レベルは '${sender.permissionLevel.name}' (${sender.permissionLevel.integer}) です"
        )
    }
}

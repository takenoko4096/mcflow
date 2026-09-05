package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class CommandLineParseResult(sourceSpan: SourceSpan) : Node(sourceSpan) {
    class Successful(
        val command: Command,
        val destination: CommandNode,
        val arguments: Map<String, Argument<*>>,
        sourceSpan: SourceSpan
    ) : CommandLineParseResult(sourceSpan) {
        override fun toString(): String {
            return "Successful</${command.name} ... $destination> $arguments"
        }
    }

    class Failure(
        val command: Command?,
        val destination: CommandNode?,
        val arguments: Map<String, Argument<*>>,
        val diagnostics: List<BclDiagnostic>,
        sourceSpan: SourceSpan
    ) : CommandLineParseResult(sourceSpan) {
        override fun toString(): String {
            return if (command == null) {
                "Failure<UNKNOWN COMMAND> $arguments; ${diagnostics.joinToString("; ")}"
            }
            else if (destination == null) {
                "Failure</${command.name}> $arguments; ${diagnostics.joinToString("; ")}"
            }
            else {
                "Failure</${command.name} ... $destination> $arguments; ${diagnostics.joinToString("; ")}"
            }
        }
    }

    abstract override fun toString(): String
}

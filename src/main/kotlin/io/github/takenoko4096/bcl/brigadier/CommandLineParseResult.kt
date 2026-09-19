package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class CommandLineParseResult(val route: List<CommandNode>, val arguments: List<Pair<ArgumentCommandNode<*>, Argument<*>>>, sourceSpan: SourceSpan) : Node(sourceSpan) {
    class Successful(
        val command: Command,
        route: List<CommandNode>,
        arguments: List<Pair<ArgumentCommandNode<*>, Argument<*>>>,
        sourceSpan: SourceSpan,
        val resultValue: Any?
    ) : CommandLineParseResult(route, arguments, sourceSpan) {
        override fun toString(): String {
            return "Successful<${route.joinToString(" ") { it.toString() }}> $arguments"
        }
    }

    class Failure(
        val command: Command?,
        route: List<CommandNode>,
        arguments: List<Pair<ArgumentCommandNode<*>, Argument<*>>>,
        val diagnostics: List<BclDiagnostic>,
        sourceSpan: SourceSpan
    ) : CommandLineParseResult(route, arguments, sourceSpan) {
        override fun toString(): String {
            return if (command == null) {
                "Failure<UNKNOWN COMMAND> $arguments; ${diagnostics.joinToString("; ")}"
            }
            else if (route.isEmpty()) {
                "Failure<${command.name}> $arguments; ${diagnostics.joinToString("; ")}"
            }
            else {
                "Failure<${route.joinToString(" ") { it.toString() }}> $arguments; ${diagnostics.joinToString("; ")}"
            }
        }
    }

    abstract override fun toString(): String
}

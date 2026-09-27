package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class CommandLineParseResult(
    val route: List<CommandNode>,
    val literals: List<Pair<LiteralCommandNode, SourceSpan>>,
    val arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
    sourceSpan: SourceSpan,
    val semanticTokens: List<BclSemanticToken>
) : Node(sourceSpan, null) {
    class Successful(
        val command: Command,
        route: List<CommandNode>,
        literals: List<Pair<LiteralCommandNode, SourceSpan>>,
        arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
        sourceSpan: SourceSpan,
        semanticTokens: List<BclSemanticToken>,
        val resultValue: Any?
    ) : CommandLineParseResult(route, literals, arguments, sourceSpan, semanticTokens) {
        override fun toString(): String {
            return "Successful<${route.joinToString(" ") { it.toString() }}> $arguments"
        }
    }

    class Failure(
        val command: Command?,
        route: List<CommandNode>,
        literals: List<Pair<LiteralCommandNode, SourceSpan>>,
        arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
        val diagnostics: List<BclDiagnostic>,
        sourceSpan: SourceSpan,
        semanticTokens: List<BclSemanticToken>
    ) : CommandLineParseResult(route, literals, arguments, sourceSpan, semanticTokens) {
        override fun toString(): String {
            return if (command == null) {
                "Failure<UNKNOWN COMMAND> $arguments;\n${diagnostics.joinToString("; ")}"
            }
            else if (route.isEmpty()) {
                "Failure<${command.name}> $arguments;\n${diagnostics.joinToString("; ")}"
            }
            else {
                "Failure<${route.joinToString(" ") { it.toString() }}> $arguments;\n${diagnostics.joinToString("; ")}"
            }
        }
    }

    abstract override fun toString(): String
}

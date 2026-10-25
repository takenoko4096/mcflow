package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.completer.CompletionCandidate
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class CommandLineParseResult(
    val route: List<CommandNode>,
    val literals: List<Pair<LiteralCommandNode, SourceSpan>>,
    val arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
    val completionCandidates: List<CompletionCandidate>,
    sourceSpan: SourceSpan,
    val lastSourcePosition: SourcePosition,
    val semanticTokens: List<BclSemanticToken>
) : Node(sourceSpan) {
    class Successful(
        val command: Command,
        route: List<CommandNode>,
        literals: List<Pair<LiteralCommandNode, SourceSpan>>,
        arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
        completionCandidates: List<CompletionCandidate>,
        sourceSpan: SourceSpan,
        lastSourcePosition: SourcePosition,
        semanticTokens: List<BclSemanticToken>,
        val resultValue: Any?
    ) : CommandLineParseResult(route, literals, arguments, completionCandidates, sourceSpan, lastSourcePosition, semanticTokens) {
        override fun toString(): String {
            return "Successful<${route.joinToString(" ") { it.toString() }}> $arguments"
        }
    }

    class Failure(
        val command: Command?,
        route: List<CommandNode>,
        literals: List<Pair<LiteralCommandNode, SourceSpan>>,
        arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
        completionCandidates: List<CompletionCandidate>,
        val diagnostics: List<BclDiagnostic>,
        sourceSpan: SourceSpan,
        lastSourcePosition: SourcePosition,
        semanticTokens: List<BclSemanticToken>
    ) : CommandLineParseResult(route, literals, arguments, completionCandidates, sourceSpan, lastSourcePosition, semanticTokens) {
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

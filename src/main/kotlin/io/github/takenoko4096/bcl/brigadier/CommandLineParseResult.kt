package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.Command
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.completer.CompletionCandidate
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class CommandLineParseResult(
    open val command: Command?,
    val sender: CommandSender,
    val dispatcher: CommandDispatcher,
    val route: List<CommandNode>,
    val literals: List<Pair<LiteralCommandNode, SourceSpan>>,
    val arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
    val completionCandidates: List<CompletionCandidate>,
    val diagnostics: List<BclDiagnostic>,
    sourceSpan: SourceSpan,
    val lastSourcePosition: SourcePosition,
    val semanticTokens: List<BclSemanticToken>
) : Node(sourceSpan) {
    class Successful(
        override val command: Command,
        sender: CommandSender,
        dispatcher: CommandDispatcher,
        route: List<CommandNode>,
        literals: List<Pair<LiteralCommandNode, SourceSpan>>,
        arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
        completionCandidates: List<CompletionCandidate>,
        diagnostics: List<BclDiagnostic>,
        sourceSpan: SourceSpan,
        lastSourcePosition: SourcePosition,
        semanticTokens: List<BclSemanticToken>,
        val resultValue: Any?
    ) : CommandLineParseResult(
        command,
        sender,
        dispatcher,
        route,
        literals,
        arguments,
        completionCandidates,
        diagnostics,
        sourceSpan,
        lastSourcePosition,
        semanticTokens
    ) {
        override fun toString(): String {
            return "Successful<${route.joinToString(" ") { it.toString() }}> $arguments"
        }
    }

    class Failure(
        command: Command?,
        sender: CommandSender,
        dispatcher: CommandDispatcher,
        route: List<CommandNode>,
        literals: List<Pair<LiteralCommandNode, SourceSpan>>,
        arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
        completionCandidates: List<CompletionCandidate>,
        diagnostics: List<BclDiagnostic>,
        sourceSpan: SourceSpan,
        lastSourcePosition: SourcePosition,
        semanticTokens: List<BclSemanticToken>
    ) : CommandLineParseResult(
        command,
        sender,
        dispatcher,
        route,
        literals,
        arguments,
        completionCandidates,
        diagnostics,
        sourceSpan,
        lastSourcePosition,
        semanticTokens
    ) {
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

    fun getSourceSpanOf(index: Int): SourceSpan? {
        return when (val target = route.getOrNull(index)) {
            is LiteralCommandNode -> literals.find { (node, _) -> node == target }?.second
            is ArgumentCommandNode<*> -> arguments.find { (node, _) -> node == target }?.second?.sourceSpan
            else -> null
        }
    }

    abstract override fun toString(): String
}

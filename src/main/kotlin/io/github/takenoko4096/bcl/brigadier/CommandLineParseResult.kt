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
import io.github.takenoko4096.bcl.reader.CommandLineReader
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
    val usedCommandLineReader: CommandLineReader,
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
        usedSubReader: CommandLineReader,
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
        usedSubReader,
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
        usedSubReader: CommandLineReader,
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
        usedSubReader,
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

    abstract override fun toString(): String

    companion object {
        fun error(sender: CommandSender, reader: CommandLineReader, dispatcher: CommandDispatcher) = Failure(
            command = null,
            sender,
            dispatcher,
            route = emptyList(),
            literals = emptyList(),
            arguments = emptyList(),
            completionCandidates = emptyList(),
            diagnostics = emptyList(),
            sourceSpan = reader.getEntireSourceSpan(),
            usedSubReader = reader,
            semanticTokens = reader.preprocessedSemanticTokens
        )
    }
}

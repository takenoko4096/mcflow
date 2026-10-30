package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.completer.CompletionCandidate
import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import kotlin.collections.plusAssign

data class NodeParseContext(
    val sourceCode: String,
    val sender: CommandSender,
    val parentState: DispatcherParseState,
    val current: CommandNode,
    val nodeCandidates: Map<String, CommandNode>
) {
    val commandLineReader: CommandLineReader = parentState.reader.clone()
    val literals = parentState.literals.toMutableList()
    val arguments = parentState.arguments.toMutableList()
    val semanticTokens = mutableListOf<BclSemanticToken>()
    val diagnostics = mutableListOf<BclDiagnostic>()
    val completionCandidates = mutableListOf<CompletionCandidate>()

    var stateAtParseError: DispatcherParseState? = null

    val nextState: DispatcherParseState
        get() = DispatcherParseState(
            reader = commandLineReader,
            literals = literals,
            arguments = arguments,
            route = parentState.route + current,
            literalCount = parentState.literalCount + if (current is LiteralCommandNode) 1 else 0
        )

    /*
     * 非致命エラーが発生した時点の state を保存する
     */
    fun saveStateAtParseError() {
        stateAtParseError = nextState.copy(
            reader = commandLineReader.clone(),
            literals = literals.toList(),
            arguments = arguments.toList()
        )
    }

    /*
     * 最終的な RoutePotential に使う state
     */
    fun resultState(): DispatcherParseState {
        return stateAtParseError ?: nextState
    }

    /*
     * 権限チェック。
     *
     * 元コードの permission() と同じ処理。
     */
    fun checkPermission(start: SourcePosition, end: SourcePosition) {
        val context = CommandPermissionCheckContext(
            sourceCode,
            current,
            start..end,
            sender
        )

        val result = current.predicate?.invoke(context)

        if (result is CommandPermissionCheckContext.Failure) {
            diagnostics += result.diagnostic
        }
    }

    /*
     * 引数不足時の completion candidate を追加する
     */
    fun addMissingArgumentCompletion() {
        if (commandLineReader.hasSeparatorBeforeCursor()) {
            completionCandidates += CompletionCandidate(
                current,
                commandLineReader.getOverNextSourceSpan().start,
                null
            )
        }
    }
}
package io.github.takenoko4096.bcl.brigadier.dispatcher

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.completer.CompletionCandidate
import io.github.takenoko4096.bcl.server.BclSemanticToken

data class RoutePotential(
    val state: DispatcherParseState,
    val diagnostics: List<BclDiagnostic>,
    val furthestCursor: Int = state.reader.cursor,
    val completionCandidates: List<CompletionCandidate>,
    val semanticTokens: List<BclSemanticToken>
)
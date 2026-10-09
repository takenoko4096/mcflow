package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.completer.CompletionCandidate

data class RoutePotential(
    val state: DispatcherParseState,
    val diagnostics: List<BclDiagnostic>,
    val furthestCursor: Int = state.reader.cursor,
    val completionCandidates: List<CompletionCandidate>
)

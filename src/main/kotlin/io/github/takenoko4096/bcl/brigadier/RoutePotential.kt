package io.github.takenoko4096.bcl.brigadier

data class RoutePotential(
    val state: DispatcherParseState,
    val diagnostics: List<BclDiagnostic>,
    val furthestCursor: Int = state.reader.cursor
)

package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken

class CommandArgumentParseContext(
    val reader: TokenSeqReader,
    val sender: CommandSender,
    val diagnostics: MutableList<BclDiagnostic>,
    val semanticTokens: MutableList<BclSemanticToken>
)

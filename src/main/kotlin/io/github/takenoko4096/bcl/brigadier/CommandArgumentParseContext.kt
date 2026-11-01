package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.server.BclSemanticToken

class CommandArgumentParseContext(
    val reader: CommandLineReader,
    val sender: CommandSender,
    val diagnostics: MutableList<BclDiagnostic>,
    val semanticTokens: MutableList<BclSemanticToken>
)

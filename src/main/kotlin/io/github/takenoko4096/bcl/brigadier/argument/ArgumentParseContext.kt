package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken

class ArgumentParseContext(
    val reader: TokenSeqReader,
    val sender: CommandSender,
    val diagnostics: MutableList<BclDiagnostic>,
    val semanticTokens: MutableList<BclSemanticToken>
)
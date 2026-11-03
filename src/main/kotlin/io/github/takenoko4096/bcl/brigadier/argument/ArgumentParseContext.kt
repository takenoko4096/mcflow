package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandDispatcher
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken

data class ArgumentParseContext(
    val reader: TokenSeqReader,
    val sender: CommandSender,
    val dispatcher: CommandDispatcher,
    val diagnostics: MutableList<BclDiagnostic>,
    val semanticTokens: MutableList<BclSemanticToken>
)

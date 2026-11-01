package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.brigadier.BclComment
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.CommandSender

class CommandDocumentFile(
    var sourceCode: String,
    var block: Block,
    var comments: List<BclComment>,
    var commentCommandParseResults: List<CommandLineParseResult>,
    val sender: CommandSender
)

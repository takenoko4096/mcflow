package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.brigadier.BclComment
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult

class CommandDocumentFile(
    var sourceCode: String,
    var block: Block,
    val comments: List<BclComment>,
    val commentCommandParseResults: List<CommandLineParseResult>,
    val commandLineExclusiveSemanticTokens: List<BclSemanticToken>
)

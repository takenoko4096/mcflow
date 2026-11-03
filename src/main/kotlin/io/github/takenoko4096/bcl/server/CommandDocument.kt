package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.BclComment
import io.github.takenoko4096.bcl.brigadier.CommandSender

class CommandDocument(
    var sourceCode: String,
    var block: Block,
    var comments: List<BclComment>,
    var sender: CommandSender
)

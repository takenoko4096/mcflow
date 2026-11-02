package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.BclComment

class CommandDocumentFile(
    var sourceCode: String,
    var block: Block,
    var comments: List<BclComment>
)

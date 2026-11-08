package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Block(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    val nodes: List<Node>
) : Node(sourceSpan, diagnostics)

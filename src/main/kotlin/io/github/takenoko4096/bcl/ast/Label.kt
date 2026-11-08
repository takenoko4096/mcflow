package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Label(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    val name: String
) : Node(sourceSpan, diagnostics)

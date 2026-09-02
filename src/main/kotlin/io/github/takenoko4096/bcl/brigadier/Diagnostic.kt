package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Diagnostic(
    val message: String,
    val sourceSpan: SourceSpan
)

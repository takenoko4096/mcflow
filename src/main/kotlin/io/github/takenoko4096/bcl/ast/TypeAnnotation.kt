package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class TypeAnnotation(
    val type: Type,
    sourceSpan: SourceSpan
) : Node(sourceSpan)

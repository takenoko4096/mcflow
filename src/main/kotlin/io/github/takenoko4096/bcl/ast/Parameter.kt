package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.ast.TypeAnnotation
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Parameter(
    sourceSpan: SourceSpan,
    name: String,
    typeAnnotation: TypeAnnotation
) : Node(sourceSpan)

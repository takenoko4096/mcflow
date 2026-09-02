package io.github.takenoko4096.bcl.ast.argument

import io.github.takenoko4096.bcl.ast.Type
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class IntArgument(
    sourceSpan: SourceSpan,
    val value: Int
) : NumberArgument(Type.INT, sourceSpan)

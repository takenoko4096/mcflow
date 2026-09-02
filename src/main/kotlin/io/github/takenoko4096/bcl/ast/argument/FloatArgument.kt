package io.github.takenoko4096.bcl.ast.argument

import io.github.takenoko4096.bcl.ast.Type
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class FloatArgument(
    sourceSpan: SourceSpan,
    val value: Float
) : NumberArgument(Type.FLOAT, sourceSpan)

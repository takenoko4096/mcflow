package io.github.takenoko4096.bcl.ast.argument

import io.github.takenoko4096.bcl.ast.Type
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class UIntArgument(
    sourceSpan: SourceSpan,
    val value: UInt
) : NumberArgument(Type.UINT, sourceSpan)

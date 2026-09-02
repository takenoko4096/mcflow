package io.github.takenoko4096.bcl.ast.argument

import io.github.takenoko4096.bcl.ast.Type
import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class NumberArgument(
    type: Type,
    sourceSpan: SourceSpan
) : Argument(type, sourceSpan), Calculatable

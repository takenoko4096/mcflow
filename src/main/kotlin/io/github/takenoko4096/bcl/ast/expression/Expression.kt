package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class Expression(val sourceSpan: SourceSpan)

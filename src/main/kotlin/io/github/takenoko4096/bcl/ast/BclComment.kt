package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class BclComment(val sourceSpan: SourceSpan) {
    class Line(sourceSpan: SourceSpan) : BclComment(sourceSpan)

    class Multiline(sourceSpan: SourceSpan) : BclComment(sourceSpan)
}

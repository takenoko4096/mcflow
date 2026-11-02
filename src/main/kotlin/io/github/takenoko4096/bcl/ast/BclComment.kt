package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

abstract class BclComment(val sourceSpan: SourceSpan) {
    class Line(sourceSpan: SourceSpan) : BclComment(sourceSpan)
    class Multiline(sourceSpan: SourceSpan) : BclComment(sourceSpan)
    class CommandLine(sourceSpan: SourceSpan, val reader: CommandLineReader) : BclComment(sourceSpan)
}

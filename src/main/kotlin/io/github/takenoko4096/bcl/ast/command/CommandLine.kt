package io.github.takenoko4096.bcl.ast.command

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.argument.Argument
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandLine(
    sourceSpan: SourceSpan,
    val name: String,
    val arguments: List<Argument>
) : Node(sourceSpan)

package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class Expression(sourceSpan: SourceSpan) : Node(sourceSpan), Calculatable

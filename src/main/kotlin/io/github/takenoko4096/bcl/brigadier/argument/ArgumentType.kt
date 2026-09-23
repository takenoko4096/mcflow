package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.reader.TokenSeqReader

abstract class ArgumentType<T>(val name: String, val hasInlayHint: Boolean = false) {
    abstract fun parse(reader: TokenSeqReader): ArgumentParseResult<T>
}

package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

abstract class ArgumentType<T>(val name: String) {
    abstract fun parse(reader: TokenSeqReader): Argument<T>
}

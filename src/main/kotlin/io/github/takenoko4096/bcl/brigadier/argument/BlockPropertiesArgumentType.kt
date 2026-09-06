package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.BlockProperties
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class BlockPropertiesArgumentType : ArgumentType<BlockProperties>("block_properties") {
    override fun parse(reader: TokenSeqReader): Argument<BlockProperties> {
        /*reader.getCurrentTokenStartPosition()
        reader.readNamespacedIdentifier()
        reader.getPreviousTokenEndPosition()*/
    }
}

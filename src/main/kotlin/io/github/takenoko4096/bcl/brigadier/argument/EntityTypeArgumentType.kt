package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityType
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class EntityTypeArgumentType : ArgumentType<EntityType>("entity_type") {
    override fun parse(reader: TokenSeqReader): Argument<EntityType> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readNamespacedIdentifier()
        val end = reader.getPreviousTokenEndPosition()

        for (entry in EntityType.entries) {
            if (identifier == entry.identifier) {
                return Argument(this, entry, start..end)
            }
        }

        throw reader.exception(start..end, "不明なエンティティタイプです: $identifier")
    }
}

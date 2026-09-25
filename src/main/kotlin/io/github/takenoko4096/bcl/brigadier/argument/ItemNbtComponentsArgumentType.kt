package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.brigadier.value.item.ItemNbtComponent
import io.github.takenoko4096.bcl.brigadier.value.item.ItemNbtComponents
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.misc.ItemLockMode
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class ItemNbtComponentsArgumentType : JsonObjectArgumentType<ItemNbtComponents>("item_nbt_components") {
    override fun interpret(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, obj: CommandJsonObject): ItemNbtComponents {
        val keySpans = mutableMapOf<NamespacedIdentifier, SourceSpan>()

        if (obj.entries.isEmpty()) {
            throw reader.exception(obj.sourceSpan, "アイテムNBTコンポーネントの読み取りに失敗しました: 空のNBTコンポーネントは無効です")
        }

        // TODO
        val components = obj.entries
            .reversed()
            .distinctBy { it.first.value }
            .associate { (key, value) ->
                val identifier = NamespacedIdentifier.parse(key.value)
                    ?: throw reader.exception(key.sourceSpan, "アイテムNBTコンポーネントの読み取りに失敗しました: キーが識別子として不正です")
                keySpans[identifier] = key.sourceSpan
                identifier to value
            }
            .mapValues { (key, value) ->
                if (value !is CommandJsonObject) {
                    throw reader.exception(value.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: コンポーネントには常にオブジェクトを渡す必要があります")
                }

                when (key) {
                    ItemNbtComponents.CAN_PLACE_ON -> {
                        val blockTypes = getBlockTypes(reader, key, value)
                        ItemNbtComponent.CanPlaceOn(blockTypes, keySpans[key]!!, value)
                    }
                    ItemNbtComponents.CAN_DESTROY -> {
                        val blockTypes = getBlockTypes(reader, key, value)
                        ItemNbtComponent.CanPlaceOn(blockTypes, keySpans[key]!!, value)
                    }
                    ItemNbtComponents.ITEM_LOCK -> {
                        val itemLockMode = getMode(reader, key, value)
                        ItemNbtComponent.ItemLock(itemLockMode, keySpans[key]!!, value)
                    }
                    ItemNbtComponents.KEEP_ON_DEATH -> {
                        if (value.entries.isNotEmpty()) {
                            throw reader.exception(value.sourceSpan, "アイテムNBTコンポーネント '$key' の読み取りに失敗しました: オブジェクトは空である必要があります")
                        }

                        ItemNbtComponent.KeepOnDeath(keySpans[key]!!, value)
                    }
                    else -> {
                        throw reader.exception(keySpans[key]!!, "アイテムNBTコンポーネントの読み取りに失敗しました: 不明なコンポーネント名です: $key")
                    }
                }
            }

        return ItemNbtComponents(components)
    }

    private fun getBlockTypes(reader: TokenSeqReader, name: NamespacedIdentifier, value: CommandJsonObject): List<BlockType> {
        for ((key, _) in value.entries) {
            if (key.value != "blocks") {
                throw reader.exception(key.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: 不明なキーです: '${key.value}'")
            }
        }

        val (_, blocks) = value.getFirst("blocks")
            ?: throw reader.exception(value.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: キー 'blocks' が見つかりません")

        if (blocks !is CommandJsonArray) {
            throw reader.exception(blocks.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: 'blocks' の値は配列である必要があります")
        }

        return blocks.value.map {
            if (it.value !is String) {
                throw reader.exception(blocks.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: 'blocks' の要素は文字列である必要があります")
            }

            val identifier = NamespacedIdentifier.parse(it.value)
                ?: throw reader.exception(it.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: '${it.value}' は識別子として不正です")

            BlockType.entries.find { blockType -> blockType.identifier == identifier }
                ?: throw reader.exception(it.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: ブロックタイプ '$identifier' が見つかりません")
        }
    }

    private fun getMode(reader: TokenSeqReader, name: NamespacedIdentifier, value: CommandJsonObject): ItemLockMode {
        for ((key, _) in value.entries) {
            if (key.value != "mode") {
                throw reader.exception(key.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: 不明なキーです: '${key.value}'")
            }
        }

        val (_, mode) = value.getFirst("mode")
            ?: throw reader.exception(value.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: キー 'mode' が見つかりません")

        if (mode.value !is String) {
            throw reader.exception(mode.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: 'mode' は文字列である必要があります")
        }

        return ItemLockMode.entries.find { it.getString() == mode.value }
            ?: throw reader.exception(mode.sourceSpan, "アイテムNBTコンポーネント '$name' の読み取りに失敗しました: モード '${mode.value}' は存在しません")
    }
}

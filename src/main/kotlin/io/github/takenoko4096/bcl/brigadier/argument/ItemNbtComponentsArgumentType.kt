package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.brigadier.value.item.ItemNbtComponent
import io.github.takenoko4096.bcl.brigadier.value.item.ItemNbtComponents
import io.github.takenoko4096.bcl.brigadier.value.item.ItemType
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.schema.CommandJsonSchema
import io.github.takenoko4096.bcl.brigadier.value.misc.ItemLockMode
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItemKind

class ItemNbtComponentsArgumentType : JsonObjectArgumentType<ItemNbtComponents>("item_nbt_components") {
    val components = CommandJsonSchema.Builder.build {
        property("minecraft:can_place_on") {
            aliases.add("can_place_on")
            optional = true
            duplicatable = false

            appendObject {
                property("blocks") {
                    duplicatable = true
                    appendArray(createString { completer ->
                        when (val result = completer.completeQuotedString(ItemType.IDENTIFIERS, CompletionItemKind.EnumMember)) {
                            is ArgumentCompleter.ShouldContinue -> CommandJsonSchema.JsonCompletionResult.Continue
                            is ArgumentCompleter.ShouldComplete -> CommandJsonSchema.JsonCompletionResult.Complete(result.items)
                        }
                    })
                }
            }
        }

        property("minecraft:can_destroy") {
            aliases.add("can_destroy")
            optional = true
            duplicatable = false

            appendObject {
                property("blocks") {
                    duplicatable = true
                    appendArray(createString { completer ->
                        when (val result = completer.completeQuotedString(ItemType.IDENTIFIERS, CompletionItemKind.EnumMember)) {
                            is ArgumentCompleter.ShouldContinue -> CommandJsonSchema.JsonCompletionResult.Continue
                            is ArgumentCompleter.ShouldComplete -> CommandJsonSchema.JsonCompletionResult.Complete(result.items)
                        }
                    })
                }
            }
        }

        property("minecraft:item_lock") {
            aliases.add("item_lock")
            optional = true
            duplicatable = true

            appendObject {
                property("mode") {
                    duplicatable = true
                    append(createString { completer ->
                        when (val result = completer.completeQuotedString(ItemLockMode.entries.map { it.getString() }, CompletionItemKind.EnumMember)) {
                            is ArgumentCompleter.ShouldContinue -> CommandJsonSchema.JsonCompletionResult.Continue
                            is ArgumentCompleter.ShouldComplete -> CommandJsonSchema.JsonCompletionResult.Complete(result.items)
                        }
                    })
                }
            }
        }

        property("minecraft:keep_on_death") {
            aliases.add("keep_on_death")
            optional = true
            duplicatable = true
            appendObject {}
        }
    }

    override fun getSchema() = components

    override fun interpret(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, obj: CommandJsonObject): ItemNbtComponents {
        val result = components.validate(obj, reader.sourceCode, diagnostics)

        if (!result) {
            return ItemNbtComponents(emptyMap())
        }

        val map = mutableMapOf<NamespacedIdentifier, ItemNbtComponent>()

        val canPlaceOnObjects = obj.getWrappers("minecraft:can_place_on", CommandJsonObject::class) + obj.getWrappers("can_place_on", CommandJsonObject::class)
        getBlockTypes(reader, diagnostics, semanticTokens, canPlaceOnObjects)?.run {
            map[ItemNbtComponents.CAN_PLACE_ON] = ItemNbtComponent.CanPlaceOn(second, first)
        }

        val canDestroyObjects = obj.getWrappers("minecraft:can_destroy", CommandJsonObject::class) + obj.getWrappers("can_destroy", CommandJsonObject::class)
        getBlockTypes(reader, diagnostics, semanticTokens, canDestroyObjects)?.run {
            map[ItemNbtComponents.CAN_DESTROY] = ItemNbtComponent.CanDestroy(second, first)
        }

        val itemLockObjects = obj.getWrappers("minecraft:item_lock", CommandJsonObject::class) + obj.getWrappers("item_lock", CommandJsonObject::class)
        getItemLockMode(reader, diagnostics, semanticTokens, itemLockObjects)?.run {
            map[ItemNbtComponents.ITEM_LOCK] = ItemNbtComponent.ItemLock(second, first)
        }

        val keepOnDeathObjects = obj.getWrappers("minecraft:keep_on_death", CommandJsonObject::class) + obj.getWrappers("keep_on_death", CommandJsonObject::class)
        keepOnDeathObjects.firstOrNull()?.run {
            map[ItemNbtComponents.KEEP_ON_DEATH] = ItemNbtComponent.KeepOnDeath(this)
        }

        return ItemNbtComponents(map)
    }

    private fun getBlockTypes(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, objects: List<CommandJsonObject>): Pair<CommandJsonObject, List<BlockType>>? {
        return objects
            .mapNotNull {
                val blockTypes = it.getWrappers("blocks", CommandJsonArray::class)
                    .map { blocks ->
                        blocks.list.mapNotNull { element ->
                            val string = element.value as String

                            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, element.sourceSpan))

                            val id = NamespacedIdentifier.parse(string) ?: run {
                                diagnostics.add(reader.diagnostic(element.sourceSpan, "アイテムNBTコンポーネントの読み取りに失敗しました: 識別子として無効な値です: '$string'"))
                                return@mapNotNull null
                            }

                            BlockType.get(id) ?: run {
                                diagnostics.add(reader.diagnostic(element.sourceSpan, "アイテムNBTコンポーネントの読み取りに失敗しました: 不明なブロックタイプです: '$string'"))
                                return@mapNotNull null
                            }
                        }
                    }
                    .firstOrNull()

                if (blockTypes == null) {
                    null
                }
                else {
                    it to blockTypes
                }
            }
            .firstOrNull()
    }

    private fun getItemLockMode(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, objects: List<CommandJsonObject>): Pair<CommandJsonObject, ItemLockMode>? {
        return objects
            .firstNotNullOfOrNull {
                val mode = it.getStrings("mode").firstNotNullOfOrNull { string ->
                    val mode = ItemLockMode.entries.find { entry -> entry.getString() == string.value }

                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ENUM, string.sourceSpan))

                    if (mode == null) {
                        diagnostics.add(reader.diagnostic(string.sourceSpan, "アイテムNBTコンポーネントの読み取りに失敗しました: 無効なアイテムロックモードです: '${string.value}'"))
                        return@firstNotNullOfOrNull null
                    }

                    return@firstNotNullOfOrNull mode
                }

                if (mode == null) null else it to mode
            }
    }
}

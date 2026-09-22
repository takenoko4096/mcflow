package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.brigadier.value.text.AbstractTextComponent
import io.github.takenoko4096.bcl.brigadier.value.text.RawText
import io.github.takenoko4096.bcl.brigadier.value.text.TextComponent
import io.github.takenoko4096.bcl.brigadier.value.text.TranslateComponent
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class RawTextComponentArgumentType : JsonObjectArgumentType<RawText>("rawtext") {
    override fun interpret(reader: TokenSeqReader, obj: CommandJsonObject): RawText {
        if ("rawtext" !in obj) {
            throw reader.exception(obj.sourceSpan, "rawtext の解釈に失敗しました: ルートオブジェクトにはキー 'rawtext' が必要です")
        }

        // TODO
        val (_, rawtextValue) = obj.getLast("rawtext")!!

        if (rawtextValue !is CommandJsonArray) {
            throw reader.exception(rawtextValue.sourceSpan, "rawtext の解釈に失敗しました: キー 'rawtext' には配列が期待されています")
        }

        val components = mutableListOf<AbstractTextComponent>()

        for (element in rawtextValue.list) {
            if (element !is CommandJsonObject) {
                throw reader.exception(element.sourceSpan, "rawtext の解釈に失敗しました: テキストコンポーネントはオブジェクトである必要があります")
            }

            components.add(toComponent(reader, element))
        }

        return RawText(components, obj, rawtextValue)
    }

    private fun toComponent(reader: TokenSeqReader, obj: CommandJsonObject): AbstractTextComponent {
        // To be valid, an object must contain one content tag: [String] text, [String] translate, [NBT Compound / JSON Object] score, or [String] selector. Having more than one is allowed, but only one is used.[note 4]

        // TODO
        if ("translate" in obj) {
            val (_, translate) = obj.getFirst("translate")!!

            if (translate.value !is String) {
                throw reader.exception(translate.sourceSpan, "rawtext の解釈に失敗しました: translate は文字列である必要があります")
            }

            return if ("with" in obj) {
                val (_, with) = obj.getLast("with")!!

                when (with) {
                    is CommandJsonArray -> {
                        val strings = with.list.map {
                            if (it.value !is String) {
                                throw reader.exception(
                                    it.sourceSpan,
                                    "rawtext の解釈に失敗しました: 配列を保持する with の要素は文字列である必要があります"
                                )
                            }
                            WithSpan(it.value, it.sourceSpan)
                        }
                        TranslateComponent(
                            WithSpan(translate.value, translate.sourceSpan),
                            TranslateComponent.Strings(strings, with),
                            obj
                        )
                    }
                    is CommandJsonObject -> {
                        TranslateComponent(
                            WithSpan(translate.value, translate.sourceSpan),
                            interpret(reader, with),
                            obj
                        )
                    }
                    else -> {
                        throw reader.exception(with.sourceSpan, "rawtext の解釈に失敗しました: with に無効な値が渡されています")
                    }
                }
            }
            else {
                TranslateComponent(
                    WithSpan(translate.value, translate.sourceSpan),
                    null,
                    obj
                )
            }
        }

        if ("text" in obj) {
            val (_, text) = obj.getFirst("text")!!

            if (text.value !is String) {
                throw reader.exception(text.sourceSpan, "rawtext の解釈に失敗しました: text は文字列である必要があります")
            }

            return TextComponent(
                WithSpan(text.value, text.sourceSpan),
                obj
            )
        }

        if ("selector" in obj) {
            TODO()
        }

        if ("score" in obj) {
            TODO()
        }

        return AbstractTextComponent.Empty(obj)
    }
}

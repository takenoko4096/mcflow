package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.brigadier.value.json.schema.CommandJsonSchema
import io.github.takenoko4096.bcl.brigadier.value.text.AbstractTextComponent
import io.github.takenoko4096.bcl.brigadier.value.text.RawText
import io.github.takenoko4096.bcl.brigadier.value.text.ScoreComponent
import io.github.takenoko4096.bcl.brigadier.value.text.SelectorComponent
import io.github.takenoko4096.bcl.brigadier.value.text.TextComponent
import io.github.takenoko4096.bcl.brigadier.value.text.TranslateComponent
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class RawTextComponentArgumentType : JsonObjectArgumentType<RawText>("rawtext") {
    val rawtext = CommandJsonSchema.Builder.build {
        property("text") {
            optional = true
            duplicatable = true
            string()
        }

        property("translate") {
            optional = true
            duplicatable = true
            string()
        }

        property("with") {
            optional = true
            duplicatable = true
            array(string)
        }

        property("selector") {
            optional = true
            duplicatable = true
            string()
        }

        property("score") {
            optional = true
            duplicatable = true
            `object` {
                property("name") {
                    optional = true
                    duplicatable = true
                    string()
                }

                property("objective") {
                    optional = true
                    duplicatable = true
                    string()
                }
            }
        }
    }.apply { (keys["with"]!!.schemas as MutableSet<CommandJsonSchema>).add(this) }

    val root = CommandJsonSchema.Builder.build {
        property("rawtext") {
            optional = false
            duplicatable = true

            array(rawtext)
        }
    }

    override fun interpret(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, obj: CommandJsonObject): RawText {
        root.validate(obj, reader.sourceCode, diagnostics)

        val rawtexts = obj.getWrappers("rawtext", CommandJsonArray::class)
            .map { array ->
                array to array.list.map { element ->
                    element as CommandJsonObject
                    toComponent(reader, diagnostics, semanticTokens, element)
                }
            }

        val (array, components) = rawtexts.first()

        return RawText(components, obj, array)
    }

    private fun toComponent(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, obj: CommandJsonObject): AbstractTextComponent {
        // To be valid, an object must contain one content tag: [String] text, [String] translate, [NBT Compound / JSON Object] score, or [String] selector. Having more than one is allowed, but only one is used.[note 4]
        var component: AbstractTextComponent = AbstractTextComponent.Empty(obj)

        if ("score" in obj) {
            val scores = obj.getWrappers("score", CommandJsonObject::class)

            val scoreComponents = scores.mapNotNull {
                val names = it.getStrings("name")
                val objectives = it.getStrings("objective")

                val selectors = names.mapNotNull { name ->
                    val result = EntitySelector.parse(name.value, diagnostics, semanticTokens)
                    if (result is Argument) {
                        name to result.value
                    }
                    else null
                }

                val objective = objectives.firstOrNull()
                val pair = selectors.firstOrNull()

                if (pair == null || objective == null) null
                else {
                    ScoreComponent(
                        WithSpan(pair.second, pair.first.sourceSpan),
                        WithSpan(objective.value, objective.sourceSpan),
                        it
                    )
                }
            }

            scoreComponents.firstOrNull()?.run {
                component = this
            }
        }

        if ("selector" in obj) {
            val selectors = obj.getStrings("selector")

            val selectorComponents = selectors.mapNotNull { string ->
                when (val selectorParseResult = EntitySelector.parse(string.value, diagnostics, semanticTokens)) {
                    is Argument<EntitySelector> -> {
                        SelectorComponent(
                            WithSpan(selectorParseResult.value, string.sourceSpan),
                            obj
                        )
                    }
                    is NoValuedArgument<EntitySelector> -> {
                        diagnostics.add(
                            reader.diagnostic(
                                string.sourceSpan,
                                "rawtext の解釈に失敗しました: selector の解析に失敗しました: ${selectorParseResult.diagnostics.joinToString { it.message }}"
                            )
                        )
                        null
                    }
                }
            }

            selectorComponents.firstOrNull()?.run {
                component = this
            }
        }

        if ("text" in obj) {
            val texts = obj.getStrings("text")
            val text = texts.first()

            return TextComponent(
                WithSpan(text.value, text.sourceSpan),
                obj
            )
        }

        if ("translate" in obj) {
            val translate = obj.getStrings("translate").first()

            component = if ("with" in obj) {
                val withs = obj.getAny("with")

                withs.filterIsInstance<CommandJsonArray>().forEach {
                    for (element in it.list) {
                        if (element.value !is String) {
                            diagnostics.add(reader.diagnostic(element.sourceSpan, "rawtext の解釈に失敗しました: 配列を保持する with の要素は文字列である必要があります"))
                        }
                    }
                }

                withs.filterIsInstance<CommandJsonObject>().forEach {
                    interpret(reader, diagnostics, semanticTokens, it)
                }

                when (val with = withs.first()) {
                    is CommandJsonArray -> {
                        val strings = with.list.map { WithSpan(it.value as String, it.sourceSpan) }
                        TranslateComponent(
                            WithSpan(translate.value, translate.sourceSpan),
                            TranslateComponent.Strings(strings, with),
                            obj
                        )
                    }
                    is CommandJsonObject -> {
                        TranslateComponent(
                            WithSpan(translate.value, translate.sourceSpan),
                            interpret(reader, diagnostics, semanticTokens, with),
                            obj
                        )
                    }
                    else -> throw IllegalStateException("NEVER HAPPENS")
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

        return component
    }
}

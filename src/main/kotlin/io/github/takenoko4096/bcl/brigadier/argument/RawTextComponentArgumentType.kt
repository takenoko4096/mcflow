package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.schema.CommandJsonSchema
import io.github.takenoko4096.bcl.brigadier.value.json.schema.ObjectSchema
import io.github.takenoko4096.bcl.brigadier.value.text.AbstractTextComponent
import io.github.takenoko4096.bcl.brigadier.value.text.RawText
import io.github.takenoko4096.bcl.brigadier.value.text.ScoreComponent
import io.github.takenoko4096.bcl.brigadier.value.text.SelectorComponent
import io.github.takenoko4096.bcl.brigadier.value.text.TextComponent
import io.github.takenoko4096.bcl.brigadier.value.text.TranslateComponent
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class RawTextComponentArgumentType : JsonObjectArgumentType<RawText>("rawtext") {
    val rawtext: ObjectSchema
    val root: ObjectSchema

    init {
        rawtext = CommandJsonSchema.Builder.build {
            property("text") {
                optional = true
                duplicatable = true
                append(createString())
            }

            property("translate") {
                optional = true
                duplicatable = true
                append(createString())
            }

            property("with") {
                optional = true
                duplicatable = true
                appendArray(createString())
            }

            property("selector") {
                optional = true
                duplicatable = true
                append(createString()) // TODO
            }

            property("score") {
                optional = true
                duplicatable = true
                appendObject {
                    property("name") {
                        optional = false
                        duplicatable = true
                        append(createString()) // TODO
                    }

                    property("objective") {
                        optional = false
                        duplicatable = true
                        append(createString()) // TODO
                    }
                }
            }
        }

        root = CommandJsonSchema.Builder.build {
            property("rawtext") {
                optional = false
                duplicatable = true

                appendArray(rawtext)
            }
        }

        (rawtext.keys["with"]!!.schemas as MutableSet<CommandJsonSchema>).add(root)
    }

    override fun getSchema() = root

    override fun interpret(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, obj: CommandJsonObject): RawText {
        if (!root.validate(obj, reader.sourceCode, diagnostics)) {
            return RawText(emptyList(), obj)
        }

        val rawtexts = obj.getWrappers("rawtext", CommandJsonArray::class)
            .map { array ->
                array.list.map { element ->
                    element as CommandJsonObject
                    toComponent(reader, diagnostics, semanticTokens, element)
                }
            }

        val components = rawtexts.first()

        return RawText(components, obj)
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
                    val offset = name.sourceSpan.trimEdge()?.start
                    val result = EntitySelector.parse(name.value, diagnostics, semanticTokens, offset, name.sourceSpan)
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
                val offset = string.sourceSpan.trimEdge()?.start
                val v = when (val selectorParseResult = EntitySelector.parse(string.value, diagnostics, semanticTokens, offset, string.sourceSpan)) {
                    is Argument<EntitySelector> -> {
                        SelectorComponent(
                            WithSpan(selectorParseResult.value, string.sourceSpan),
                            obj
                        )
                    }
                    is NoValuedArgument<EntitySelector> -> {
                        null
                    }
                }

                v
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

        val translate = if ("translate" in obj) {
            obj.getStrings("translate").first()
        }
        else null

        val with = if ("with" in obj) {
            val withs = obj.getAny("with")

            withs.filterIsInstance<CommandJsonArray>().forEach {
                for (element in it.list) {
                    if (element.value !is String) {
                        diagnostics.add(reader.diagnostic(element.sourceSpan, "rawtext の解釈に失敗しました: 配列を保持する with の要素は文字列である必要があります"))
                    }
                }
            }

            val withRawTexts = withs.filterIsInstance<CommandJsonObject>().map {
                interpret(reader, diagnostics, semanticTokens, it)
            }

            when (val with = withs.first()) {
                is CommandJsonArray -> {
                    val strings = with.list.map { WithSpan(it.value as String, it.sourceSpan) }
                    TranslateComponent.Strings(strings, with)
                }
                is CommandJsonObject -> {
                    withRawTexts.first()
                }
                else -> null
            }
        }
        else null

        if (translate != null || with != null) {
            return TranslateComponent(
                if (translate == null) null else WithSpan(translate.value, translate.sourceSpan),
                with,
                obj
            )
        }

        return component
    }
}

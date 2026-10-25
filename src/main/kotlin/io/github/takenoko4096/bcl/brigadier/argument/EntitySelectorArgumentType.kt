package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.item.ItemType
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.entity.AccessibleActorProperties
import io.github.takenoko4096.bcl.brigadier.value.entity.ActorProperty
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityContainerSlotType
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityType
import io.github.takenoko4096.bcl.brigadier.value.entity.GameMode
import io.github.takenoko4096.bcl.brigadier.value.entity.InvertibleValue
import io.github.takenoko4096.bcl.brigadier.value.entity.SelectorArguments
import io.github.takenoko4096.bcl.brigadier.value.entity.SelectorType
import io.github.takenoko4096.bcl.brigadier.value.misc.InvertibleIntegerRange
import io.github.takenoko4096.bcl.completer.BclCompleter
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat

class EntitySelectorArgumentType(private val validator: (TokenSeqReader.(EntitySelector, SourceSpan, MutableList<BclDiagnostic>) -> Unit)? = null) : ArgumentType<EntitySelector>("entity_selector") {
    private val parsers: Map<String, TokenSeqReader.(SelectorArguments, MutableList<BclDiagnostic>, MutableList<BclSemanticToken>, SourceSpan) -> Unit> = mapOf(
        "c" to ::c,
        "dx" to ::dx,
        "dy" to ::dy,
        "dz" to ::dz,
        "family" to ::family,
        "haspermission" to ::hasPermission,
        "has_property" to ::hasProperty,
        "hasitem" to ::hasItem,
        "l" to ::l,
        "lm" to ::lm,
        "m" to ::m,
        "name" to ::name,
        "r" to ::r,
        "rm" to ::rm,
        "rx" to ::rx,
        "rxm" to ::rxm,
        "ry" to ::ry,
        "rym" to ::rym,
        "scores" to ::scores,
        "tag" to ::tag,
        "type" to ::type,
        "x" to ::x,
        "y" to ::y,
        "z" to ::z
    )

    // returning null means ShouldContinue
    private val completers: Map<String, (BclCompleter) -> BclCompleter.ValueCompletionResult> = mapOf(
        "c" to ::completeOne,
        "dx" to ::completeZero,
        "dy" to ::completeZero,
        "dz" to ::completeZero,
        "family" to ::completeFamily,
        "haspermission" to ::completeHasPermission,
        "has_property" to ::completeHasProperty,
        "hasitem" to ::completeHasItem,
        "m" to ::completeGameMode,
        "rm" to ::completeZero,
        "scores" to ::completeScores,
        "name" to ::completeName,
        "tag" to ::completeTag,
        "type" to ::completeType
    )

    private fun TokenSeqReader.readIsInverted(): Boolean {
        return if (peek() `is` Symbol.LOGICAL_NOT) {
            next()
            true
        }
        else false
    }

    private fun c(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val c = reader.readInteger()
        val end = reader.getPreviousTokenEndPosition()

        var failure = false

        if (arguments.c != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'c' の読み取りに失敗しました: セレクタ引数 c は重複できません"))
            failure = true
        }

        if (c == 0) {
            diagnostics.add(reader.diagnostic(
                start..end,
                "セレクタ引数 'c' の読み取りに失敗しました: セレクタ引数 c は 0以外の整数が有効です"
            ))
            failure = true
        }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (!failure) {
            arguments.c = WithSpan(c, start..end)
        }
    }

    private fun dx(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val dx = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.dx != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'dx' の読み取りに失敗しました: セレクタ引数 dx は重複できません"))
            return
        }

        arguments.dx = WithSpan(dx, start..end)
    }

    private fun dy(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val dy = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.dy != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'dy' の読み取りに失敗しました: セレクタ引数 dy は重複できません"))
            return
        }

        arguments.dy = WithSpan(dy, start..end)
    }

    private fun dz(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val dz = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.dz != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'dz' の読み取りに失敗しました: セレクタ引数 dz は重複できません"))
            return
        }

        arguments.dz = WithSpan(dz, start..end)
    }

    private fun family(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val inverted = reader.readIsInverted()
        val start = reader.getCurrentTokenStartPosition()
        val string = reader.readQuotableCommandString()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, start..end))

        arguments.family.add(InvertibleValue(inverted, string, start..end))
    }

    private fun hasPermission(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        reader.expect(Symbol.BRACE_START)

        if (reader.peek() `is` Symbol.BRACE_END) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'haspermission' の読み取りに失敗しました: 空の haspermission は無効です"))
            return
        }

        val hasPermission = SelectorArguments.HasPermission()

        while (true) {
            val start1 = reader.getCurrentTokenStartPosition()
            val name = reader.readCommandString()
            val end1 = reader.getPreviousTokenEndPosition()

            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ENUM, start1..end1))

            val permission = SelectorArguments.HasPermission.Permission.parse(name)
                ?: run {
                    diagnostics.add(reader.diagnostic(
                        start1..end1,
                        "セレクタ引数 'haspermission' の読み取りに失敗しました: 不明なパーミッション名です: $name"
                    ))
                    null
                }

            reader.expect(Symbol.ASSIGN)

            val start2 = reader.getCurrentTokenStartPosition()
            val flag = reader.readCommandString()
            val end2 = reader.getPreviousTokenEndPosition()

            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, start2..end2))

            val state = SelectorArguments.HasPermission.PermissionState.parse(flag)
                ?: run {
                    diagnostics.add(reader.diagnostic(
                        start2..end2,
                        "セレクタ引数 'haspermission' の読み取りに失敗しました: 不明なパーミッションステートです: '$flag'; ${
                            SelectorArguments.HasPermission.PermissionState.entries.joinToString(" | ") { it.getString() }
                        } が期待されています"
                    ))
                    null
                }
            if (permission != null && state != null) hasPermission.set(permission, state)

            val start3 = reader.getCurrentTokenStartPosition()
            if (reader.isOver()) {
                throw reader.exception(reader.getOverNextSourceSpan(), "セレクタ引数 'haspermission' の読み取りに失敗しました: 引数が '${Symbol.BRACE_END.value}' で閉じられていません")
            }
            else if (reader.peek() `is` Symbol.COMMA) {
                reader.next()
            }
            else if (reader.peek() `is` Symbol.BRACE_END) {
                reader.next()
                break
            }
            else {
                reader.next()
                val end3 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start3..end3, "セレクタ引数 'haspermission' の読み取りに失敗しました: 不明なトークンです: ${reader.peek()}")
            }
        }

        if (arguments.hasPermission != null) {
            val end = reader.getPreviousTokenEndPosition()
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'haspermission' の読み取りに失敗しました: haspermission は重複できません"))
            return
        }

        arguments.hasPermission = hasPermission
    }

    private fun hasProperty(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()

        val properties = mutableListOf<InvertibleValue<NamespacedIdentifier>>()

        val list: List<Pair<NamespacedIdentifier, InvertibleValue<Any>>> = reader.readDuplicatableMap(
            Symbol.BRACE_START,
            Symbol.BRACE_END,
            kvSeparatorParser = {
                expect(Symbol.ASSIGN)
                val start2 = getCurrentTokenStartPosition()
                val inv = readIsInverted()
                val end2 = getPreviousTokenEndPosition()
                WithSpan(inv, if (inv) start2..end2 else SourceSpan.NULL)
            },
            keyParser = {
                val start2 = getCurrentTokenStartPosition()
                val key = readCommandString()
                val end2 = getPreviousTokenEndPosition()

                if (key == "property") {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.PARAMETER, start2..end2))
                    return@readDuplicatableMap NamespacedIdentifier("", "property")
                }

                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start2..end2))

                val identifier = NamespacedIdentifier.parse(key) ?: run {
                    diagnostics.add(diagnostic(start2..end2, "セレクタ引数 'has_property' の読み取りに失敗しました: 文字列 '$key' は識別子として無効です (識別子は '${NamespacedIdentifier.REGEX}' の形式に従う必要があります)"))
                    return@readDuplicatableMap null
                }

                if (AccessibleActorProperties.getProperty(identifier) == null) {
                    diagnostics.add(diagnostic(start2..end2, "セレクタ引数 'has_property' の読み取りに失敗しました: 識別子 '$identifier' に一致するプロパティは存在しません"))
                    return@readDuplicatableMap null
                }

                return@readDuplicatableMap identifier
            },
            valueParser = { key, inv ->
                if (key == NamespacedIdentifier("", "property")) {
                    val start2 = getCurrentTokenStartPosition()
                    val identifier = readNamespacedIdentifier()
                    val end2 = getPreviousTokenEndPosition()
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start2..end2))
                    if (AccessibleActorProperties.getProperty(identifier) == null) {
                        diagnostics.add(reader.diagnostic(start2..end2, "セレクタ引数 'has_property' の読み取りに失敗しました: '$identifier' は不明なプロパティです"))
                    }
                    properties.add(InvertibleValue(inv.value, identifier, start2..end2))
                    return@readDuplicatableMap null
                }

                val start2 = getCurrentTokenStartPosition()
                val string = readQuotableCommandString(allowFloat = true, keepQuotation = true)
                val end2 = getPreviousTokenEndPosition()

                val value = if (string in setOf("true", "false")) {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, start2..end2))
                    string.toBoolean()
                }
                else if (string.matches(KeyChar.NUMBER_REGEX)) {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start2..end2))
                    val i = string.toIntOrNull()
                    if (i == null) {
                        diagnostics.add(diagnostic(start2..end2, "セレクタ引数 'has_property' の読み取りに失敗しました: 浮動小数点数はプロパティの値として無効です"))
                        return@readDuplicatableMap null
                    }
                    else i
                }
                else if (string.startsWith('"') && string.endsWith('"')) {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, start2..end2))
                    string.slice(1..<string.lastIndex)
                }
                else {
                    diagnostics.add(diagnostic(start2..end2, "セレクタ引数 'has_property' の読み取りに失敗しました: '$string' は無効な値です"))
                    return@readDuplicatableMap null
                }

                val invertible = InvertibleValue<Any>(inv.value, value, start2..end2)

                if (key == null) {
                    return@readDuplicatableMap invertible
                }

                val property = AccessibleActorProperties.getProperty(key)!!
                val propertyValue = property.toValue(value)

                if (propertyValue == null) {
                    diagnostics.add(diagnostic(start2..end2, "プロパティ '$property' に存在しないエントリです: '$value'"))
                }

                return@readDuplicatableMap invertible
            }
        )

        val end = reader.getPreviousTokenEndPosition()

        if (arguments.hasProperty != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'has_property' の読み取りに失敗しました: has_property は重複できません"))
            return
        }

        arguments.hasProperty = SelectorArguments.HasProperty(
            properties,
            list,
            start..end
        )
    }

    private fun itemPredicateObject(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): SelectorArguments.HasItem? {
        var item: ItemType? = null
        var location: EntityContainerSlotType? = null
        var slot: InvertibleValue<IntRange>? = null
        var quantity: InvertibleValue<IntRange>? = null
        var data: Int? = null

        val startG = reader.getCurrentTokenStartPosition()

        reader.readTypedMap(
            prefix = Symbol.BRACE_START,
            postfix = Symbol.BRACE_END,
            keyParser = {
                val start2 = getCurrentTokenStartPosition()
                val s = readCommandString()
                val end2 = getPreviousTokenEndPosition()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.PARAMETER, start2..end2))
                s
            },
            kvSeparatorParser = {
                expect(Symbol.ASSIGN)
                val start = getCurrentTokenStartPosition()
                val inv = readIsInverted()
                val end = getPreviousTokenEndPosition()
                WithSpan(inv, if (inv) start..end else SourceSpan.NULL)
            },
            fallbackValueParser = { key, _ ->
                diagnostics.add(diagnostic(key.sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました: 不明なキーです: '${key.value}'"))
            },
            valueParsers = mapOf(
                "item" to { inv ->
                    if (inv.value) {
                        diagnostics.add(diagnostic(inv.sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました: キー 'item' は反転できません"))
                    }

                    val start2 = getCurrentTokenStartPosition()
                    val id = readNamespacedIdentifier()
                    val end2 = getPreviousTokenEndPosition()

                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start2..end2))

                    item = ItemType.get(id) ?: run {
                        diagnostics.add(diagnostic(start2..end2, "セレクタ引数 'hasitem' の読み取りに失敗しました:  アイテムタイプ '$id' は存在しません"))
                        return@to
                    }
                },
                "slot" to { inv ->
                    val start = getCurrentTokenStartPosition()
                    val v = readIntRange()
                    val end = getPreviousTokenEndPosition()

                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

                    slot = InvertibleValue(inv.value, v, start..end)
                },
                "quantity" to { inv ->
                    val start = getCurrentTokenStartPosition()
                    val v = readIntRange()
                    val end = getPreviousTokenEndPosition()

                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

                    quantity = InvertibleValue(inv.value, v, start..end)
                },
                "location" to { inv ->
                    if (inv.value) {
                        diagnostics.add(diagnostic(inv.sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました:  キー 'location' は反転できません"))
                    }

                    val start2 = getCurrentTokenStartPosition()
                    val string = readCommandString()
                    val end2 = getPreviousTokenEndPosition()

                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ENUM, start2..end2))

                    location = EntityContainerSlotType.parse(string)
                        ?: run {
                            diagnostics.add(diagnostic(start2..end2, "セレクタ引数 'hasitem' の読み取りに失敗しました:  location '$string' は存在しません"))
                            return@to
                        }
                },
                "data" to { inv ->
                    if (inv.value) {
                        diagnostics.add(diagnostic(inv.sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました:  キー 'data' は反転できません"))
                    }

                    val start2 = getCurrentTokenStartPosition()
                    val v = readInteger()
                    val end2 = getPreviousTokenEndPosition()

                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start2..end2))

                    data = v
                }
            )
        )

        val endG = reader.getPreviousTokenEndPosition()

        if (item == null) {
            diagnostics.add(reader.diagnostic(startG..endG, "セレクタ引数 'hasitem' の読み取りに失敗しました: キー 'item' は必須です"))
            return null
        }

        return SelectorArguments.HasItem(item, location, slot, quantity, data, startG..endG)
    }

    private fun hasItem(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val list = mutableListOf<SelectorArguments.HasItem>()

        val start = reader.getCurrentTokenStartPosition()

        val t = reader.peek()
        if (t `is` Symbol.ARRAY_START) {
            list.addAll(reader.readList(Symbol.ARRAY_START, Symbol.ARRAY_END) {
                itemPredicateObject(this, diagnostics, semanticTokens)
            })
        }
        else if (t `is` Symbol.BRACE_START) {
            itemPredicateObject(reader, diagnostics, semanticTokens)?.run { list.add(this) }
        }
        else {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数 'hasitem' の読み取りに失敗しました: 不明なトークンです: $t")
        }

        val end = reader.getPreviousTokenEndPosition()

        if (arguments.hasItem.isNotEmpty()) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました: hasitem は重複できません"))
            return
        }

        arguments.hasItem.addAll(list)
    }

    private fun l(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val l = reader.readInteger()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.l != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'l' の読み取りに失敗しました: セレクタ引数 l は重複できません"))
            return
        }

        if (l < 0) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'l' の読み取りに失敗しました: 負のレベルを条件にすることはできません"))
            return
        }

        arguments.l = l
    }

    private fun lm(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val lm = reader.readInteger()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.lm != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'lm' の読み取りに失敗しました: セレクタ引数 lm は重複できません"))
            return
        }

        if (lm < 0) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'lm' の読み取りに失敗しました: 負のレベルを条件にすることはできません"))
            return
        }

        arguments.lm = lm
    }

    private fun m(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val inv = reader.readIsInverted()
        val start = reader.getCurrentTokenStartPosition()
        val s = reader.readCommandString(allowFloat = true)
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ENUM, start..end))

        if (arguments.m != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'm' の読み取りに失敗しました: セレクタ引数 m は \"\"否定であっても\"\" 重複できません"))
            return
        }

        val gameMode = GameMode.entries.find { it.getNames().contains(s) }
            ?: run {
                diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'm' の読み取りに失敗しました: ゲームモード '$s' は存在しません"))
                return
            }

        arguments.m = InvertibleValue(inv, gameMode, start..end)
    }

    private fun name(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val inverted = reader.readIsInverted()

        val start = reader.getCurrentTokenStartPosition()
        val name = reader.unfatalize(diagnostics) { readQuotableCommandString() }
        val end = reader.getPreviousTokenEndPosition()

        val valueSourceSpan = if (end.index < start.index) null else start..end

        valueSourceSpan?.run {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, this))
        }

        var failure = false

        if (!inverted && arguments.name.any { !it.inverted }) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'name' の読み取りに失敗しました: name は否定でない限り重複できません"))
            failure = true
        }

        if (name is TokenSeqReader.ReadResult.Failure) {
            failure = true
        }

        if (failure) {
            return
        }

        name as TokenSeqReader.ReadResult.Successful
        arguments.name.add(InvertibleValue(inverted, name.value, start..end))
    }

    private fun r(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val r = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.r != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'r' の読み取りに失敗しました: セレクタ引数 r は重複できません"))
            return
        }

        if (r < 0) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'r' の読み取りに失敗しました: 半径を負の値にすることはできません"))
            return
        }

        arguments.r = r
    }

    private fun rm(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val rm = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.rm != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'rm' の読み取りに失敗しました: セレクタ引数 rm は重複できません"))
            return
        }

        if (rm < 0) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'rm' の読み取りに失敗しました: 半径を負の値にすることはできません"))
            return
        }

        arguments.rm = rm
    }

    private fun rx(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val rx = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.rx != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'rx' の読み取りに失敗しました: セレクタ引数 rx は重複できません"))
            return
        }

        if (rx !in -90f..90f) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'rx' の読み取りに失敗しました: 縦回転は -90 ～ 90 の範囲が有効です"))
            return
        }

        arguments.rx = rx
    }

    private fun rxm(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val rxm = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.rxm != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'rxm' の読み取りに失敗しました: セレクタ引数 rxm は重複できません"))
            return
        }

        if (rxm !in -90f..90f) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'rxm' の読み取りに失敗しました: 縦回転は -90 ～ 90 の範囲が有効です"))
            return
        }

        arguments.rxm = rxm
    }

    private fun ry(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val ry = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.ry != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'ry' の読み取りに失敗しました: セレクタ引数 ry は重複できません"))
            return
        }

        if (ry !in -180f..180f) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'ry' の読み取りに失敗しました: 横回転は -180 ～ 180 の範囲が有効です"))
            return
        }

        arguments.ry = ry
    }

    private fun rym(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val rym = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.rym != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'rym' の読み取りに失敗しました: セレクタ引数 rym は重複できません"))
            return
        }

        if (rym !in -180f..180f) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'rym' の読み取りに失敗しました: 横回転は -180 ～ 180 の範囲が有効です"))
            return
        }

        arguments.rym = rym
    }

    private fun scores(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        reader.expect(Symbol.BRACE_START)

        if (reader.peek() `is` Symbol.BRACE_END) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'scores' の読み取りに失敗しました: 空の scores は無効です"))
            return
        }

        val scores = SelectorArguments.Scores()

        while (true) {
            val start2 = reader.getCurrentTokenStartPosition()
            val objective = reader.unfatalize(diagnostics) { readQuotableCommandString() }
            val end2 = reader.getPreviousTokenEndPosition()

            if (objective.nonnull) {
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, start2..end2))
            }

            reader.expect(Symbol.ASSIGN)
            val inverted = reader.readIsInverted()

            val start3 = reader.getCurrentTokenStartPosition()
            val range = reader.readIntRange()
            val end3 = reader.getPreviousTokenEndPosition()

            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start3..end3))

            if (objective is TokenSeqReader.ReadResult.Successful) {
                scores.add(
                    WithSpan(objective.value, start2..end2),
                    InvertibleValue(inverted, range, start3..end3)
                )
            }
            else {
                // nanimo sinai yo
            }

            if (reader.isOver()) {
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "セレクタ引数 'scores' の読み取りに失敗しました: 引数が '${Symbol.BRACE_END.value}' で閉じられていません")
            }
            else if (reader.peek() `is` Symbol.COMMA) {
                reader.next()
            }
            else if (reader.peek() `is` Symbol.BRACE_END) {
                reader.next()
                break
            }
            else {
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "セレクタ引数 'scores' の読み取りに失敗しました: 不明なトークンです: ${reader.peek()}")
            }
        }

        val end = reader.getPreviousTokenEndPosition()

        var failure = false

        if (scores.isEmpty()) {
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'scores' の読み取りに失敗しました: 空の条件を渡すことはできません"))
            failure = true
        }

        if (arguments.scores != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'scores' の読み取りに失敗しました: scores は重複できません"))
            failure = true
        }

        if (!failure) arguments.scores = scores
    }

    private fun tag(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        if (reader.peek() `is` Symbol.ARRAY_END || reader.peek() `is` Symbol.COMMA) {
            arguments.tag.add(InvertibleValue.uninverted(null, SourceSpan.NULL))
            return
        }

        val inverted = reader.readIsInverted()

        if (reader.peek() `is` Symbol.ARRAY_END || reader.peek() `is` Symbol.COMMA) {
            arguments.tag.add(InvertibleValue.inverted(null, SourceSpan.NULL))
            return
        }

        val start = reader.getCurrentTokenStartPosition()
        val string = reader.unfatalize(diagnostics) { readQuotableCommandString() }
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, start..end))

        if (string is TokenSeqReader.ReadResult.Successful) {
            arguments.tag.add(InvertibleValue(inverted, string.value, start..end))
        }
    }

    private fun type(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val inverted = reader.readIsInverted()
        val identifier = reader.readNamespacedIdentifier()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start..end))

        if (!inverted && arguments.type.any { !it.inverted }) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 type は否定でない限り重複できません"))
        }

        val entityType = EntityType.get(identifier)
            ?: run {
                diagnostics.add(reader.diagnostic(start..end, "セレクタ引数 'type' の読み取りに失敗しました: 不明なエンティティタイプです: $identifier"))
                return
            }

        arguments.type.add(InvertibleValue(inverted, entityType, start..end))
    }

    private fun x(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val x = reader.readCoordinateComponent(allowLocal = false)
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.x != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'x' の読み取りに失敗しました: セレクタ引数 x は重複できません"))
            return
        }

        arguments.x = x
    }

    private fun y(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val y = reader.readCoordinateComponent(allowLocal = false)
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.y != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'y' の読み取りに失敗しました: セレクタ引数 y は重複できません"))
            return
        }

        arguments.y = y
    }

    private fun z(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        val z = reader.readCoordinateComponent(allowLocal = false)
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (arguments.z != null) {
            diagnostics.add(reader.diagnostic(sourceSpan, "セレクタ引数 'z' の読み取りに失敗しました: セレクタ引数 z は重複できません"))
            return
        }

        arguments.z = z
    }

    private fun unknown(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, sourceSpan: SourceSpan) {
        val start = reader.getCurrentTokenStartPosition()
        reader.readQuotableCommandString(allowFloat = true)
        val end = reader.getPreviousTokenEndPosition()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ERROR, start..end))
    }

    private fun parseSelectorType(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): SelectorType? {
        val start = reader.getCurrentTokenStartPosition()

        val t = reader.peek()
        if (t.type == TokenType.DOUBLE_QUOTED) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            arguments.name.add(InvertibleValue.uninverted(t.value, start..end))
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SELECTOR_TYPE, start..end))
            return SelectorType.PLAYER
        }

        val string = reader.readCommandString(nonTerminators = charArrayOf('@'))
        val end = reader.getPreviousTokenEndPosition()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SELECTOR_TYPE, start..end))

        for (type in SelectorType.entries) {
            if (type.getString() == t.value) {
                return type
            }
        }

        if (string == KeyChar.WILDCARD.toString()) {
            return SelectorType.PLAYER
        }

        if ('@' in string) {
            diagnostics.add(reader.diagnostic(start..end, "セレクターの読み取りに失敗しました: 不明なセレクタータイプです: '$string'"))
            return null
        }

        return SelectorType.PLAYER
    }

    private fun parseSelectorArguments(reader: TokenSeqReader, arguments: SelectorArguments, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>) {
        val start = reader.getCurrentTokenStartPosition()
        if (reader.peek() `is` Symbol.ARRAY_START) {
            reader.next()
        }
        else {
            return
        }

        if (reader.isOver()) {
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数リストが '${Symbol.ARRAY_END.value}' で閉じられていません")
        }

        if (reader.peek() `is` Symbol.ARRAY_END) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            diagnostics.add(reader.diagnostic(start..end, "セレクタ引数を空にすることはできません"))
            return
        }

        while (true) {
            val start2 = reader.getCurrentTokenStartPosition()
            val key = reader.readCommandString()
            val end2 = reader.getPreviousTokenEndPosition()

            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.PARAMETER, start2..end2))

            val parser = parsers[key] ?: run {
                diagnostics.add(reader.diagnostic(start2..end2, "セレクタ引数の読み取りに失敗しました: セレクタ引数 '$key' は存在しません"))
                return@run ::unknown
            }

            val eqStart = reader.getCurrentTokenStartPosition()
            reader.expect(Symbol.ASSIGN)
            val eqEnd = reader.getPreviousTokenEndPosition()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, eqStart..eqEnd))

            if (reader.isOver()) {
                throw reader.exception(reader.getOverNextSourceSpan(), "セレクタ引数の読み取りに失敗しました: ${Symbol.ASSIGN.value} の後に値が欠落しています")
            }

            val keySpan = start2..end2

            parser(reader, arguments, diagnostics, semanticTokens, keySpan)

            val start4 = reader.getCurrentTokenStartPosition()
            if (reader.isOver()) {
                throw reader.exception(reader.getOverNextSourceSpan(), "セレクタ引数リストの読み取りに失敗しました: セレクタ引数リストが '${Symbol.ARRAY_END.value}' で閉じられていません")
            }
            else if (reader.peek() `is` Symbol.ARRAY_END) {
                reader.next()
                break
            }
            else if (reader.peek() `is` Symbol.COMMA) {
                reader.next()
            }
            else {
                val p = reader.peek()
                reader.next()
                val end4 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start4..end4, "セレクタ引数の読み取りに失敗しました: 不明なトークンです: $p")
            }
        }
    }

    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<EntitySelector> {
        val arguments = SelectorArguments()

        if (reader.isOver()) {
            throw reader.exception(reader.getOverNextSourceSpan(), "セレクターの読み取りに失敗しました: 入力が不足しています")
        }

        val start = reader.getCurrentTokenStartPosition()

        val type = parseSelectorType(reader, arguments, diagnostics, semanticTokens)

        if (type == SelectorType.PLAYER) {
            val end = reader.getPreviousTokenEndPosition()
            return Argument(this, EntitySelector(type, arguments), start..end)
        }

        if (!reader.isOver()) {
            parseSelectorArguments(reader, arguments, diagnostics, semanticTokens)
        }

        val end = reader.getPreviousTokenEndPosition()

        if (type == null) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        val selector = EntitySelector(type, arguments)
        validator?.invoke(reader, selector, start..end, diagnostics)

        return if (diagnostics.none { it.isError }) {
            Argument(this, selector, start..end)
        }
        else {
            NoValuedArgument(this, start..end, diagnostics)
        }
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        val completer = BclCompleter(context)

        val typeResult = completer.completeUnquotedString(
            candidates = listOf("@s", "@p", "@a", "@r", "@e", "@n", "@initiator"),
            CompletionItemKind.Variable,
            prefixable = setOf('@')
        )
        if (typeResult is BclCompleter.ShouldComplete) {
            return typeResult.items
        }
        completer.skipWhitespaceAndComments()

        val openResult = completer.completeChar(listOf('['), kind = CompletionItemKind.Keyword)
        if (openResult is BclCompleter.ShouldComplete) {
            return openResult.items
        }
        completer.skipWhitespaceAndComments()

        while (true) {
            val argResult = completer.completeUnquotedString(parsers.keys.toList(), CompletionItemKind.Property)
            if (argResult is BclCompleter.ShouldComplete) {
                return argResult.items
            }
            completer.skipWhitespaceAndComments()
            val argName = (argResult as BclCompleter.ShouldContinue).input

            (completer.completeChar(
                listOf('='),
                CompletionItemKind.Keyword
            ) as? BclCompleter.ShouldComplete)?.run {
                return items
            }
            completer.skipWhitespaceAndComments()

            val argCompleter = completers[argName]

            if (argCompleter == null) {
                val s = completer.completeQuotableString(emptyList(), CompletionItemKind.Text, prefixable = setOf('!'))
                if (s is BclCompleter.ShouldComplete) {
                    return emptyList()
                }
            }
            else {
                val argValRes = argCompleter(completer)
                if (argValRes is BclCompleter.ShouldComplete) {
                    return argValRes.items
                }
            }
            completer.skipWhitespaceAndComments()

            val lastResult = completer.completeChar(listOf(',', ']'), CompletionItemKind.Keyword)
            if (lastResult is BclCompleter.ShouldComplete) {
                return lastResult.items
            }
            completer.skipWhitespaceAndComments()
            lastResult as BclCompleter.ShouldContinue
            if (lastResult.input == ']'.toString()) {
                break
            }
        }

        return emptyList()
    }

    private fun completeZero(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completer.completeInteger(listOf(0), CompletionItemKind.Value)
    }

    private fun completeOne(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completer.completeInteger(listOf(1), CompletionItemKind.Value)
    }

    private fun completeFamily(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completer.combine(
            {
                val invertion = completeChar(listOf(KeyChar.EXCLAMATION), CompletionItemKind.Keyword)
                if (invertion is BclCompleter.ShouldComplete) return@combine invertion
                completeUnquotedString(emptyList(), CompletionItemKind.EnumMember)
            },
            {
                completeUnquotedString(emptyList(), CompletionItemKind.EnumMember)
            }
        )
    }

    private fun completeHasPermission(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completeMapLike(completer, SelectorArguments.HasPermission.Permission.strings) { completer, _ ->
            completer.completeUnquotedString(
                SelectorArguments.HasPermission.Permission.strings,
                CompletionItemKind.EnumMember
            )
        }
    }

    private fun completeHasProperty(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completeMapLike(
            completer,
            keys = AccessibleActorProperties.properties.map { it.identifier.toString() } + "property",
            callback = { completer, key ->
                if (key == "property") {
                    return@completeMapLike completer.completeUnquotedString(
                        AccessibleActorProperties.properties.map { it.identifier.toString() },
                        CompletionItemKind.EnumMember
                    )
                }

                val property = NamespacedIdentifier.parse(key)?.run(AccessibleActorProperties::getProperty)
                    ?: return@completeMapLike BclCompleter.ShouldComplete(emptyList())

                val entryCompleter: BclCompleter.() -> BclCompleter.ValueCompletionResult = when (property) {
                    is ActorProperty.EnumProperty -> {
                        { completeQuotedString(property.values, CompletionItemKind.EnumMember) }
                    }
                    is ActorProperty.IntegerProperty -> {
                        { completeInteger(property.values, CompletionItemKind.Value) }
                    }
                    is ActorProperty.BooleanProperty -> {
                        { completeUnquotedString(property.values.map { it.toString() }, CompletionItemKind.Value) }
                    }
                }

                return@completeMapLike completer.combine(
                    {
                        val invertion = completeChar(listOf('!'), CompletionItemKind.Keyword)
                        completer.skipWhitespaceAndComments()
                        if (invertion is BclCompleter.ShouldComplete) {
                            return@combine invertion
                        }
                        completer.skipWhitespaceAndComments()
                        return@combine completer.entryCompleter()
                    },
                    {
                        return@combine completer.entryCompleter()
                    }
                )
            }
        )
    }

    private fun completeHasItem(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completeTypedMapLike(
            completer,
            mapOf(
                "item" to {
                    it.completeUnquotedString(ItemType.IDENTIFIERS, CompletionItemKind.EnumMember)
                },
                "location" to {
                    it.completeUnquotedString(EntityContainerSlotType.strings, CompletionItemKind.EnumMember)
                },
                "slot" to { completer ->
                    completer.combine(
                        {
                            val invertion = completeChar(listOf('!'), CompletionItemKind.Keyword)
                            skipWhitespaceAndComments()
                            if (invertion is BclCompleter.ShouldComplete) return@combine invertion
                            invertion as BclCompleter.ShouldContinue
                            val slot = completeIntegerRange(listOf(InvertibleIntegerRange(min = 0, max = 8)), CompletionItemKind.Value)
                            skipWhitespaceAndComments()
                            if (slot is BclCompleter.ShouldComplete) return@combine slot
                            slot as BclCompleter.ShouldContinue
                            return@combine BclCompleter.ShouldContinue(invertion.input + slot.input)
                        },
                        {
                            completeIntegerRange(listOf(InvertibleIntegerRange(min = 0, max = 8)), CompletionItemKind.Value)
                        }
                    )
                },
                "quantity" to { completer ->
                    completer.combine(
                        {
                            val invertion = completeChar(listOf('!'), CompletionItemKind.Keyword)
                            skipWhitespaceAndComments()
                            if (invertion is BclCompleter.ShouldComplete) return@combine invertion
                            invertion as BclCompleter.ShouldContinue
                            val slot = completeIntegerRange(listOf(InvertibleIntegerRange.ZERO), CompletionItemKind.Value)
                            skipWhitespaceAndComments()
                            if (slot is BclCompleter.ShouldComplete) return@combine slot
                            slot as BclCompleter.ShouldContinue
                            return@combine BclCompleter.ShouldContinue(invertion.input + slot.input)
                        },
                        {
                            completeIntegerRange(listOf(InvertibleIntegerRange.ZERO), CompletionItemKind.Value)
                        }
                    )
                },
                "data" to { it.completeInteger(listOf(0), CompletionItemKind.Value) }
            )
        )
    }

    private fun completeTypedMapLike(completer: BclCompleter, keys: Map<String, (BclCompleter) -> BclCompleter.ValueCompletionResult>, fallback: ((BclCompleter, String) -> BclCompleter.ValueCompletionResult)? = null): BclCompleter.ValueCompletionResult {
        val sb = StringBuilder()

        val open = completer.completeChar(listOf('{'), CompletionItemKind.Keyword)
        completer.skipWhitespaceAndComments()
        if (open is BclCompleter.ShouldComplete) {
            return BclCompleter.ShouldComplete(listOf(
                CompletionItem("{}").apply {
                    insertTextFormat = InsertTextFormat.Snippet
                    insertText = "{$0}"
                }
            ))
        }

        sb.append('{')

        while (true) {
            val keyResult = completer.completeUnquotedString(
                keys.keys.toList(),
                CompletionItemKind.Property
            )
            completer.skipWhitespaceAndComments()
            if (keyResult is BclCompleter.ShouldComplete) {
                return keyResult
            }
            val key = (keyResult as BclCompleter.ShouldContinue).input
            sb.append(key)

            (completer.completeChar(listOf('='), CompletionItemKind.Keyword) as? BclCompleter.ShouldComplete)
                ?.run { return this }
            completer.skipWhitespaceAndComments()
            sb.append('=')

            val keyCompleter: ((BclCompleter, String) -> BclCompleter.ValueCompletionResult)? = keys[key]?.run {
                { completer, _ -> this(completer) }
            } ?: fallback

            if (keyCompleter == null) {
                val s = completer.completeQuotableString(emptyList(), CompletionItemKind.Text, prefixable = setOf('!'))
                completer.skipWhitespaceAndComments()
                if (s is BclCompleter.ShouldComplete) {
                    return BclCompleter.ShouldComplete(emptyList())
                }
                s as BclCompleter.ShouldContinue
                sb.append(s.input)
            }
            else {
                val valueResult = keyCompleter(completer, key)
                completer.skipWhitespaceAndComments()
                if (valueResult is BclCompleter.ShouldComplete) {
                    return valueResult
                }
                valueResult as BclCompleter.ShouldContinue
                sb.append(valueResult.input)
            }

            val lastResult = completer.completeChar(listOf(',', '}'), CompletionItemKind.Keyword)
            completer.skipWhitespaceAndComments()
            if (lastResult is BclCompleter.ShouldComplete) {
                return lastResult
            }
            lastResult as BclCompleter.ShouldContinue
            sb.append(lastResult.input)
            if (lastResult.input == '}'.toString()) {
                break
            }
        }

        return BclCompleter.ShouldContinue(sb.toString())
    }

    private fun completeMapLike(completer: BclCompleter, keys: List<String>, callback: (BclCompleter, String) -> BclCompleter.ValueCompletionResult): BclCompleter.ValueCompletionResult {
        val sb = StringBuilder()

        val open = completer.completeChar(listOf('{'), CompletionItemKind.Keyword)
        completer.skipWhitespaceAndComments()
        if (open is BclCompleter.ShouldComplete) {
            return BclCompleter.ShouldComplete(listOf(
                CompletionItem("{}").apply {
                    insertTextFormat = InsertTextFormat.Snippet
                    insertText = "{$0}"
                }
            ))
        }
        sb.append('{')

        while (true) {
            val keyResult = completer.completeUnquotedString(keys, CompletionItemKind.Property)
            completer.skipWhitespaceAndComments()
            if (keyResult is BclCompleter.ShouldComplete) {
                return keyResult
            }
            keyResult as BclCompleter.ShouldContinue
            sb.append(keyResult.input)

            (completer.completeChar(listOf('='), CompletionItemKind.Keyword) as? BclCompleter.ShouldComplete)
                ?.run { return this }
            completer.skipWhitespaceAndComments()
            sb.append('=')

            val valueResult = callback(completer, keyResult.input)
            completer.skipWhitespaceAndComments()
            if (valueResult is BclCompleter.ShouldComplete) {
                return valueResult
            }
            valueResult as BclCompleter.ShouldContinue
            sb.append(valueResult.input)

            val lastResult = completer.completeChar(listOf(',', '}'), CompletionItemKind.Keyword)
            completer.skipWhitespaceAndComments()
            if (lastResult is BclCompleter.ShouldComplete) {
                return lastResult
            }
            lastResult as BclCompleter.ShouldContinue
            sb.append(lastResult.input)
            if (lastResult.input == '}'.toString()) {
                break
            }
        }

        return BclCompleter.ShouldContinue(sb.toString())
    }

    private fun completeGameMode(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completer.combine(
            {
                val invertion = completeChar(listOf(KeyChar.EXCLAMATION), CompletionItemKind.Keyword)
                if (invertion is BclCompleter.ShouldComplete) return@combine invertion
                invertion as BclCompleter.ShouldContinue
                completeUnquotedString(
                    GameMode.entries.flatMap { it.getNames() },
                    CompletionItemKind.EnumMember
                )
            },
            {
                completeUnquotedString(
                    GameMode.entries.flatMap { it.getNames() },
                    CompletionItemKind.EnumMember
                )
            }
        )
    }

    private fun completeScores(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completeTypedMapLike(completer, emptyMap()) { completer, _ ->
            val invertion = completer.completeChar(listOf('!'), CompletionItemKind.Keyword, optional = true)
            if (invertion is BclCompleter.ShouldComplete) return@completeTypedMapLike invertion
            completer.skipWhitespaceAndComments()
            return@completeTypedMapLike completer.completeIntegerRange(
                ranges = listOf(InvertibleIntegerRange()),
                CompletionItemKind.Value
            )
        }
    }

    private fun completeName(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        val invertion = completer.completeChar(listOf('!'), CompletionItemKind.Keyword, optional = true)
        if (invertion is BclCompleter.ShouldComplete) return invertion
        return completer.completeQuotableString(emptyList(), CompletionItemKind.Text)
    }

    private fun completeTag(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        val invertion = completer.completeChar(listOf('!'), CompletionItemKind.Keyword, optional = true)
        if (invertion is BclCompleter.ShouldComplete) return invertion
        completer.skipWhitespaceAndComments()
        return completer.completeQuotableString(
            emptyList(), // TODO Dynamic Completion
            CompletionItemKind.Text
        )
    }

    private fun completeType(completer: BclCompleter): BclCompleter.ValueCompletionResult {
        return completer.combine(
            {
                val invertion = completeChar(listOf(KeyChar.EXCLAMATION), CompletionItemKind.Keyword)
                if (invertion is BclCompleter.ShouldComplete) return@combine invertion
                completeUnquotedString(EntityType.IDENTIFIERS, CompletionItemKind.EnumMember)
            },
            {
                completeUnquotedString(EntityType.IDENTIFIERS, CompletionItemKind.EnumMember)
            }
        )
    }

    companion object {
        fun players(): EntitySelectorArgumentType {
            return EntitySelectorArgumentType { selector, span, diagnostics ->
                if (!selector.isPlayer) {
                    diagnostics.add(diagnostic(span, "セレクターの読み取りに失敗しました: ここではプレイヤーセレクターが期待されています"))
                }
            }
        }

        fun single(): EntitySelectorArgumentType {
            return EntitySelectorArgumentType { selector, span, diagnostics ->
                if (!selector.isSingle) {
                    diagnostics.add(diagnostic(span, "セレクターの読み取りに失敗しました: ここでは単一のエンティティを指すセレクターが期待されています"))
                }
            }
        }
    }
}

package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.item.ItemType
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityContainerSlotType
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityType
import io.github.takenoko4096.bcl.brigadier.value.entity.GameMode
import io.github.takenoko4096.bcl.brigadier.value.entity.InvertibleValue
import io.github.takenoko4096.bcl.brigadier.value.entity.SelectorArguments
import io.github.takenoko4096.bcl.brigadier.value.entity.SelectorType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType

class EntitySelectorArgumentType(private val validator: (TokenSeqReader.(EntitySelector, SourceSpan) -> Unit)? = null) : ArgumentType<EntitySelector>("entity_selector") {
    private val parsers: Map<String, TokenSeqReader.(SelectorArguments) -> Unit> = mapOf(
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

    private fun TokenSeqReader.readIsInverted(): Boolean {
        return if (peek() `is` Symbol.LOGICAL_NOT) {
            next()
            true
        }
        else false
    }

    private fun c(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val c = reader.readInteger()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.c != null) {
            throw reader.exception(start..end, "セレクタ引数 'c' の読み取りに失敗しました: セレクタ引数 c は重複できません")
        }

        if (c == 0) {
            throw reader.exception(start..end, "セレクタ引数 'c' の読み取りに失敗しました: セレクタ引数 c は 0以外の整数が有効です")
        }

        arguments.c = WithSpan(c, start..end)
    }

    private fun dx(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val dx = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.dx != null) {
            throw reader.exception(start..end, "セレクタ引数 'dx' の読み取りに失敗しました: セレクタ引数 dx は重複できません")
        }

        arguments.dx = WithSpan(dx, start..end)
    }

    private fun dy(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val dy = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.dy != null) {
            throw reader.exception(start..end, "セレクタ引数 'dy' の読み取りに失敗しました: セレクタ引数 dy は重複できません")
        }

        arguments.dy = WithSpan(dy, start..end)
    }

    private fun dz(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val dz = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.dz != null) {
            throw reader.exception(start..end, "セレクタ引数 'dz' の読み取りに失敗しました: セレクタ引数 dz は重複できません")
        }

        arguments.dz = WithSpan(dz, start..end)
    }

    private fun family(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val inverted = reader.readIsInverted()
        val string = reader.readQuotableCommandString()
        val end = reader.getPreviousTokenEndPosition()
        arguments.family.add(InvertibleValue(inverted, string, start..end))
    }

    private fun hasPermission(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        reader.expect(Symbol.BRACE_START)

        if (reader.peek() `is` Symbol.BRACE_END) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数 'haspermission' の読み取りに失敗しました: 空の haspermission は無効です")
        }

        val hasPermission = SelectorArguments.HasPermission()

        while (true) {
            val name = reader.readCommandString()
            val permission = SelectorArguments.HasPermission.Permission.parse(name)
                ?: run {
                    val end = reader.getPreviousTokenEndPosition()
                    throw reader.exception(start..end, "セレクタ引数 'haspermission' の読み取りに失敗しました: 不明なパーミッション名です: $name")
                }

            reader.expect(Symbol.ASSIGN)

            val flag = reader.readCommandString()
            val state = SelectorArguments.HasPermission.PermissionState.parse(flag)
                ?: run {
                    val end = reader.getPreviousTokenEndPosition()
                    throw reader.exception(start..end, "セレクタ引数 'haspermission' の読み取りに失敗しました: 不明なパーミッションステートです: '$flag'; ${SelectorArguments.HasPermission.PermissionState.entries.joinToString(" | ") { it.getString() }} が期待されています")
                }
            hasPermission.set(permission, state)

            if (reader.isOver()) {
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "セレクタ引数 'haspermission' の読み取りに失敗しました: 引数が '${Symbol.BRACE_END.value}' で閉じられていません")
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
                throw reader.exception(start..end, "セレクタ引数 'haspermission' の読み取りに失敗しました: 不明なトークンです: ${reader.peek()}")
            }
        }

        if (arguments.hasPermission != null) {
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数 'haspermission' の読み取りに失敗しました: haspermission は重複できません")
        }

        arguments.hasPermission = hasPermission
    }

    private fun hasProperty(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()

        val properties = mutableListOf<InvertibleValue<NamespacedIdentifier>>()

        val list = reader.readPairList(
            Symbol.BRACE_START,
            Symbol.BRACE_END,
            kvSeparatorParser = {
                expect(Symbol.ASSIGN)
                val start = getCurrentTokenStartPosition()
                val inv = readIsInverted()
                val end = getPreviousTokenEndPosition()
                WithSpan(inv, if (inv) start..end else SourceSpan.NULL)
            },
            keyParser = {
                val start = getCurrentTokenStartPosition()
                val key = readCommandString()
                val end = getPreviousTokenEndPosition()

                if (key == "property") {
                    expect(Symbol.ASSIGN)
                    val inv = readIsInverted()
                    val start = getCurrentTokenStartPosition()
                    val identifier = readNamespacedIdentifier()
                    val end = getPreviousTokenEndPosition()
                    properties.add(InvertibleValue(inv, identifier, start..end))
                    return@readPairList null
                }

                return@readPairList NamespacedIdentifier.parse(key)
                    ?: run {

                        throw exception(start..end, "セレクタ引数 'has_property' の読み取りに失敗しました: 文字列 '$key' は識別子として無効です (識別子は '${NamespacedIdentifier.REGEX}' の形式に従う必要があります)")
                    }
            },
            valueParser = { inv ->
                val start = getCurrentTokenStartPosition()
                val flag = readBool()
                val end = getPreviousTokenEndPosition()
                InvertibleValue(inv.value, flag, start..end)
            }
        )

        val end = reader.getPreviousTokenEndPosition()

        if (arguments.hasProperty != null) {
            throw reader.exception(start..end, "セレクタ引数 'has_property' の読み取りに失敗しました: has_property は重複できません")
        }

        arguments.hasProperty = SelectorArguments.HasProperty(
            properties,
            list
        )
    }

    private fun itemPredicateObject(reader: TokenSeqReader): SelectorArguments.HasItem {
        val startG = reader.getCurrentTokenStartPosition()
        val map = reader.readTypedMap(
            prefix = Symbol.BRACE_START,
            postfix = Symbol.BRACE_END,
            keyParser = {
                readCommandString()
            },
            kvSeparatorParser = {
                expect(Symbol.ASSIGN)
                val start = getCurrentTokenStartPosition()
                val inv = readIsInverted()
                val end = getPreviousTokenEndPosition()
                WithSpan(inv, if (inv) start..end else SourceSpan.NULL)
            },
            valueParsers = mapOf(
                "item" to { inv ->
                    if (inv.value) {
                        throw exception(inv.sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました: キー 'item' は反転できません")
                    }

                    val start2 = getCurrentTokenStartPosition()
                    val id = readNamespacedIdentifier()
                    val type = ItemType.get(id)
                    val end = getPreviousTokenEndPosition()

                    if (type == null) {
                        throw exception(start2..end, "セレクタ引数 'hasitem' の読み取りに失敗しました:  アイテムタイプ '$id' は存在しません")
                    }

                    WithSpan(type, start2..end)
                },
                "slot" to { inv ->
                    val start = getCurrentTokenStartPosition()
                    val v = readIntRange()
                    val end = getPreviousTokenEndPosition()
                    InvertibleValue(inv.value, v, start..end)
                },
                "quantity" to { inv ->
                    val start = getCurrentTokenStartPosition()
                    val v = readIntRange()
                    val end = getPreviousTokenEndPosition()
                    InvertibleValue(inv.value, v, start..end)
                },
                "location" to { inv ->
                    if (inv.value) {
                        throw exception(inv.sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました:  キー 'location' は反転できません")
                    }

                    val start2 = getCurrentTokenStartPosition()
                    val location = readCommandString()
                    val slotType = EntityContainerSlotType.parse(location)
                        ?: run {
                            val end = getPreviousTokenEndPosition()
                            throw exception(start2..end, "セレクタ引数 'hasitem' の読み取りに失敗しました:  location '$location' は存在しません")
                        }

                    val end = getPreviousTokenEndPosition()
                    WithSpan(slotType, start2..end)
                },
                "data" to { inv ->
                    if (inv.value) {
                        throw exception(inv.sourceSpan, "セレクタ引数 'hasitem' の読み取りに失敗しました:  キー 'data' は反転できません")
                    }

                    val start = getCurrentTokenStartPosition()
                    val v = readInteger()
                    val end = getPreviousTokenEndPosition()
                    WithSpan(v, start..end)
                }
            )
        )
        val endG = reader.getPreviousTokenEndPosition()

        val item = map["item"] as WithSpan<ItemType>?
        val location = map["location"] as WithSpan<EntityContainerSlotType>?
        val slot = map["slot"] as InvertibleValue<IntRange>?
        val quantity = map["quantity"] as InvertibleValue<IntRange>?
        val data = map["data"] as WithSpan<Int>?

        if (item == null) {
            throw reader.exception(startG..endG, "セレクタ引数 'hasitem' の読み取りに失敗しました: キー 'item' は必須です")
        }

        return SelectorArguments.HasItem(item, location, slot, quantity, data, startG..endG)
    }

    private fun hasItem(reader: TokenSeqReader, arguments: SelectorArguments) {
        val list = mutableListOf<SelectorArguments.HasItem>()

        val start = reader.getCurrentTokenStartPosition()

        val t = reader.peek()
        if (t `is` Symbol.ARRAY_START) {
            list.addAll(reader.readList(Symbol.ARRAY_START, Symbol.ARRAY_END) {
                itemPredicateObject(this)
            })
        }
        else if (t `is` Symbol.BRACE_START) {
            list.add(itemPredicateObject(reader))
        }
        else {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数 'hasitem' の読み取りに失敗しました: 不明なトークンです: $t")
        }

        val end = reader.getPreviousTokenEndPosition()

        if (arguments.hasItem.isNotEmpty()) {
            throw reader.exception(start..end, "セレクタ引数 'hasitem' の読み取りに失敗しました: hasitem は重複できません")
        }

        arguments.hasItem.addAll(list)
    }

    private fun l(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val l = reader.readInteger()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.l != null) {
            throw reader.exception(start..end, "セレクタ引数 'l' の読み取りに失敗しました: セレクタ引数 l は重複できません")
        }

        if (l < 0) {
            throw reader.exception(start..end, "セレクタ引数 'l' の読み取りに失敗しました: 負のレベルを条件にすることはできません")
        }

        arguments.l = l
    }

    private fun lm(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val lm = reader.readInteger()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.lm != null) {
            throw reader.exception(start..end, "セレクタ引数 'lm' の読み取りに失敗しました: セレクタ引数 lm は重複できません")
        }

        if (lm < 0) {
            throw reader.exception(start..end, "セレクタ引数 'lm' の読み取りに失敗しました: 負のレベルを条件にすることはできません")
        }

        arguments.lm = lm
    }

    private fun m(reader: TokenSeqReader, arguments: SelectorArguments) {
        val startFromInv = reader.getCurrentTokenStartPosition()
        val inv = reader.readIsInverted()
        val start = reader.getCurrentTokenStartPosition()
        val s = reader.readCommandString(allowFloat = true)
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.m != null) {
            throw reader.exception(startFromInv..end, "セレクタ引数 'm' の読み取りに失敗しました: セレクタ引数 m は \"\"否定であっても\"\" 重複できません")
        }

        val gameMode = GameMode.entries.find { it.getNames().contains(s) }
            ?: run {
                throw reader.exception(start..end, "セレクタ引数 'm' の読み取りに失敗しました: ゲームモード '$s' は存在しません")
            }

        arguments.m = InvertibleValue(inv, gameMode, start..end)
    }

    private fun name(reader: TokenSeqReader, arguments: SelectorArguments) {
        val startFromInv = reader.getCurrentTokenStartPosition()
        val inverted = reader.readIsInverted()
        val start = reader.getCurrentTokenStartPosition()
        val name = reader.readQuotableCommandString()
        val end = reader.getPreviousTokenEndPosition()

        if (!inverted && arguments.name.any { !it.inverted }) {
            throw reader.exception(startFromInv..end, "セレクタ引数 'name' の読み取りに失敗しました: name は否定でない限り重複できません")
        }

        arguments.name.add(InvertibleValue(inverted, name, start..end))
    }

    private fun r(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val r = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.r != null) {
            throw reader.exception(start..end, "セレクタ引数 'r' の読み取りに失敗しました: セレクタ引数 r は重複できません")
        }

        if (r < 0) {
            throw reader.exception(start..end, "セレクタ引数 'r' の読み取りに失敗しました: 半径を負の値にすることはできません")
        }

        arguments.r = r
    }

    private fun rm(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val rm = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.rm != null) {
            throw reader.exception(start..end, "セレクタ引数 'rm' の読み取りに失敗しました: セレクタ引数 rm は重複できません")
        }

        if (rm < 0) {
            throw reader.exception(start..end, "セレクタ引数 'rm' の読み取りに失敗しました: 半径を負の値にすることはできません")
        }

        arguments.rm = rm
    }

    private fun rx(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val rx = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.rx != null) {
            throw reader.exception(start..end, "セレクタ引数 'rx' の読み取りに失敗しました: セレクタ引数 rx は重複できません")
        }

        if (rx !in -90f..90f) {
            throw reader.exception(start..end, "セレクタ引数 'rx' の読み取りに失敗しました: 縦回転は -90 ～ 90 の範囲が有効です")
        }

        arguments.rx = rx
    }

    private fun rxm(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val rxm = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.rxm != null) {
            throw reader.exception(start..end, "セレクタ引数 'rxm' の読み取りに失敗しました: セレクタ引数 rxm は重複できません")
        }

        if (rxm !in -90f..90f) {
            throw reader.exception(start..end, "セレクタ引数 'rxm' の読み取りに失敗しました: 縦回転は -90 ～ 90 の範囲が有効です")
        }

        arguments.rxm = rxm
    }

    private fun ry(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val ry = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.ry != null) {
            throw reader.exception(start..end, "セレクタ引数 'ry' の読み取りに失敗しました: セレクタ引数 ry は重複できません")
        }

        if (ry !in -180f..180f) {
            throw reader.exception(start..end, "セレクタ引数 'ry' の読み取りに失敗しました: 横回転は -180 ～ 180 の範囲が有効です")
        }

        arguments.ry = ry
    }

    private fun rym(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val rym = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.rym != null) {
            throw reader.exception(start..end, "セレクタ引数 'rym' の読み取りに失敗しました: セレクタ引数 rym は重複できません")
        }

        if (rym !in -180f..180f) {
            throw reader.exception(start..end, "セレクタ引数 'rym' の読み取りに失敗しました: 横回転は -180 ～ 180 の範囲が有効です")
        }

        arguments.rym = rym
    }

    private fun scores(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        reader.expect(Symbol.BRACE_START)

        if (reader.peek() `is` Symbol.BRACE_END) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数 'scores' の読み取りに失敗しました: 空の scores は無効です")
        }

        val scores = SelectorArguments.Scores()

        while (true) {
            val start2 = reader.getCurrentTokenStartPosition()
            val objective = reader.readQuotableCommandString()
            val end2 = reader.getPreviousTokenEndPosition()
            reader.expect(Symbol.ASSIGN)
            val start3 = reader.getCurrentTokenStartPosition()
            val inverted = reader.readIsInverted()
            val range = reader.readIntRange()
            val end3 = reader.getPreviousTokenEndPosition()

            scores.add(WithSpan(objective, start2..end2), InvertibleValue(inverted, range, start3..end3))

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

        if (scores.isEmpty()) {
            throw reader.exception(start..end, "セレクタ引数 'scores' の読み取りに失敗しました: 空の条件を渡すことはできません")
        }

        if (arguments.scores != null) {
            throw reader.exception(start..end, "セレクタ引数 'scores' の読み取りに失敗しました: scores は重複できません")
        }

        arguments.scores = scores
    }

    private fun tag(reader: TokenSeqReader, arguments: SelectorArguments) {
        if (reader.peek() `is` Symbol.ARRAY_END || reader.peek() `is` Symbol.COMMA) {
            arguments.tag.add(InvertibleValue.uninverted(null, SourceSpan.NULL))
            return
        }

        val start = reader.getCurrentTokenStartPosition()
        val inverted = reader.readIsInverted()

        if (reader.peek() `is` Symbol.ARRAY_END || reader.peek() `is` Symbol.COMMA) {
            val end = reader.getPreviousTokenEndPosition()
            arguments.tag.add(InvertibleValue.inverted(null, start..end))
            return
        }

        val string = reader.readQuotableCommandString()
        val end = reader.getPreviousTokenEndPosition()
        arguments.tag.add(InvertibleValue(inverted, string, start..end))
    }

    private fun type(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val inverted = reader.readIsInverted()

        val identifier = reader.readNamespacedIdentifier()
        val entityType = EntityType.get(identifier)
            ?: run {
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "セレクタ引数 'type' の読み取りに失敗しました: 不明なエンティティタイプです: $identifier")
            }

        val end = reader.getPreviousTokenEndPosition()

        if (!inverted && arguments.type.any { !it.inverted }) {
            throw reader.exception(start..end, "セレクタ引数 type は否定でない限り重複できません")
        }

        arguments.type.add(InvertibleValue(inverted, entityType, start..end))
    }

    private fun x(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val x = reader.readCoordinateComponent(allowLocal = false)
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.x != null) {
            throw reader.exception(start..end, "セレクタ引数 'x' の読み取りに失敗しました: セレクタ引数 x は重複できません")
        }

        arguments.x = x
    }

    private fun y(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val y = reader.readCoordinateComponent(allowLocal = false)
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.y != null) {
            throw reader.exception(start..end, "セレクタ引数 'y' の読み取りに失敗しました: セレクタ引数 y は重複できません")
        }

        arguments.y = y
    }

    private fun z(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        val z = reader.readCoordinateComponent(allowLocal = false)
        val end = reader.getPreviousTokenEndPosition()

        if (arguments.z != null) {
            throw reader.exception(start..end, "セレクタ引数 'z' の読み取りに失敗しました: セレクタ引数 z は重複できません")
        }

        arguments.z = z
    }

    private fun parseSelectorType(reader: TokenSeqReader, arguments: SelectorArguments): SelectorType {
        val start = reader.getCurrentTokenStartPosition()

        val t = reader.peek()
        return when (t.type) {
            TokenType.DOUBLE_QUOTED -> {
                reader.next()
                val end = reader.getPreviousTokenEndPosition()
                arguments.name.add(InvertibleValue.uninverted(t.value, start..end))
                SelectorType.PLAYER
            }
            TokenType.WORD -> run {
                for (type in SelectorType.entries) {
                    if (type.getString() == t.value) {
                        reader.next()
                        return@run type
                    }
                }

                reader.next()
                val end = reader.getPreviousTokenEndPosition()

                if (t.value.startsWith(Symbol.AT.value)) {
                    throw reader.exception(start..end, "セレクターの読み取りに失敗しました: 不明なセレクタータイプです: '${t.value}'")
                }

                arguments.name.add(InvertibleValue.uninverted(t.value, start..end))
                SelectorType.PLAYER
            }
            TokenType.SYMBOL -> {
                if (t `is` Symbol.MULTIPLY) {
                    reader.next()
                    val end = reader.getPreviousTokenEndPosition()
                    arguments.name.add(InvertibleValue.uninverted(t.value, start..end))
                    SelectorType.PLAYER
                }
                else {
                    reader.next()
                    val end = reader.getPreviousTokenEndPosition()
                    throw reader.exception(start..end, "セレクターの読み取りに失敗しました: 不明なセレクタータイプです ('${t.value}')")
                }
            }
            else -> {
                reader.next()
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "セレクターの読み取りに失敗しました: 不明なセレクタータイプです ('${t.value}')")
            }
        }
    }

    private fun parseSelectorArguments(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()
        if (reader.peek() `is` Symbol.ARRAY_START) {
            reader.next()
        }
        else {
            return
        }

        if (reader.peek() `is` Symbol.ARRAY_END) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数を空にすることはできません")
        }

        while (true) {
            val start2 = reader.getCurrentTokenStartPosition()

            val key = reader.readCommandString()
            if (key !in parsers) {
                val end2 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start2..end2, "セレクタ引数の読み取りに失敗しました: セレクタ引数 '$key' は存在しません")
            }

            reader.expect(Symbol.ASSIGN)

            val parser = parsers[key]!!

            parser(reader, arguments)

            val start3 = reader.getCurrentTokenStartPosition()
            if (reader.isOver()) {
                val end2 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start2..end2, "セレクタ引数リストの読み取りに失敗しました: セレクタ引数リストが '${Symbol.ARRAY_END.value}' で閉じられていません")
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
                val end3 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start3..end3, "セレクタ引数の読み取りに失敗しました: 不明なトークンです: $p")
            }
        }
    }

    override fun parse(reader: TokenSeqReader): Argument<EntitySelector> {
        val arguments = SelectorArguments()

        val start = reader.getCurrentTokenStartPosition()

        val type = parseSelectorType(reader, arguments)

        if (type == SelectorType.PLAYER) {
            val end = reader.getPreviousTokenEndPosition()
            return Argument(this, EntitySelector(type, arguments), start..end)
        }

        if (!reader.isOver()) {
            parseSelectorArguments(reader, arguments)
        }

        val end = reader.getPreviousTokenEndPosition()

        val selector = EntitySelector(type, arguments)
        validator?.invoke(reader, selector, start..end)

        return Argument(this, selector, start..end)
    }

    companion object {
        fun players(): EntitySelectorArgumentType {
            return EntitySelectorArgumentType { selector, span ->
                if (!selector.isPlayer) {
                    throw exception(span, "セレクターの読み取りに失敗しました: ここではプレイヤーセレクターが期待されています")
                }
            }
        }

        fun single(): EntitySelectorArgumentType {
            return EntitySelectorArgumentType { selector, span ->
                if (!selector.isSingle) {
                    throw exception(span, "セレクターの読み取りに失敗しました: ここでは単一のエンティティを指すセレクターが期待されています")
                }
            }
        }
    }
}

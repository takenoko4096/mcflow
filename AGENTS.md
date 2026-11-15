# 開発方針

- 明示的に依頼されない限り、ソースコードを変更しないこと。
- 設計について議論する際は、メリットだけでなくデメリットも説明すること。
- 不明確な仕様について、勝手に仮定して実装方針を決定しないこと。
- 既存の設計を尊重しつつ、問題点があれば率直に指摘すること。
- コードを提案する際は、その設計上の理由も説明すること。

# プロジェクトに関する基本情報

- プロジェクト名: 「mcflow」
- ゲーム「Minecraft Bedrock Edition」(**NOT** Java Edition) の「コマンド」専用言語サーバー
- Kotlin で lsp4j 1.0.0 を使用して開発中
- 但し、諸事情で以下のような拡張構文を、拡張子 `.mcflow` に対してのみ認める：

## 拡張構文一覧

### エスケープシーケンスによる行継続 (実装済み)

通常:
```mcfunction
execute as @a at @s if score @s obj = @e[c=1] obj run say foo
```

拡張構文:
```mcflow
execute as @a \
    at @s \
    if score @s obj = @e[c=1] obj \
    run say foo
```

### コマンド先頭の `/` の許可 (実装済み)

通常:
```mcfunction
say foo
```

拡張構文:
```mcflow
/say foo
```

### ブロックコメント (実装済み)

通常:
```mcfunction
# This is a comment.
```

拡張構文:
```mcflow
This is a line comment.

#::
    This is a block comment.
    #::
        nest
    ::#
::#
```

### '@' で開始されるステートメント (未実装)

例:
```mcflow
@if (true) {
    /say foo
}
@else {
    /say bar
}
```

## その他注意が必要な点:

- Bedrock Edition のコマンドの仕様は Java Edition のそれとやや異なる。特に、`teleport ~~1~` や `give@s apple 1` のようにスペース(` `) が無くても、代わりに記号(`~` や `@` 等。)が区切りとして機能するケースが多い。

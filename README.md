# bedrock-command-lsp

## 未実装

- Parser
  - 文字列マクロ
  - `@calculate`, `@define`
- BedrockBrigadier
  - より多くの `ArgumentType` の実装
    - `AbstractJson`
    - `RawText`
    - `EntitySelector`
      - テスト足りてない
      - `has_property` のプロパティ名バリデーション

## 制約

コマンドラインに文字列マクロを除く拡張構文はない。例えば
```mcfunction
@calculate x = 42
scoreboard players set @s test x
```
は解釈不可能。

```mcfunction
@define x = (@s test)
@calculate x = 42
```

または

```mcfunction
@calculate (@s test) = 42
```

と記述する。

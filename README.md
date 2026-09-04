# bedrock-command-lsp

## 未実装

- Tokenizer
  - 相対／ローカル座標のテスト
- Parser
  - 文字列マクロ
  - `@calculate`, `@define`
- BedrockBrigadier
  - より多くの `ArgumentType` の実装
    - `Enum`, `Selector`, `Pos`, `Rot`, `Identifier`

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

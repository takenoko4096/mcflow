# bedrock-command-lsp

## 未実装

- Parser
  - 文字列マクロ
  - `@calculate`, `@define`
- BedrockBrigadier
  - より多くの `ArgumentType` の実装
    - `EntitySelector`
      - `has_property` のプロパティ名バリデーション
  - completion
  - `ArgumentNodeBuilder::complete()` の提供
  - 残りのコマンドの実装

## 既知のバグ
- 文字列内で `\` を使用して改行すると semantic highlighting がおかしくなる
- `StringArgumentType.greedy()` の改行時の semantic highlighting バグ

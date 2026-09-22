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
  - `ArgumentNodeBuilder::complete()` の提供

## 既知のバグ
- `ItemNbtComponentsArgumentType`, `RawTextComponentArgumentType` がキーの重複時使用されないキーの値を解釈しない
- `RawTextComponentArgumentType` の `score` が 必要なキーを含まない場合キャスト例外やヌルポインタ例外を投げる

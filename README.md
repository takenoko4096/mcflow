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
  - Education をオンにした際のみに利用可能な `EntityType`, `BlockType`, `ItemType`
  - `requires {}`, BDS, Education Commands

## 既知のバグ
- コマンドノードの実行時に致命例外が投げられた場合コマンドラインの全範囲を選択する処理が走るが、semantic token 追加時に改行を含んでいても自動分割が行われない
- クォートに囲まれた文字列内で `\` を使用して改行すると semantic highlighting がおかしくなる
- スポーンイベント引数が有効なスポーンイベントであることを要求する
- 大文字・小文字が区別されてしまう

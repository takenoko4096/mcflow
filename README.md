# mcflow

Minecraft Bedrock Edition用高精度コマンド言語サーバー及びその関連ツール

## 未実装

### brigadier

- `ArgumentNodeBuilder::complete()`
- `requires()`
- Education Edition
- Bedrock Dedicated Server

### arguments
- `LootTableArgumentType`
- `EntitySelectorArgumentType`
- `RawTextComponentArgumentType`
  - セレクターの補完

## 既知のバグ

### 原因特定済

- コマンドノードの実行時に致命例外が投げられた場合コマンドラインの全範囲を選択する処理が走るが、semantic token 追加時に改行を含んでいても自動分割が行われない
- スポーンイベント引数が有効なスポーンイベントであることを要求する
- 大文字・小文字が区別されてしまう

### 未特定／未調査

- クォートに囲まれた文字列内で `\` を使用して改行すると semantic highlighting がおかしくなる

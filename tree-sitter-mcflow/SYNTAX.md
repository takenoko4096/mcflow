# 文法事項

## 普通のコマンドの例

```
playsound random.chestclosed @a ~ ~ ~ 1 1 1
give @s apple
summon allay "a" ^ ^ ^1
summon minecraft:armor_stand ^ ^ ^1
give @s apple 1 0 {"minecraft:can_destroy":{"blocks":["sand"]}}
kill @s[has_property={property=minecraft:armadillo_state, minecraft:armadillo_state="rolled_up", minecraft:can_move=!false}]
```

## コメント + 拡張構文

これに対して、

- 先頭の `/` が許可される
- ラインコメント `# comment` が許可される
- ブロックコメント `#:: block comment ::#` が許可される
- ブロックコメントは無条件で行を跨げる
- ブロックコメントはネストできる
- 行継続 `\` + `\n` が許可される
- 行継続を使用して行A, Bを連結したとき、A, Bの間には空行/コメント行/行継続がいくつあっても問題ない
- `#/` (`#` + `/` ではない) で開始される行はコメントコマンド
- コメントコマンドは即時に解析・実行されてそれ以降の行の解析に影響を及ぼす

ただし:

- 上の説明における「+」とは、ホワイトスペース及びコメントを無視したとき、トークンが隣接していることを意味する
- `"Quoted String"` 内ではコメントや行継続を利用できない
- コメント内では行継続を利用できない
- ラインコメント内では行を跨ぐブロックコメントを利用できない(つまりブロックコメントと見做されない)
- コメントコマンドには行継続機能とコメント機能はない
- 行継続後にコマンド内部に現れたコメントコマンドはコマンドの子ノードとして扱う

以下はすべて有効:

```
# standalone comment

give @s apple # trailing comment

#:: block
comment ::#

summon allay \
    # continuation comment

    "a" ^ ^ ^1

#/ comment command

give @s apple

give @s \
    #/first
    apple \
    #/second
    1 0
```

以上に対して次を期待する:
```
(source_file
  (command_line)
  (command_line)
  (command_line)
  (comment_command)
  (command_line)
  (command_line
    (comment_command)
    (comment_command)))
```

## 

私がtree-sitterに求めるのは、以上の文法に従って
**「コマンドライン」「コメントコマンド」の開始及び終端の位置を正確に取ること**
だけなので、それさえ満たせば設計のすべてはあなたにお任せする
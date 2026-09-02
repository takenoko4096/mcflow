# bedrock-command-lsp

コマンドラインに拡張構文はない。例えば
```mcfunction
@calculate x = 42
scoreboard players set @s test x
```
は解釈不可能。

これを行うには、
```mcfunction
#define x @s test
@calculate x = 42
```
のように前もって明示的に変換する必要がある。
ここで、命令 `#define` は `#define <String> <ScoreHolder>`

故に `IntegerArgumentType` と `IntExpression` はまったくの別物。

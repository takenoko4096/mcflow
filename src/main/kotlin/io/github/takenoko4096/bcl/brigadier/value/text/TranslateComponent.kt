package io.github.takenoko4096.bcl.brigadier.value.text

class TranslateComponent(val translate: String, val with: TranslateWith) : AbstractTextComponent() {
    sealed class TranslateWith
}
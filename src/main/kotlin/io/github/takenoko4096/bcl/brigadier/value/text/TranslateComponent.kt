package io.github.takenoko4096.bcl.brigadier.value.text

class TranslateComponent(val translate: String, val with: With) : AbstractTextComponent() {
    sealed class With

    class Strings(val strings: List<String>) : With()
}

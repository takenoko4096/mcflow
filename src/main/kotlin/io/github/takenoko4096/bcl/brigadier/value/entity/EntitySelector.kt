package io.github.takenoko4096.bcl.brigadier.value.entity

class EntitySelector {
    sealed class OptionState

    class SetOnceOptionState : OptionState()
    class InvertibleSetOptionState : OptionState()
}

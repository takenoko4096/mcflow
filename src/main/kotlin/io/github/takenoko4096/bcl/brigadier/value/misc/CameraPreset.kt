package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

enum class CameraPreset(namespace: String = NamespacedIdentifier.DEFAULT_NAMESPACE, value: String? = null): CommandEnumerable {
    FIRST_PERSON,
    FIXED_BOOM,
    FOLLOW_ORBIT,
    FREE,
    THIRD_PERSON,
    THIRD_PERSON_FRONT,
    CONTROL_SCHEME_CAMERA;

    val identifier = NamespacedIdentifier(namespace, value ?: name.lowercase())

    override fun getNames(): Set<String> {
        return setOf(identifier.toString())
    }
}

package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.ast.expression.Expression

class RequiredArgumentNode<T : Expression>(val name: String, val type: T) : ArgumentNode() {

}

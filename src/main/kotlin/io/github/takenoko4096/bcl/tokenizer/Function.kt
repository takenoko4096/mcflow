package io.github.takenoko4096.bcl.tokenizer

infix fun Token?.`is`(symbol: Symbol): Boolean {
    return this?.`is`(symbol) == true
}

infix fun Token?.`!is`(symbol: Symbol): Boolean {
    return this?.`is`(symbol) == false
}

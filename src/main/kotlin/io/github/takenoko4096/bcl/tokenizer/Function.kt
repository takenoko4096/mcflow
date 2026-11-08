package io.github.takenoko4096.bcl.tokenizer

import io.github.takenoko4096.bcl.parser.Keyword

infix fun Token?.`is`(symbol: Symbol): Boolean {
    return this?.`is`(symbol) == true
}

infix fun Token?.`!is`(symbol: Symbol): Boolean {
    return this?.`is`(symbol) == false
}

infix fun Token?.`is`(keyword: Keyword): Boolean {
    return this?.`is`(keyword) == true
}

infix fun Token?.`!is`(keyword: Keyword): Boolean {
    return this?.`is`(keyword) == false
}

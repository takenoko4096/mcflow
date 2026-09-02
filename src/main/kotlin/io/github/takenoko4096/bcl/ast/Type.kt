package io.github.takenoko4096.bcl.ast

enum class Type(val value: String) {
    VOID("void"),
    BOOLEAN("bool"),
    INT("int"),
    UINT("uint"),
    FLOAT("float"),
    STRING("str"),
    SELECTOR("selector"),
    POSITION("pos"),
    ROTATION("rot"),
    ITEM_TYPE("item_type"),
    JSON("json")
}

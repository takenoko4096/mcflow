package io.github.takenoko4096.bcl.tokenizer

enum class TokenType(private val description: String) {
    INT_RAD2("符号付き2進整数"),
    INT_RAD8("符号付き8進整数"),
    INT("符号付き10進整数"),
    INT_RAD16("符号付き16進整数"),

    UINT_RAD2("符号無し2進整数"),
    UINT_RAD8("符号無し8進整数"),
    UINT("符号無し10進整数"),
    UINT_RAD16("符号無し16進整数"),

    FLOAT("浮動小数点数"),

    SINGLE_QUOTED("シングルクォート文字列"),
    DOUBLE_QUOTED("ダブルクォート文字列"),
    BACK_QUOTED("バッククォート文字列"),
    WORD("クォート無し文字列"),

    SYMBOL("記号"),

    COORDINATE_RELATIVE("相対座標成分"),
    COORDINATE_LOCAL("ローカル座標成分"),

    TIME_D("日数"),
    TIME_S("秒数"),
    TIME_T("ティック数"),

    INVALID("無効なトークン");

    override fun toString(): String {
        return "$name ($description)"
    }
}

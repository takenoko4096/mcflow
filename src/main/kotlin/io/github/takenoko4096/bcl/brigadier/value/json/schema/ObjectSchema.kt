package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue

class ObjectSchema(val keys: Map<String, KeySchema>) : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        if (value !is CommandJsonObject) {
            diagnostics.add(BclDiagnostic("ここではオブジェクトが期待されています", sourceCode, value.sourceSpan))
            return false
        }

        var result = true

        for ((name, keySchema) in keys) {
            if (name !in value && !keySchema.isOptional) {
                diagnostics.add(BclDiagnostic("キー '$name' は必須です", sourceCode, value.sourceSpan))
                result = false
            }

            for ((key, child) in value.entries.filter { it.first.value == name }) {
                if (!keySchema.isNullable && child.value == null) {
                    diagnostics.add(BclDiagnostic("キー '$name' は null を受け入れません", sourceCode, child.sourceSpan))
                    result = false
                }

                if (!keySchema.duplicatable && value.entries.count { it.first.value == key.value } > 1) {
                    diagnostics.add(BclDiagnostic("キー '${key.value}' が重複しています", sourceCode, key.sourceSpan))
                    result = false
                }

                var keyResult = false
                val dummyDiagnostics = mutableListOf<BclDiagnostic>()
                for (schema in keySchema.schemas) {
                    if (schema.validate(child, sourceCode, dummyDiagnostics)) {
                        keyResult = true
                    }
                }

                if (!keyResult) {
                    diagnostics.addAll(dummyDiagnostics)
                    result = false
                }
            }
        }

        return result
    }
}

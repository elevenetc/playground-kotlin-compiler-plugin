package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.compiler.plugin.CliOption

internal fun Option(name: String, description: String): CliOption {
    return CliOption(
        optionName = name,
        valueDescription = description,
        description = description,
    )
}
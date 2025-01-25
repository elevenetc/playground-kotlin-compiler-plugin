package org.jetbrains.kotlin.utils

import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder

class KeepTempFolder : TemporaryFolder() {
    override fun after() {
        //ignore
    }
}
package org.jetbrains.kotlin

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ClassMethodsCallsTracer {

    companion object {
        @JvmStatic
        val instance = ClassMethodsCallsTracer()
    }

    private val log = mutableMapOf<String, MutableList<ClassMethodTrace>>()

    fun start(classFqn: String, methodFqn: String, params: Map<String, String> = emptyMap()): String {
        val id = Uuid.random().toString()
        val list = log.getOrDefault(classFqn, mutableListOf())
        list.add(ClassMethodTrace(id, methodFqn, params, System.currentTimeMillis()))
        return id
    }

    fun end(classFqn: String, startId: String, returnValue: String? = null) {
        val classEntry = log[classFqn] ?: error("End called before start for `$classFqn`")
        val callEntry = classEntry.firstOrNull { it.id == startId }
            ?: error("End called before start for `$classFqn`. Start id (`$startId`) isn't found")
        val idx = classEntry.indexOf(callEntry)

        callEntry.copy(callEnd = System.currentTimeMillis(), returnValue = returnValue).also { entry ->
            classEntry[idx] = entry
        }
    }

    fun getLog(): Map<String, List<ClassMethodTrace>> {
        return log.toMap()
    }
}

data class ClassMethodTrace(
    val id: String,
    var methodFqn: String,
    val params: Map<String, String> = emptyMap(),
    val callStart: Long,
    val callEnd: Long = -1,
    val returnValue: String? = null
)
package org.jetbrains.kotlin

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
@IgnoreCallLog
class InstanceLogger {

    companion object Companion {
        @JvmStatic
        val instance = InstanceLogger()
    }

    private val log = mutableMapOf<String, MutableList<ClassMethodTrace>>()

    fun start(classFqn: String, methodFqn: String, params: Map<String, String> = emptyMap()): String {
        val uuid = Uuid.random()
        val id = uuid.toJavaUuid().toString()
        val list = log.getOrDefault(classFqn, mutableListOf())
        list.add(ClassMethodTrace(id, methodFqn, params, System.currentTimeMillis()))
        log[classFqn] = list
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
    var methodName: String,
    val params: Map<String, String> = emptyMap(),
    val callStart: Long,
    val callEnd: Long = -1,
    val returnValue: String? = null
)

@IgnoreCallLog
class InstanceLoggerDumper {

    companion object {
        @JvmStatic
        val instance = InstanceLoggerDumper()
    }

    private val subDir = "instance-log-dump"
    private val filePrefix = "instance-log"

    private val json = Json {
        prettyPrint = true
    }

    fun dump(logger: InstanceLogger) {
        storeDump(stringify(createDump(logger)), subDir, filePrefix)
    }

    private fun stringify(logger: InstanceLogger): String {
        return stringify(createDump(logger))
    }

    private fun stringify(dump: Map<String, TypeLogs>): String {
        return json.encodeToString(dump)
    }

    private fun createDump(tracer: InstanceLogger): Map<String, TypeLogs> {

        val log = tracer.getLog()

        return log.map {
            val fqn = it.key
            val logs = it.value
            TypeLogs(fqn, logs.map { l -> Log(l.methodName, l.params, l.returnValue, l.callStart, l.callEnd) })
        }.associateBy { it.fqn }
    }

    @Serializable
    data class Log(
        val name: String,
        val parameters: Map<String, String>,
        val returnValue: String?,
        val start: Long, val end: Long
    )

    @Serializable
    data class TypeLogs(
        val fqn: String, val logs: List<Log>
    )
}
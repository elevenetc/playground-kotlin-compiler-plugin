package org.jetbrains.kotlin

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CallLogger {

    val calls = mutableMapOf<Uuid, Call>()
    var currentCall: Call? = null
    val history = mutableListOf<Call>()
    var enableBump = false

    fun start(callFqn: String): Uuid {
        val call = Call(Uuid.random(), callFqn, System.currentTimeMillis(), -1, Thread.currentThread().name)
        calls[call.id] = call

        if (currentCall?.ended == false) {
            currentCall?.children?.add(call)
        } else {
            /**
             * Current call is ended.
             * TODO: create new tree? Current workaround is to attach to existing one
             */
            currentCall?.children?.add(call)
        }
        call.parent = currentCall
        updateCurrent(call)
        return call.id
    }

    fun end(id: Uuid) {
        calls[id] ?: error("Call $id does not exist")
        calls[id]?.end = System.currentTimeMillis()
        updateCurrent(currentCall?.parent)
        if (enableBump) LoggerDumper.instance.dump(this)
    }

    fun dumpToString(): String {
        return LoggerDumper.instance.dumpToString(this)
    }

    private fun updateCurrent(call: Call?) {
        if (call != null) history.add(call)
        currentCall = call
    }

    data class Call(
        val id: Uuid,
        val fqn: String,
        val start: Long,
        var end: Long,
        val threadName: String
    ) {
        var parent: Call? = null
        val children = mutableListOf<Call>()
    }

    val Call.ended: Boolean
        get() = this.end != -1L

    companion object {
        @JvmStatic
        val instance = CallLogger()
    }
}
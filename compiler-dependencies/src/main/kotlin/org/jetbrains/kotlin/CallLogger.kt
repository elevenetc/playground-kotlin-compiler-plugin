package org.jetbrains.kotlin

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CallLogger {

    val threads = LinkedHashMap<Long, ThreadContainer>()
    val history = mutableListOf<Call>()
    var enableDump = false

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            storeDump(dump(), "traces", "trace")
        })
    }

    fun start(callFqn: String): Uuid {
        val call = newCall(callFqn)

        val t = getCurrentThreadContainer()

        if (t.root == null) {
            t.root = call
        }

        t.calls[call.id] = call

        if (t.currentCall?.ended == false) {
            t.currentCall?.children?.add(call)
        } else {
            /**
             * Current call is ended.
             * TODO: create new tree? Current workaround is to attach to existing one
             */
            t.currentCall?.children?.add(call)
        }
        call.parent = t.currentCall
        updateCurrent(call)
        return call.id
    }

    fun end(id: Uuid) {
        val t = getCurrentThreadContainer()
        t.calls[id] ?: error("Call $id does not exist")
        t.calls[id]?.end = System.currentTimeMillis()
        updateCurrent(t.currentCall?.parent)
        if (enableDump) LoggerDumper.instance.dump(this)
    }

    fun dump(): String {
        return LoggerDumper.instance.dumpString(this)
    }

    private fun getCurrentThreadContainer(): ThreadContainer {
        val currentThread = Thread.currentThread()
        val id = currentThread.id
        if (!threads.containsKey(id)) {
            threads[id] = ThreadContainer(ThreadId(id, currentThread.name))
        }
        return threads[id] ?: error("Thread container with id=$id is not found")
    }

    private fun updateCurrent(call: Call?) {
        if (call != null) {
            val t = getCurrentThreadContainer()
            history.add(call)
            t.currentCall = call
        }
    }

    private fun newCall(callFqn: String) =
        Call(
            id = Uuid.random(),
            fqn = callFqn,
            start = System.currentTimeMillis(),
            end = -1,
            thread = ThreadId(Thread.currentThread().id, Thread.currentThread().name)
        )

    data class Call(
        val id: Uuid,
        val fqn: String,
        val start: Long,
        var end: Long,
        val thread: ThreadId
    ) {
        var parent: Call? = null
        val children = mutableListOf<Call>()
    }

    data class ThreadId(val id: Long, val name: String)

    data class ThreadContainer(
        val id: ThreadId
    ) {
        var root: Call? = null
        val calls = mutableMapOf<Uuid, Call>()
        var currentCall: Call? = null
    }

    private val Call.ended: Boolean
        get() = this.end != -1L

    companion object {
        @JvmStatic
        val instance = CallLogger()
    }
}
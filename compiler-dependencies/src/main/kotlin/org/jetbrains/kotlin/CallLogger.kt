package org.jetbrains.kotlin

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class CallLogger {

    val threads = LinkedHashMap<Long, ThreadContainer>() // thread-id -> thread container
    val stacks = LinkedHashMap<Long, MutableList<StackInstruction>>() // thread-id -> stack instructions
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
        updateCurrentCall(call)
        addPushStackInstruction(call)
        return call.id
    }

    fun end(id: Uuid) {
        val t = getCurrentThreadContainer()
        t.calls[id] ?: error("Call $id does not exist")
        t.calls[id]?.end = System.nanoTime()
        updateCurrentCall(t.currentCall?.parent)
        addPopStackInstruction()
        if (enableDump) TraceDumper.instance.dump(this)
    }

    fun dump(): String {
        return TraceDumper.instance.dumpString(this)
    }

    private fun getCurrentThreadContainer(): ThreadContainer {
        val currentThread = Thread.currentThread()
        val id = currentThread.id
        if (!threads.containsKey(id)) {
            threads[id] = ThreadContainer(ThreadId(id, currentThread.name))
        }
        return threads[id] ?: error("Thread container with id=$id is not found")
    }

    private fun updateCurrentCall(call: Call?) {
        if (call != null) {
            val t = getCurrentThreadContainer()
            t.currentCall = call
        }
    }

    private fun addPushStackInstruction(call: Call) {
        val t = getCurrentThreadContainer()
        val stack = stacks.getOrPut(t.id.id) { mutableListOf() }
        stack.add(
            StackInstruction.Push(
                call.id.toJavaUuid().toString(),
                System.nanoTime(),
                Uuid.random().toJavaUuid().toString()
            )
        )
    }

    private fun addPopStackInstruction() {
        val t = getCurrentThreadContainer()
        val stack = stacks.getOrPut(t.id.id) { mutableListOf() }
        stack.add(
            StackInstruction.Pop(
                System.nanoTime(),
                Uuid.random().toJavaUuid().toString()
            )
        )
    }

    private fun newCall(callFqn: String) =
        Call(
            id = Uuid.random(),
            fqn = callFqn,
            start = System.nanoTime(),
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

sealed class StackInstruction(val id: String, val start: Long) {
    class Push(val nodeId: String, start: Long, id: String) : StackInstruction(id, start)
    class Pop(start: Long, id: String) : StackInstruction(id, start)
}
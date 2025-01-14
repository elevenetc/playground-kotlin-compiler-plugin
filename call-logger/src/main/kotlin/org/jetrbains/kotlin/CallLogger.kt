package org.jetrbains.kotlin

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CallLogger {

    val calls = mutableMapOf<Uuid, Call>()
    var currentCall: Call? = null

    fun start(callFqn: String): Uuid {
        val call = Call(Uuid.random(), callFqn, System.currentTimeMillis(), -1)
        calls[call.id] = call

        if (currentCall?.ended == false) {
            currentCall?.children?.add(call)
        }
        call.parent = currentCall
        currentCall = call
        return call.id
    }

    fun end(id: Uuid) {
        calls[id] ?: error("Call $id does not exist")
        calls[id]?.end = System.currentTimeMillis()
        currentCall = currentCall?.parent
    }

    data class Call(
        val id: Uuid,
        val fqn: String,
        val start: Long,
        var end: Long
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
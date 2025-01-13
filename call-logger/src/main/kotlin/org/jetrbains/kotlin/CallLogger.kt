package org.jetrbains.kotlin

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CallLogger {

    val calls = mutableMapOf<Uuid, Call>()

    fun start(callFqn: String): Uuid {
        val call = Call(Uuid.random(), callFqn, System.currentTimeMillis(), -1)
        calls[call.id] = call
        return call.id
    }

    fun end(id: Uuid) {
        val call = calls[id] ?: error("Call $id does not exist")
        calls[id] = call.copy(end = System.currentTimeMillis())
    }

    data class Call(
        val id: Uuid,
        val fqn: String,
        val start: Long,
        val end: Long
    )

    companion object {
        @JvmStatic
        val instance = CallLogger()
    }
}
@file:OptIn(ExperimentalUuidApi::class)

package org.jetbrains.kotlin

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.DumpLog.StackInstruction
import org.jetbrains.kotlin.StackInstruction.Pop
import org.jetbrains.kotlin.StackInstruction.Push
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class TraceDumper {

    private val json = Json {
        prettyPrint = true
    }

    fun dump(logger: CallLogger) {
        storeDump(createDump(logger))
    }

    fun dumpString(logger: CallLogger): String {
        val dump = createDump(logger)
        return json.encodeToString(dump)
    }

    private fun storeDump(dump: DumpLog) {
        val dir = File(filePath).also { if (!it.exists()) it.mkdirs() }
        val file = File(dir, fileName)
        file.writeText(json.encodeToString(dump))
    }

    private fun createDump(logger: CallLogger): DumpLog {

        val stacks = logger.stacks.mapNotNull {
            val instructions = it.value
            val threadId = it.key
            if (instructions.isEmpty()) {
                null
            } else {
                threadId to instructions.map { inst ->
                    when (inst) {
                        is Push -> StackInstruction(inst.id, inst.start, inst.nodeId, "push")
                        is Pop -> StackInstruction(inst.id, inst.start, null, "pop")
                    }
                }
            }
        }.toMap()

        val roots = logger.threads.map {
            val threadContainer = it.value
            threadContainer.root?.toTreeNode() ?: error("Thread container doesn't have root")
        }.associateBy { it.threadId }

        return DumpLog(
            timestamp = System.currentTimeMillis(),
            stacks = stacks,
            roots = roots,
        )
    }

    companion object {
        @JvmStatic
        val instance = TraceDumper()
    }
}

private fun CallLogger.Call.toTreeNode(): DumpLog.TreeNode {
    val children = this.children.map {
        it.toTreeNode()
    }
    return DumpLog.TreeNode(this.id.toJavaUuid().toString(), this.start, this.end, this.fqn, this.thread.id, children)
}

private const val filePath = "build/generated/trace-dump"
private const val fileName = "trace.json"

@Serializable
data class DumpLog(
    val timestamp: Long,
    val stacks: Map<Long, List<StackInstruction>>,
    val roots: Map<Long, TreeNode>
) {

    @Serializable
    val version = 1

    @Serializable
    data class StackInstruction(
        val id: String,
        val start: Long,
        val nodeId: String?,
        val type: String
    )

    @Serializable
    data class TreeNode(
        val id: String, val start: Long, val end: Long, val fqn: String,
        val threadId: Long,
        val children: List<TreeNode>
    )
}
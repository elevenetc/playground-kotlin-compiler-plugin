@file:OptIn(ExperimentalUuidApi::class)

package org.jetbrains.kotlin

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.uuid.ExperimentalUuidApi

class LoggerDumper {

    private val json = Json {
        prettyPrint = true
    }

    fun dump(logger: CallLogger) {
        storeDump(createDump(logger))
    }

    fun dumpToString(logger: CallLogger): String {
        val dump = createDump(logger)
        return json.encodeToString(dump)
    }

    private fun storeDump(dump: DumpLog) {
        val dir = File(filePath).also { if (!it.exists()) it.mkdirs() }
        val file = File(dir, fileName)
        file.writeText(json.encodeToString(dump))
    }

    private fun createDump(logger: CallLogger): DumpLog {

        val history =
            logger.history.map { DumpLog.HistoryItem(it.id.toString(), it.start, it.end, it.thread.name, it.fqn) }

        val roots = logger.threads.map {
            val threadContainer = it.value
            threadContainer.root?.toTreeNode() ?: error("Thread container doesn't have root")
        }.associateBy { it.threadId }

        val threadsNames = logger.threads.map {
            it.key to it.value.id.name
        }.toMap()

        return DumpLog(
            timestamp = System.currentTimeMillis(),
            history = history,
            threadsRoots = roots,
            threadsNamesMap = threadsNames
        )
    }

    companion object {
        @JvmStatic
        val instance = LoggerDumper()
    }
}

private fun CallLogger.Call.toTreeNode(): DumpLog.TreeNode {
    val children = this.children.map {
        it.toTreeNode()
    }
    return DumpLog.TreeNode(this.id.toString(), this.start, this.end, this.fqn, this.thread.id, children)
}

private const val filePath = "build/generated/logger-dump"
private const val fileName = "log.json"

@Serializable
data class DumpLog(
    val timestamp: Long,
    val history: List<HistoryItem>,
    val threadsRoots: Map<Long, TreeNode>,
    val threadsNamesMap: Map<Long, String>
) {

    @Serializable
    data class HistoryItem(
        val id: String, val start: Long, val end: Long,
        val threadName: String,
        val fqn: String
    )

    @Serializable
    data class TreeNode(
        val id: String, val start: Long, val end: Long, val fqn: String,
        val threadId: Long,
        val children: List<TreeNode>
    )
}
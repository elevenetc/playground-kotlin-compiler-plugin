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

        val root = logger.history.first()
        val log = DumpLog(
            System.currentTimeMillis(),
            logger.history.map { DumpLog.HistoryItem(it.id.toString(), it.start, it.end, it.threadName, it.fqn) },
            root.toTreeNode()
        )
        return log
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
    return DumpLog.TreeNode(this.id.toString(), this.start, this.end, this.fqn, this.threadName, children)
}

private const val filePath = "build/generated/logger-dump"
private const val fileName = "log.json"

@Serializable
data class DumpLog(
    val timestamp: Long,
    val history: List<HistoryItem>,
    val treeRoot: TreeNode
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
        val threadName: String,
        val children: List<TreeNode>
    )
}
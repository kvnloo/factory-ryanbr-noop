package com.noop.agent

import org.json.JSONObject
import java.io.File

/**
 * On-phone audit trail of every agent request, refusal and grant change. Bounded to [MAX_LINES].
 * Entries record WHAT was asked and the outcome, never the values returned or the text of a note.
 */
class AgentAudit(private val file: File, private val nowMs: () -> Long = System::currentTimeMillis) {
    @Synchronized
    fun record(kind: String, tool: String? = null, outcome: String, detail: Map<String, Any?> = emptyMap()) {
        val entry = JSONObject().put("ts", nowMs()).put("kind", kind).put("outcome", outcome)
        if (tool != null) entry.put("tool", tool)
        detail.forEach { (k, v) -> entry.put(k, v ?: JSONObject.NULL) }
        val lines = (if (file.exists()) file.readLines() else emptyList()) + entry.toString()
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(lines.takeLast(MAX_LINES).joinToString("\n", postfix = "\n"))
        tmp.setReadable(false, false); tmp.setReadable(true, true)
        check(tmp.renameTo(file))
    }

    @Synchronized
    fun recent(limit: Int = 50): List<JSONObject> =
        if (!file.exists()) emptyList()
        else file.readLines().filter { it.isNotBlank() }.takeLast(limit).mapNotNull { runCatching { JSONObject(it) }.getOrNull() }.reversed()

    @Synchronized
    fun clear() { file.delete() }

    companion object {
        const val MAX_LINES = 300
    }
}

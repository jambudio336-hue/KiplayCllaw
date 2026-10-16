package com.k1play.agentpro.tools

import com.k1play.agentpro.memory.SemanticMemory
import kotlinx.serialization.json.*
import javax.inject.Inject

class MemoryManageTool @Inject constructor(
    private val semanticMemory: SemanticMemory
) : Tool {
    override val name = "memory.manage"
    override val description = "Manage persistent on-device memory across chats. Use remember only for stable facts the user explicitly wants saved, search to recall facts, and forget to remove a fact. Save concise, non-sensitive information only. Memory changes require user approval."
    override val parameters = ToolParameters(
        properties = mapOf(
            "action" to ParameterProperty("string", "Operation", enum = listOf("remember", "search", "forget")),
            "key" to ParameterProperty("string", "Short fact name"),
            "value" to ParameterProperty("string", "Fact content"),
            "category" to ParameterProperty("string", "Fact category"),
            "query" to ParameterProperty("string", "Search phrase"),
            "id" to ParameterProperty("integer", "Fact ID to remove")
        ),
        required = listOf("action")
    )
    override val requiresApproval = true

    override suspend fun execute(params: JsonObject): ToolResult {
        val action = params["action"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult.error("Aksi wajib diisi.")
        return when (action) {
            "remember" -> {
                val key = params["key"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                val value = params["value"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                val category = params["category"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty().ifBlank { "general" }
                if (key.isBlank() || value.isBlank()) return ToolResult.error("Nama dan isi fakta wajib diisi.")
                if (key.length > 100 || value.length > 500 || category.length > 60) return ToolResult.error("Teks terlalu panjang.")
                semanticMemory.remember(key, value, category)
                ToolResult.success(buildJsonObject {
                    put("saved", true)
                    put("key", key)
                    put("category", category)
                    put("message", "Fakta tersimpan di memori lokal perangkat.")
                })
            }
            "search" -> {
                val query = params["query"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                if (query.isBlank()) return ToolResult.error("Kata pencarian wajib diisi.")
                val matches = semanticMemory.search(query).take(20)
                ToolResult.success(buildJsonObject {
                    put("count", matches.size)
                    put("facts", buildJsonArray {
                        matches.forEach { fact ->
                            add(buildJsonObject {
                                put("id", fact.id)
                                put("key", fact.key)
                                put("value", fact.value)
                                put("category", fact.category)
                            })
                        }
                    })
                })
            }
            "forget" -> {
                val id = params["id"]?.jsonPrimitive?.longOrNull
                    ?: return ToolResult.error("ID fakta wajib diisi.")
                semanticMemory.forget(id)
                ToolResult.success(buildJsonObject { put("deleted", true); put("id", id) })
            }
            else -> ToolResult.error("Gunakan aksi remember, search, atau forget.")
        }
    }
}

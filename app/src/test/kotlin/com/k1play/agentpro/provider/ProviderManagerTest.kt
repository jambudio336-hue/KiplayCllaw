package com.k1play.agentpro.provider

import org.junit.Assert.*
import org.junit.Test

class ProviderManagerTest {

    @Test
    fun `all three providers implement Provider`() {
        val anthropic: Provider = AnthropicProvider()
        val openai: Provider = OpenAIProvider()
        val gemini: Provider = GeminiProvider()

        assertEquals("anthropic", anthropic.name)
        assertEquals("openai", openai.name)
        assertEquals("gemini", gemini.name)
    }

    @Test
    fun `anthropic defaults`() {
        assertEquals(AnthropicProvider.DEFAULT_MODEL, "claude-sonnet-4-6")
        assertTrue(AnthropicProvider.API_URL.contains("anthropic.com"))
    }

    @Test
    fun `openai defaults`() {
        assertEquals(OpenAIProvider.DEFAULT_MODEL, "gpt-5.2")
        assertTrue(OpenAIProvider.API_URL.contains("openai.com"))
    }

    @Test
    fun `gemini defaults`() {
        assertEquals(GeminiProvider.DEFAULT_MODEL, "gemini-3-flash-preview")
        assertTrue(GeminiProvider.API_URL.contains("googleapis.com"))
    }

    @Test
    fun `provider info data class`() {
        val info = ProviderInfo("anthropic", "Anthropic (Claude)", "claude-sonnet-4-6", true)
        assertEquals("anthropic", info.type)
        assertEquals("Anthropic (Claude)", info.displayName)
        assertTrue(info.hasKey)
    }

    @Test
    fun `provider info without key`() {
        val info = ProviderInfo("openai", "OpenAI (GPT)", "gpt-5.2", false)
        assertFalse(info.hasKey)
    }
}

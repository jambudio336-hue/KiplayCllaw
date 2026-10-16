package com.k1play.agentpro.provider

import com.k1play.agentpro.config.AppConfig
import com.k1play.agentpro.config.SecureKeyStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages available AI providers and handles provider switching.
 * Reads the active provider from AppConfig and configures it with
 * the encrypted API key from SecureKeyStore.
 */
@Singleton
class ProviderManager @Inject constructor(
    private val appConfig: AppConfig,
    private val secureKeyStore: SecureKeyStore,
    private val anthropicProvider: AnthropicProvider,
    private val openAIProvider: OpenAIProvider,
    private val geminiProvider: GeminiProvider,
    private val openRouterProvider: OpenRouterProvider
) {
    private val providers = mapOf(
        "anthropic" to anthropicProvider,
        "openai" to openAIProvider,
        "gemini" to geminiProvider,
        "openrouter" to openRouterProvider
    )

    /** Get the currently active provider, configured with its API key */
    fun activeProvider(): Provider {
        val type = appConfig.providerType
        val provider = providers[type] ?: anthropicProvider
        configureProvider(type, provider)
        return provider
    }

    /** Switch to a different provider */
    fun switchProvider(type: String) {
        if (type in providers) {
            appConfig.providerType = type
        }
    }

    /** Get the active provider type name */
    fun activeType(): String = appConfig.providerType

    /** Get all available provider types */
    fun availableProviders(): List<ProviderInfo> = listOf(
        ProviderInfo("anthropic", "Anthropic (Claude)", "claude-sonnet-4-6",
            secureKeyStore.hasApiKey("anthropic"),
            models = listOf("claude-sonnet-4-6", "claude-opus-4-6", "claude-haiku-4-5")),
        ProviderInfo("openai", "OpenAI (GPT)", "gpt-5.2",
            secureKeyStore.hasApiKey("openai"),
            models = listOf("gpt-5.2", "gpt-5.2-chat-latest", "gpt-5-mini", "gpt-4.1", "gpt-4.1-mini")),
        ProviderInfo("gemini", "Google (Gemini)", "gemini-3-flash-preview",
            secureKeyStore.hasApiKey("gemini"),
            models = listOf("gemini-3.1-pro-preview", "gemini-3-flash-preview", "gemini-2.5-flash", "gemini-2.5-pro")),
        ProviderInfo("openrouter", "OpenRouter", "openrouter/free",
            secureKeyStore.hasApiKey("openrouter"),
            models = listOf("openrouter/free"))
    )

    /** Check if a provider has an API key configured */
    fun hasKey(type: String): Boolean = secureKeyStore.hasApiKey(type)

    /** Store an API key for a provider */
    fun setApiKey(type: String, apiKey: String) {
        secureKeyStore.storeApiKey(type, apiKey)
    }

    /** Remove an API key for a provider */
    fun removeApiKey(type: String) {
        secureKeyStore.deleteApiKey(type)
    }

    private fun configureProvider(type: String, provider: Provider) {
        val apiKey = secureKeyStore.getApiKey(type) ?: return
        var model = appConfig.model
        // Older builds could leave a Claude-only model ID in the shared model setting
        // when switching providers during setup. OpenRouter requires its own model slug.
        if (type == "openrouter" && (model == "claude-sonnet-4-6" || '/' !in model)) {
            model = "openrouter/free"
            appConfig.model = model
        }
        when (provider) {
            is AnthropicProvider -> provider.configure(apiKey, model)
            is OpenAIProvider -> provider.configure(apiKey, model)
            is GeminiProvider -> provider.configure(apiKey, model)
            is OpenRouterProvider -> provider.configure(apiKey, model)
        }
    }
}

data class ProviderInfo(
    val type: String,
    val displayName: String,
    val defaultModel: String,
    val hasKey: Boolean,
    val models: List<String> = emptyList()
)

package kr.kro.tripsketch.config

import io.github.cdimascio.dotenv.Dotenv

object EnvLoader {
    private val dotenv: Dotenv by lazy {
        try {
            Dotenv.configure().directory(".").ignoreIfMissing().load()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load .env file", e)
        }
    }

    fun getProperty(key: String): String? {
        return dotenv[key]
    }

    fun getPropertyOrDefault(key: String, defaultValue: String): String {
        return dotenv[key] ?: defaultValue
    }

    fun getAllProperties(): Map<String, String> {
        return dotenv.entries().associateBy({ it.key }, { it.value })
    }
}

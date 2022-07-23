package tech.harmonysoft.oss.common.info

/**
 * Facades all available [CommonInfoProvider] implementations.
 */
interface CommonInfoRegistry {

    val info: Map<String, String>
}
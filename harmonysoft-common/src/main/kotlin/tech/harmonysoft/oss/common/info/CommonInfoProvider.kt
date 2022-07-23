package tech.harmonysoft.oss.common.info

/**
 * Defines an interface for exposing common application info
 */
interface CommonInfoProvider {

    val info: Map<String, String>
}
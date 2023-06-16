package tech.harmonysoft.oss.common.environment

object EnvironmentUtil {

    val APPLE_SILICON = System.getProperty("os.name") == "Mac OS X"
                        && System.getProperty("os.arch") == "aarch64"

}
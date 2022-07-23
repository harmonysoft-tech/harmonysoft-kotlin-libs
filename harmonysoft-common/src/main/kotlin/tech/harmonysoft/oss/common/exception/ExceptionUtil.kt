package tech.harmonysoft.oss.common.exception

import java.io.PrintWriter
import java.io.StringWriter

object ExceptionUtil {

    fun exceptionToString(e: Throwable): String {
        return StringWriter().apply {
            use {
                PrintWriter(this).use { pw ->
                    e.printStackTrace(pw)
                }
            }
        }.toString()
    }
}
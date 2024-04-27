package tech.harmonysoft.oss.common.cli

import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

@Named
class CommandLineHelper {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val shell: List<String> =
        run {
            val os = System.getProperty("os.name")
            if (os.lowercase().contains("windows")) {
                listOf("cmd", "/C")
            } else {
                listOf("bash", "-c")
            }
        }

    fun execute(
        commandLine: String,
        commandDescription: String,
        environmentVariables: Map<String, String> = emptyMap(),
    ): String {
        return execute(
            commandLine = commandLine,
            description = commandDescription,
            keepProcessRunning = false,
            environmentVariables = environmentVariables,
        ).output ?: throw IllegalStateException(
            "can not get output from executing '$commandDescription': $commandLine"
        )
    }

    fun start(
        commandLine: String,
        processDescription: String,
        environmentVariables: Map<String, String> = emptyMap(),
    ): Process {
        return execute(
            commandLine = commandLine,
            description = processDescription,
            keepProcessRunning = true,
            environmentVariables = environmentVariables,
        ).process
    }

    private fun execute(
        commandLine: String,
        description: String,
        keepProcessRunning: Boolean,
        environmentVariables: Map<String, String>,
    ): ProcessInfo {
        val cmdLine = (shell + commandLine)
        val processBuilder = ProcessBuilder().command(cmdLine)
        processBuilder.environment() += environmentVariables
        return try {
            logger.info(
                "start '{}' using command line '{}' and environment {}",
                description,
                cmdLine.joinToString(" "),
                environmentVariables,
            )
            val process = processBuilder.start()
            if (keepProcessRunning) {
                thread(isDaemon = true) {
                    process.inputReader().lineSequence().forEach {
                        logger.info("sub-process {} [STDOUT] - {}", description, it)
                    }
                }
                thread(isDaemon = true) {
                    process.errorReader().lineSequence().forEach {
                        logger.info("sub-process {} [STDERR] - {}", description, it)
                    }
                }
                ProcessInfo(process, null)
            } else {
                val stdout = process.inputReader().readText()
                val stderr = process.errorReader().readText()
                logger.info(
                    "executed '{}' using command line '{}'\nstdout:\n{}\nstderr:\n{}",
                    description,
                    commandLine,
                    stdout.takeIf { it.isNotBlank() } ?: "<empty>",
                    stderr.takeIf { it.isNotBlank() } ?: "<empty>",
                )
                val output = stdout.takeIf { it.isNotBlank() } ?: stderr
                ProcessInfo(process, output)
            }
        } catch (e: Exception) {
            logger.error(
                "unexpected exception on attempt to {} using command line '{}'",
                description,
                cmdLine.joinToString(" "),
                e,
            )
            throw IllegalStateException("can't $description", e)
        }
    }

    private data class ProcessInfo(
        val process: Process,
        val output: String?,
    )
}
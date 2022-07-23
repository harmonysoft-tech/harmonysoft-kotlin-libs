package tech.harmonysoft.oss.common.backup.file.impl

import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.backup.file.FileSystemBackupService
import tech.harmonysoft.oss.common.time.clock.ClockProvider
import tech.harmonysoft.oss.common.time.util.DateTimeHelper
import java.io.File
import java.io.FileFilter
import java.time.LocalDate
import java.time.LocalTime
import java.util.Stack
import javax.inject.Named

@Named
class FileSystemBackupServiceImpl(
    private val clockProvider: ClockProvider,
    private val dateTimeHelper: DateTimeHelper
) : FileSystemBackupService {

    private val logger = LoggerFactory.getLogger(FileSystemBackupServiceImpl::class.java)

    override fun prepareNewBackupDir(rootBackupDir: File): File {
        if (rootBackupDir.isFile) {
            throw IllegalArgumentException(
                "failed to prepare new backup dir - expected ${rootBackupDir.canonicalPath} to be a backup directory "
                + "but it points to a file"
            )
        }

        val clock = clockProvider.data

        val dateDir = File(rootBackupDir, dateTimeHelper.formatDate(LocalDate.now(clock)))
        if (dateDir.isFile) {
            throw IllegalStateException(
                "failed to prepare new backup dir - expected ${dateDir.canonicalPath} to be a backup directory "
                + "but it points to a file"
            )
        }

        val timeDir = File(dateDir, dateTimeHelper.formatTime(LocalTime.now(clock)))
        if (timeDir.isFile) {
            throw IllegalStateException(
                "failed to prepare new backup dir - expected ${timeDir.canonicalPath} to be a backup directory "
                + "but it points to a file"
            )
        }

        return if (timeDir.isDirectory) {
            timeDir
        } else {
            val created = timeDir.mkdirs()
            if (created) {
                timeDir
            } else {
                throw IllegalStateException(
                    "failed to prepare a backup dir at path ${timeDir.canonicalPath} - it doesn't exist and we "
                    + "failed to create it"
                )
            }
        }
    }

    override fun getLastAvailableBackupDir(rootBackupDir: File, ttlInDays: Int): File? {
        if (!rootBackupDir.isDirectory) {
            return null
        }

        return findMostRecentDirectory(rootBackupDir) {
            dateTimeHelper.parseDate(it, DateTimeHelper.DATE_PATTERN)
        }?.let { dateDirectory ->
            findMostRecentDirectory(dateDirectory) { timeDirectory ->
                dateTimeHelper.parseTime(timeDirectory, DateTimeHelper.TIME_PATTERN)
            }
        }
    }

    private fun <T : Comparable<T>> findMostRecentDirectory(rootDir: File, childDirNameParser: (String) -> T): File? {
        return rootDir.listFiles { file -> file?.isDirectory ?: false }?.mapNotNull {
            try {
                childDirNameParser(it.name) to it
            } catch (e: Exception) {
                logger.warn("Unexpected directory is found under backup dir {}", rootDir.canonicalPath, e)
                null
            }
        }?.maxByOrNull { it.first }?.second
    }

    override fun backup(backupDir: File, toBackup: File, toBackupFilter: (File) -> Boolean) {
        if (!backupDir.isDirectory) {
            val created = backupDir.mkdirs()
            if (!created) {
                logger.warn("Failed to backup files from '{}' to '{}' - can't create the output dir",
                            toBackup.canonicalPath, backupDir.canonicalPath)
                return
            }
        }

        val toProcess = Stack<File>().apply { push(toBackup) }
        while (toProcess.isNotEmpty()) {
            val file = toProcess.pop()
            if (file.isFile) {
                if (toBackupFilter(file)) {
                    val destination = File(backupDir, file.name)
                    if (!destination.parentFile.exists()) {
                        val created = destination.parentFile.mkdirs()
                        if (!created) {
                            logger.warn("Failed to backup files from '{}' can't create the output dir {}",
                                        toBackup.canonicalPath, backupDir.canonicalPath)
                            return
                        }
                    }
                    file.copyTo(destination, overwrite = true)
                    logger.info("Backed up file {} as {}", file.canonicalPath, destination.canonicalPath)
                }
            } else if (file.isDirectory && file != backupDir) {
                file.listFiles()?.forEach {
                    toProcess.push(it)
                }
            }
        }
    }

    override fun cleanOutdated(rootDir: File, ttlDays: Int) {
        if (!rootDir.isDirectory) {
            logger.info("Skip cleaning backup as target dir doesn't exist ({})", rootDir.canonicalPath)
            return
        }

        val firstLiveData = LocalDate.now(clockProvider.data).minusDays(ttlDays.toLong())
        rootDir.listFiles(FileFilter {
            it.isDirectory
        })?.forEach { dir ->
            try {
                dateTimeHelper.parseDate(dir.name)
            } catch (e: Exception) {
                logger.warn(
                    "Unexpected directory named '{}' is found in {} backup dir. It doesn't conform to pattern '{}'. "
                    + "Keeping it as-is, take care of it manually if necessary",
                    dir.name, rootDir.canonicalPath, DateTimeHelper.DATE_PATTERN
                )
                null
            }?.takeIf {
                it.isBefore(firstLiveData)
            }?.let {
                val success = dir.deleteRecursively()
                if (success) {
                    logger.info("Cleaned outdated backup directory '{}'", dir.canonicalPath)
                } else {
                    logger.warn("Failed to clean up an outdated directory '{}'. Please take care of it manually",
                                dir.canonicalPath)
                }
            }
        }
    }
}
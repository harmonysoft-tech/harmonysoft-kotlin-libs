package tech.harmonysoft.oss.common.backup.file

import java.io.File

/**
 * Defines contracts for managing file system-based backup directories. E.g. we might download some input,
 * parse it and process. We'd like to keep the input for some time, say, a week to be able tracing back
 * problems with it if any.
 */
interface FileSystemBackupService {

    /**
     * @return  backup dir for the given base dir. It's up to underlying implementation to define how backups
     *          should be organised internally, the only requirement is that when [cleanOutdated] is called,
     *          it should remove all backups which are older than the given ttl
     */
    fun prepareNewBackupDir(rootBackupDir: File): File

    /**
     * Multiple backup folders might be created by [prepareNewBackupDir]. This method tries to find the most
     * recent one created within given ttl.
     */
    fun getLastAvailableBackupDir(rootBackupDir: File, ttlInDays: Int): File?

    /**
     * @param backupDir         backup directory to store the data
     * @param toBackup          file or root directory to back up at the given backup dir
     * @param toBackupFilter    when this method is asked to back up a directory, we might want to filter
     *                          the input and store only subset of its files. This parameter allows to do that
     */
    fun backup(backupDir: File, toBackup: File, toBackupFilter: (File) -> Boolean = { true })

    fun cleanOutdated(rootDir: File, ttlDays: Int)
}
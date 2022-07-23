package tech.harmonysoft.oss.common.schedule

interface TaskScheduler {

    /**
     * Schedules given tasks for the give callback
     *
     * @param tasks     new schedule to apply. All previously scheduled tasks which are not in the provided here
     *                  are automatically cancelled
     * @param callback  callback to call when it's time to execute the task
     */
    fun schedule(tasks: Collection<ScheduledTask>, callback: Callback)

    fun interface Callback {

        fun onTriggered(taskId: String)
    }
}
package tech.harmonysoft.oss.common.schedule

interface TaskSchedulerFactory {

    fun newScheduler(schedulerId: String): TaskScheduler
}
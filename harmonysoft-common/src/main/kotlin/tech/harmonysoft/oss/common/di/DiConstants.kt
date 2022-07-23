package tech.harmonysoft.oss.common.di

import javax.annotation.Priority

object DiConstants {

    /**
     * We want to use DI framework-agnostic setup, that's why we use [Priority] instead of spring's
     * `@Primary` annotation. E.g. library test implementation of particular service should have more
     * priority than production implementation.
     *
     * However, we can't exclude a possibility that end application would like to use its own implementation
     * as primary. That's why we don't want to use [Int.MAX_VALUE] for library primary implementation.
     *
     * This constant defines priority to use for library primary implementations.
     */
    const val LIB_PRIMARY_PRIORITY = 10000
}
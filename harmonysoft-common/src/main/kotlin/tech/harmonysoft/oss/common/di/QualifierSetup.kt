package tech.harmonysoft.oss.common.di

import javax.inject.Qualifier

/**
 * Common meta-annotation for DI qualifier annotations. Normally it should be marked by [Qualifier] annotation
 * as well, but at least Spring doesn't pick it
 * from meta-annotation - `QualifierAnnotationAutowireCandidateResolver.isQualifier()`
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
annotation class QualifierSetup

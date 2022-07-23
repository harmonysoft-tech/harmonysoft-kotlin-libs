package tech.harmonysoft.oss.common.di

import org.springframework.core.annotation.AnnotationAwareOrderComparator
import javax.inject.Named

@Named
class DiElementsComparatorConfigProviderImpl : DiElementsComparatorConfigProvider {

    override fun getData(): Comparator<in Any> {
        return AnnotationAwareOrderComparator.INSTANCE
    }

    override fun refresh() {
    }

    override fun probe(): Comparator<in Any> {
        return data
    }
}
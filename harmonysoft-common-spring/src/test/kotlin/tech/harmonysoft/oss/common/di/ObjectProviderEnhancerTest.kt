package tech.harmonysoft.oss.common.di

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext

internal class ObjectProviderEnhancerTest {

    @Test
    fun `when ObjectProvider is used as a constructor dependency then custom cacheable implementation is used instead`() {
        val context = AnnotationConfigApplicationContext(MyConfiguration::class.java)
        val bean = context.getBean(RegularBean::class.java)

        assertThat(bean.publicProperty).isInstanceOf(CacheableObjectProvider::class.java)
        assertThat(bean.publicProperty.getObject()).isSameAs(context.getBean(Dependency1::class.java))

        assertThat(bean.privatePropertyExposed).isInstanceOf(CacheableObjectProvider::class.java)
        assertThat(bean.privatePropertyExposed.getObject()).isSameAs(context.getBean(Dependency2::class.java))

        assertThat(bean.constructorParameterExposed).isInstanceOf(CacheableObjectProvider::class.java)
        assertThat(bean.constructorParameterExposed.getObject()).isSameAs(context.getBean(Dependency3::class.java))
    }

    @Test
    fun `when @Primary is defined then it's respected`() {
        val context = AnnotationConfigApplicationContext(MyConfiguration::class.java)
        val bean = context.getBean(CommonInterfaceUserBean1::class.java)
        assertThat(bean.i.getObject()).isInstanceOf(CommonInterfaceImpl11::class.java)
    }

    @Test
    fun `when @Priority is defined then it's respected`() {
        val context = AnnotationConfigApplicationContext(MyConfiguration::class.java)
        val bean = context.getBean(CommonInterfaceUserBean2::class.java)
        assertThat(bean.i.getObject()).isInstanceOf(CommonInterfaceImpl21::class.java)
    }

    @Test
    fun `when Provider is used as a constructor dependency then custom cacheable implementation is used instead`() {
        val context = AnnotationConfigApplicationContext(MyConfiguration::class.java)
        val bean = context.getBean(ProviderBean::class.java)

        assertThat(bean.prop).isInstanceOf(CacheableProvider::class.java)
        assertThat(bean.prop.get()).isSameAs(context.getBean(Dependency1::class.java))
    }
}
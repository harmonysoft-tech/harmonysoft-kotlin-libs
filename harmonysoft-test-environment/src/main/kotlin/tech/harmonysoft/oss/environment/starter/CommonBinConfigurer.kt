package tech.harmonysoft.oss.environment.starter

object CommonBinConfigurer {
    fun configure() {
        // disable tomcat
        System.setProperty("spring.main.web-application-type", "none")
    }
}

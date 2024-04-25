package tech.harmonysoft.oss.environment.bin

object CommonBinConfigurer {
    fun configure() {
        // disable tomcat
        System.setProperty("spring.main.web-application-type", "none")
    }
}

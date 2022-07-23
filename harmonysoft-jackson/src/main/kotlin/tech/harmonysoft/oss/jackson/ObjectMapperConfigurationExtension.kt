package tech.harmonysoft.oss.jackson

import com.fasterxml.jackson.databind.ObjectMapper

interface ObjectMapperConfigurationExtension {

    fun configure(mapper: ObjectMapper)
}
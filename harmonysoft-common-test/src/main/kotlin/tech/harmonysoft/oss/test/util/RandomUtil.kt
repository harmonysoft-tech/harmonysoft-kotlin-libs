package tech.harmonysoft.oss.test.util

import java.util.concurrent.ThreadLocalRandom

object RandomUtil {

    private val characters = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun generateString(length: Int): String {
        return (1..length)
            .map {
                ThreadLocalRandom.current().nextInt(0, characters.size)
            }.map(characters::get)
            .joinToString("")
    }
}
plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-test"))

    api("redis.clients:jedis")
}
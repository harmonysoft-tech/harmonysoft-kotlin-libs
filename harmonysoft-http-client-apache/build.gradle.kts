plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-http-client"))

    api("org.apache.httpcomponents.client5:httpclient5:${Version.APACHE_HTTP_CLIENT}")
}
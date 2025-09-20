package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class GrupoDDapp2025S2Application

fun main(args: Array<String>) {
    val tzId = "America/Argentina/Buenos_Aires"
    System.setProperty("user.timezone", tzId)
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(tzId))

    println("JVM TZ = ${java.time.ZoneId.systemDefault()} | user.timezone=${System.getProperty("user.timezone")}")

    runApplication<GrupoDDapp2025S2Application>(*args)
}

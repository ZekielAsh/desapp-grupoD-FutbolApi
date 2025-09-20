package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GrupoDDapp2025S2Application

fun main(args: Array<String>) {
    // ⚙️ Forzar TZ correcta ANTES de crear el DataSource / conectar a Postgres
    val tzId = "America/Argentina/Buenos_Aires"
    System.setProperty("user.timezone", tzId)
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(tzId))

    // (Opcional) Log rápido para verificar qué está usando la JVM
    println("JVM TZ = ${java.time.ZoneId.systemDefault()} | user.timezone=${System.getProperty("user.timezone")}")

    runApplication<GrupoDDapp2025S2Application>(*args)
}

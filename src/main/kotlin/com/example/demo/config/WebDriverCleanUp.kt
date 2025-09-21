package com.example.demo.config

import org.openqa.selenium.WebDriver
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class WebDriverCleanup(private val webDriver: WebDriver) {

    @EventListener
    fun onApplicationShutdown(event: ContextClosedEvent) {
        webDriver.quit()
    }
}

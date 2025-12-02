package com.example.demo.aspect

import com.example.demo.model.ApiAuditLog
import com.example.demo.service.ApiAuditService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Aspect
@Component
class ControllerAuditAspect(
    private val apiAuditService: ApiAuditService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ControllerAuditAspect::class.java)
        private const val MAX_ERROR_MESSAGE_LENGTH = 1900
        private const val MAX_PARAMS_LENGTH = 900
        private val SENSITIVE_PATHS = listOf("/auth/login", "/auth/register", "/auth/")
        private const val REDACTED_MESSAGE = "[REDACTED - Sensitive Data]"
        private const val ANONYMOUS_USER = "anonymous"
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    }

    /**
     * Intercepts all public methods in classes annotated with @RestController or @Controller.
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    fun auditControllerMethods(joinPoint: ProceedingJoinPoint): Any? {

        // 1. Get timestamp
        val timestamp = LocalDateTime.now()
        val timestampFormatted = timestamp.format(DATE_TIME_FORMATTER)

        // 2. Get authenticated user
        val username = getCurrentUsername()

        // 3. Get HTTP request information
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val httpMethod = request.method
        val path = request.requestURI

        // 4. Get controller method information
        val controllerName = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name

        // 5. Sanitize parameters if the path is sensitive
        val params = if (isSensitivePath(path)) {
            REDACTED_MESSAGE
        } else {
            joinPoint.args.joinToString(separator = ", ", prefix = "[", postfix = "]")
                .take(MAX_PARAMS_LENGTH)
        }

        // Log inicio de la petición
        logger.info(">>> API Request | Timestamp: {} | User: {} | Method: {} | Path: {} | Controller: {}.{} | Params: {}",
            timestampFormatted, username, httpMethod, path, controllerName, methodName, params)

        val startTime = System.currentTimeMillis()
        var wasSuccess = true
        var errorMessage: String? = null
        var result: Any?

        try {
            // 6. Execute the original controller method
            result = joinPoint.proceed()

        } catch (e: Throwable) {
            // 7. Capture error if controller throws exception
            wasSuccess = false
            // Sanitize error messages for sensitive endpoints
            errorMessage = if (isSensitivePath(path)) {
                sanitizeErrorMessage(e.message ?: e.javaClass.simpleName)
            } else {
                (e.message ?: e.javaClass.simpleName).take(MAX_ERROR_MESSAGE_LENGTH)
            }

            logger.error("!!! API Error | Timestamp: {} | User: {} | Method: {} | Path: {} | Controller: {}.{} | Error: {}",
                timestampFormatted, username, httpMethod, path, controllerName, methodName, errorMessage, e)

            throw e // Re-throw for Spring to handle

        } finally {
            // 8. This block always executes (success or error)
            val duration = System.currentTimeMillis() - startTime
            val status = if (wasSuccess) "SUCCESS" else "FAILED"

            // Log fin de la petición
            logger.info("<<< API Response | Timestamp: {} | User: {} | Method: {} | Path: {} | Status: {} | Duration: {}ms",
                timestampFormatted, username, httpMethod, path, status, duration)

            // 9. Create audit log object
            val log = ApiAuditLog(
                username = username,
                httpMethod = httpMethod,
                path = path,
                controllerName = controllerName,
                methodName = methodName,
                params = params,
                executionTimeMs = duration,
                wasSuccess = wasSuccess,
                errorMessage = errorMessage
            )

            // 10. Save the log to database
            try {
                apiAuditService.logApiCall(log)
                logger.debug("Audit log saved to database for {} {}", httpMethod, path)
            } catch (e: Exception) {
                logger.error("Failed to save audit log to database", e)
            }
        }

        return result
    }

    /**
     * Gets the current authenticated username from Spring Security Context.
     * Returns "anonymous" if no authentication is present.
     */
    private fun getCurrentUsername(): String {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated && authentication.name != "anonymousUser") {
                authentication.name
            } else {
                ANONYMOUS_USER
            }
        } catch (e: Exception) {
            logger.warn("Failed to get authenticated user", e)
            ANONYMOUS_USER
        }
    }

    /**
     * Checks if the given path is considered sensitive and should not log parameters.
     */
    private fun isSensitivePath(path: String): Boolean {
        return SENSITIVE_PATHS.any { path.startsWith(it) }
    }

    /**
     * Sanitizes error messages for sensitive endpoints.
     */
    private fun sanitizeErrorMessage(message: String): String {
        // Para endpoints de autenticación, usar mensaje genérico
        val lowercaseMessage = message.lowercase()

        // Si el mensaje contiene información sensible, reemplazar con mensaje genérico
        if (lowercaseMessage.contains("credential") ||
            lowercaseMessage.contains("password") ||
            lowercaseMessage.contains("username") ||
            lowercaseMessage.contains("user:") ||
            lowercaseMessage.contains("invalid") ||
            lowercaseMessage.contains("not found") ||
            lowercaseMessage.contains("unauthorized") ||
            lowercaseMessage.contains("authentication") ||
            lowercaseMessage.contains("login")) {
            return "Authentication failed"
        }

        // Si no es un mensaje sensible, sanitizar passwords y tokens específicos
        return message
            .replace(Regex("password[=:].*?(?=,|\\]|\\}|$)", RegexOption.IGNORE_CASE), "password=[REDACTED]")
            .replace(Regex("token[=:].*?(?=,|\\]|\\}|$)", RegexOption.IGNORE_CASE), "token=[REDACTED]")
            .take(MAX_ERROR_MESSAGE_LENGTH)
    }
}
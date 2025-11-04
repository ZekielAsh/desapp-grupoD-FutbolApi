package com.example.demo.aspect

import com.example.demo.model.ApiAuditLog
import com.example.demo.service.ApiAuditService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class ControllerAuditAspect(
    private val apiAuditService: ApiAuditService
) {

    companion object {
        private const val MAX_ERROR_MESSAGE_LENGTH = 1900
        private const val MAX_PARAMS_LENGTH = 900
        private val SENSITIVE_PATHS = listOf("/auth/login", "/auth/register", "/auth/")
        private const val REDACTED_MESSAGE = "[REDACTED - Sensitive Data]"
    }

    /**
     * Intercepts all public methods in classes annotated with @RestController or @Controller.
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    fun auditControllerMethods(joinPoint: ProceedingJoinPoint): Any? {

        // 1. Get HTTP request information
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val httpMethod = request.method
        val path = request.requestURI

        // 2. Get controller method information
        val controllerName = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name

        // 3. Sanitize parameters if the path is sensitive
        val params = if (isSensitivePath(path)) {
            REDACTED_MESSAGE
        } else {
            joinPoint.args.joinToString(separator = ", ", prefix = "[", postfix = "]")
                .take(MAX_PARAMS_LENGTH)
        }

        println("Interceptando API call: $httpMethod $path ($controllerName.$methodName)")

        val startTime = System.currentTimeMillis()
        var wasSuccess = true
        var errorMessage: String? = null
        var result: Any?

        try {
            // 4. Execute the original controller method
            result = joinPoint.proceed()

        } catch (e: Throwable) {
            // 5. Capture error if controller throws exception
            wasSuccess = false
            // Sanitize error messages for sensitive endpoints
            errorMessage = if (isSensitivePath(path)) {
                sanitizeErrorMessage(e.message ?: e.javaClass.simpleName)
            } else {
                (e.message ?: e.javaClass.simpleName).take(MAX_ERROR_MESSAGE_LENGTH)
            }
            println("Error en $httpMethod $path: $errorMessage")
            throw e // Re-throw for Spring to handle

        } finally {
            // 6. This block always executes (success or error)
            val duration = System.currentTimeMillis() - startTime

            // 7. Create audit log object
            val log = ApiAuditLog(
                httpMethod = httpMethod,
                path = path,
                controllerName = controllerName,
                methodName = methodName,
                params = params,
                executionTimeMs = duration,
                wasSuccess = wasSuccess,
                errorMessage = errorMessage
            )

            // 8. Save the log
            apiAuditService.logApiCall(log)
            println("API call a $path registrada. Duración: ${duration}ms")
        }

        return result
    }

    /**
     * Checks if the given path is considered sensitive and should not log parameters.
     */
    private fun isSensitivePath(path: String): Boolean {
        return SENSITIVE_PATHS.any { path.startsWith(it) }
    }

    /**
     * Sanitizes error messages for sensitive endpoints to avoid leaking information.
     */
    private fun sanitizeErrorMessage(message: String): String {
        val sanitized = when {
            message.contains("credentials", ignoreCase = true) -> "Authentication failed"
            message.contains("password", ignoreCase = true) -> "Authentication failed"
            message.contains("username", ignoreCase = true) -> "Authentication failed"
            message.contains("not found", ignoreCase = true) -> "Authentication failed"
            message.contains("invalid", ignoreCase = true) -> "Authentication failed"
            message.contains("unauthorized", ignoreCase = true) -> "Authentication failed"
            else -> "Authentication error"
        }
        return sanitized.take(MAX_ERROR_MESSAGE_LENGTH)
    }
}
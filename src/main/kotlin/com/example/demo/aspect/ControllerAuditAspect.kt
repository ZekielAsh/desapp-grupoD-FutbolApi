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
        val params = joinPoint.args.joinToString(separator = ", ", prefix = "[", postfix = "]")
            .take(MAX_PARAMS_LENGTH)

        println("Interceptando API call: $httpMethod $path ($controllerName.$methodName)")

        val startTime = System.currentTimeMillis()
        var wasSuccess = true
        var errorMessage: String? = null
        var result: Any?

        try {
            // 3. Execute the original controller method
            result = joinPoint.proceed()

        } catch (e: Throwable) {
            // 4. Capture error if controller throws exception
            wasSuccess = false
            errorMessage = (e.message ?: e.javaClass.simpleName).take(MAX_ERROR_MESSAGE_LENGTH)
            println("Error en $httpMethod $path: $errorMessage")
            throw e // Re-throw for Spring to handle

        } finally {
            // 5. This block always executes (success or error)
            val duration = System.currentTimeMillis() - startTime

            // 6. Create audit log object
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

            // 7. Save the log
            apiAuditService.logApiCall(log)
            println("API call a $path registrada. Duración: ${duration}ms")
        }

        return result
    }
}
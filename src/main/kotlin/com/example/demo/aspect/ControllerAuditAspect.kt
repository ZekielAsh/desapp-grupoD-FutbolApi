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
    private val apiAuditService: ApiAuditService // Inyectamos el mismo servicio de guardado
) {

    /**
     * Esta es la nueva "regla" de intercepción.
     * Le decimos a Spring: "Ejecuta este código ALREDEDOR (Around)
     * de cualquier método público en cualquier clase anotada con
     * @RestController O @Controller".
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    fun auditControllerMethods(joinPoint: ProceedingJoinPoint): Any? {

        // 1. Obtenemos el request actual para sacar info HTTP
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val httpMethod = request.method
        val path = request.requestURI

        // 2. Obtenemos la info del método del controlador
        val controllerName = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name
        val params = joinPoint.args.joinToString(separator = ", ", prefix = "[", postfix = "]")

        println("Interceptando API call: $httpMethod $path ($controllerName.$methodName)")

        val startTime = System.currentTimeMillis()
        var wasSuccess = true
        var errorMessage: String? = null
        var result: Any?

        try {
            // 3. Ejecutamos el método original del controlador
            result = joinPoint.proceed()

        } catch (e: Throwable) {
            // 4. Si el controlador lanzó un error, lo capturamos
            wasSuccess = false
            errorMessage = e.message
            println("Error en $httpMethod $path: $errorMessage")
            throw e // Volvemos a lanzar el error para que Spring lo maneje

        } finally {
            // 5. Este bloque se ejecuta SIEMPRE (haya éxito o error)
            val duration = System.currentTimeMillis() - startTime

            // 6. Creamos el objeto de log con la nueva info
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

            // 7. Guardamos el log
            apiAuditService.logApiCall(log)
            println("API call a $path registrada. Duración: ${duration}ms")
        }

        return result // Devolvemos el resultado original (ej. el JSON)
    }
}
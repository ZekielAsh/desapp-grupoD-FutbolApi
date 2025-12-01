package com.example.demo.controller

import com.example.demo.model.ApiAuditLog
import com.example.demo.service.ApiAuditService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "API audit log endpoints")
class ApiAuditController(
    private val apiAuditService: ApiAuditService
) {

    @Operation(summary = "Get all audit logs", description = "Retrieve all API audit logs")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Array<ApiAuditLog>::class),
                examples = [ExampleObject(value = """[{"id": 0, "httpMethod": "string", "path": "string", "controllerName": "string", "methodName": "string", "params": "string", "executionTimeMs": 0, "wasSuccess": true, "errorMessage": "string", "timestamp": "string"}]""")]
            )])
    ])
    @GetMapping("/logs")
    fun getAllAuditLogs(): List<ApiAuditLog> {
        return apiAuditService.getAllAuditLogs()
    }
}
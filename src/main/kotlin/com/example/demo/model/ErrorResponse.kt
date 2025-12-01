package com.example.demo.model

data class ErrorResponse(
    val error: String,
    val message: String,
    val status: Int
)


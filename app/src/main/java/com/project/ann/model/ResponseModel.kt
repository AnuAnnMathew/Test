package com.project.ann.model

data class ResponseModel(
    val `data`: Data,
    val message: String,
    val status: Int
)
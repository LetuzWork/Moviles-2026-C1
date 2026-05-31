package com.menusemana.core.common

sealed interface Result<out T> {
    data object Loading : Result<Nothing>
    data class Success<T>(val data: T) : Result<T>
    data class Error(val type: ErrorType, val cause: Throwable? = null) : Result<Nothing>
}

enum class ErrorType { Network, NotFound, Unknown }

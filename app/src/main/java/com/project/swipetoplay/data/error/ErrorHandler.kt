package com.project.swipetoplay.data.error

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

object ErrorHandler {
    fun getUserFriendlyMessage(exception: Throwable): String {
        return when (exception) {
            is SocketTimeoutException -> {
                "The request took too long to respond. Please try again."
            }
            is UnknownHostException -> {
                "Unable to connect to the server. Please check your internet connection."
            }
            is IOException -> {
                "Unable to connect to the server. Please check your internet connection."
            }
            is HttpException -> {
                getUserFriendlyMessage(exception.code(), exception.message())
            }
            else -> {
                "Something went wrong. Please try again."
            }
        }
    }

    fun getUserFriendlyMessage(httpCode: Int, message: String?): String {
        return when (httpCode) {
            401 -> {
                "Your session has expired. Please log in again."
            }
            403 -> {
                "You do not have permission to perform this action."
            }
            404 -> {
                "The requested resource was not found."
            }
            500, 502, 503, 504 -> {
                "A server error occurred. Please try again later."
            }
            else -> {
                message ?: "Something went wrong. Please try again."
            }
        }
    }
}


package com.project.swipetoplay.data.error

import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

object ErrorHandler {
    fun getUserFriendlyMessage(exception: Throwable): String {
        return when (exception) {
            is SocketTimeoutException -> {
                "The request took too long to respond. Please check if the server is running and try again."
            }
            is SocketException -> {
                when {
                    exception.message?.contains("Connection reset", ignoreCase = true) == true -> {
                        "The server closed the connection. Please verify that:\n" +
                        "• The server is running on ${com.project.swipetoplay.BuildConfig.API_BASE_URL}\n" +
                        "• You're on the same network\n" +
                        "• The firewall isn't blocking the connection"
                    }
                    exception.message?.contains("Connection refused", ignoreCase = true) == true -> {
                        "Unable to connect to the server. Please verify that the server is running on ${com.project.swipetoplay.BuildConfig.API_BASE_URL}"
                    }
                    else -> {
                        "Network connection error. Please check your internet connection and try again."
                    }
                }
            }
            is UnknownHostException -> {
                "Unable to connect to the server. Please check your internet connection and verify the server address."
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


package com.project.swipetoplay.domain.model


sealed class AuthResult {
    
    data class Success(val user: GoogleUser) : AuthResult()

    
    data class Error(val message: String) : AuthResult()

    
    data object Cancelled : AuthResult()
}


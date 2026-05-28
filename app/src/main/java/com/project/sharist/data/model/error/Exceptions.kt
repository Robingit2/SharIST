package com.project.sharist.data.model.error

class NotFoundException(message: String = "Not found") : Exception(message)
class AuthException(message: String = "Authentication failed") : Exception(message)

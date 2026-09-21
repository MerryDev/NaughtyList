package net.neruxvace.naughtylist.backend.exception

sealed class ApplicationException(message: String) : RuntimeException(message)

class ResourceNotFoundException(message: String) : ApplicationException(message)
class ResourceConflictException(message: String) : ApplicationException(message)
class InvalidRequestException(message: String) : ApplicationException(message)
class InvalidCredentialsException(message: String) : ApplicationException(message)
package jan.ondra.insights.exception

import org.slf4j.event.Level
import org.slf4j.event.Level.INFO
import org.slf4j.event.Level.WARN
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT

sealed class InsightsException(
    val logLevel: Level,
    val httpStatusCode: HttpStatus,
    val clientInfo: String,
    val serverLog: String,
    cause: Throwable? = null
): RuntimeException(cause)

class InvalidRequestDataException : InsightsException(
    logLevel = INFO,
    httpStatusCode = BAD_REQUEST,
    clientInfo = "Invalid request data",
    serverLog = "Invalid request data",
)

class UserNotRegisteredException : InsightsException(
    logLevel = WARN,
    httpStatusCode = CONFLICT,
    clientInfo = "User is not registered",
    serverLog = "An unregistered user tried to perform an action",
)

class UserAlreadyRegisteredException(cause: DuplicateKeyException): InsightsException(
    logLevel = WARN,
    httpStatusCode = CONFLICT,
    clientInfo = "Registration failed",
    serverLog = "A registered user tried to register again",
    cause = cause
)

class EmailAlreadyExistsException(cause: DuplicateKeyException): InsightsException(
    logLevel = INFO,
    httpStatusCode = CONFLICT,
    clientInfo = "The email already exists",
    serverLog = "The email already exists",
    cause = cause
)

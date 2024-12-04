package jan.ondra.insights.exception

import org.slf4j.event.Level
import org.slf4j.event.Level.INFO
import org.slf4j.event.Level.WARN
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND

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

class UserNotRegisteredException(cause: DataIntegrityViolationException? = null) : InsightsException(
    logLevel = WARN,
    httpStatusCode = NOT_FOUND,
    clientInfo = "User is not registered",
    serverLog = "An unregistered user tried to perform an action",
    cause = cause
)

class UserAlreadyRegisteredException(cause: DuplicateKeyException) : InsightsException(
    logLevel = WARN,
    httpStatusCode = CONFLICT,
    clientInfo = "Registration failed",
    serverLog = "A registered user tried to register again",
    cause = cause
)

class EmailAlreadyExistsException(cause: DuplicateKeyException) : InsightsException(
    logLevel = INFO,
    httpStatusCode = CONFLICT,
    clientInfo = "The email already exists",
    serverLog = "The email already exists",
    cause = cause
)

class SourceNotFoundException(cause: EmptyResultDataAccessException? = null) : InsightsException(
    logLevel = WARN,
    httpStatusCode = NOT_FOUND,
    clientInfo = "Source not found",
    serverLog = "No source with the given ID belongs to the requesting user",
    cause = cause
)

class InsightNotFoundException : InsightsException(
    logLevel = WARN,
    httpStatusCode = NOT_FOUND,
    clientInfo = "Insight not found",
    serverLog = "No insight with the given ID belongs to the requesting user",
)

class SourceDoesNotBelongToUserException : InsightsException(
    logLevel = WARN,
    httpStatusCode = NOT_FOUND,
    clientInfo = "Source not found",
    serverLog = "A user tried to perform an action on a source that doesn't belong to him",
)

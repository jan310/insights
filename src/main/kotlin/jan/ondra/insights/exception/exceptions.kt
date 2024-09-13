package jan.ondra.insights.exception

import org.springframework.dao.DuplicateKeyException

sealed class InsightsException(
    val clientInfo: String,
    val serverLog: String,
    cause: Throwable? = null
): RuntimeException(cause)

class InvalidRequestDataException(
    clientInfo: String,
    serverLog: String
): InsightsException(
    clientInfo = clientInfo,
    serverLog = serverLog
)

class UserNotRegisteredException(
    clientInfo: String,
    serverLog: String,
): InsightsException(
    clientInfo = clientInfo,
    serverLog = serverLog
)

class UserAlreadyRegisteredException(
    clientInfo: String,
    serverLog: String,
    cause: DuplicateKeyException
): InsightsException(
    clientInfo = clientInfo,
    serverLog = serverLog,
    cause = cause
)

class EmailAlreadyExistsException(
    clientInfo: String,
    serverLog: String,
    cause: DuplicateKeyException
): InsightsException(
    clientInfo = clientInfo,
    serverLog = serverLog,
    cause = cause
)

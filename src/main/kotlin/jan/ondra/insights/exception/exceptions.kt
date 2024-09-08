package jan.ondra.insights.exception

sealed class InsightsException(
    val errorCause: String,
    val clientInfo: String
): RuntimeException()

class InvalidRequestDataException(errorCause: String, clientInfo: String): InsightsException(
    errorCause = errorCause,
    clientInfo = clientInfo
)

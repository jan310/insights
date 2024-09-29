package jan.ondra.insights.exception

import jakarta.servlet.http.HttpServletRequest
import jan.ondra.insights.util.getUserIdFromBearerToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun constructLogMessage(
        userId: String,
        requestMethod: String,
        requestPath: String,
        serverLog: String
    ) = "Request failed [userID: '$userId' | endpoint: '$requestMethod $requestPath' | cause: '$serverLog']"

    @ExceptionHandler(InsightsException::class)
    fun handleInsightsException(ex: InsightsException, request: HttpServletRequest): ResponseEntity<String> {
        logger.makeLoggingEventBuilder(ex.logLevel).log(
            constructLogMessage(
                userId = getUserIdFromBearerToken(request.getHeader(AUTHORIZATION)),
                requestMethod = request.method,
                requestPath = request.requestURI,
                serverLog = ex.serverLog
            ),
            ex.cause
        )
        return ResponseEntity(ex.clientInfo, ex.httpStatusCode)
    }
}

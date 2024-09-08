package jan.ondra.insights.exception

import jakarta.servlet.http.HttpServletRequest
import jan.ondra.insights.util.getUserIdFromBearerToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun constructLogMessage(
        errorCause: String,
        userId: String,
        requestMethod: String,
        requestPath: String
    ) = "Request failed: $errorCause [userID: '$userId' | endpoint: '$requestMethod $requestPath']"

    @ExceptionHandler(InvalidRequestDataException::class)
    @ResponseStatus(BAD_REQUEST)
    fun handleInvalidRequestDataException(ex: InvalidRequestDataException, request: HttpServletRequest): String {
        logger.info(constructLogMessage(
            errorCause = ex.errorCause,
            userId = getUserIdFromBearerToken(request.getHeader(AUTHORIZATION)),
            requestMethod = request.method,
            requestPath = request.requestURI
        ))
        return ex.clientInfo
    }

}

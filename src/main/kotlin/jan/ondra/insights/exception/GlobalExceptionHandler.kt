package jan.ondra.insights.exception

import jakarta.servlet.http.HttpServletRequest
import jan.ondra.insights.util.getUserIdFromBearerToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun constructLogMessage(
        userId: String,
        requestMethod: String,
        requestPath: String,
        serverLog: String,
        originalErrorMessage: String? = null
    ) = "Request failed [userID: '$userId' | endpoint: '$requestMethod $requestPath']:" +
            "\n$serverLog${if (originalErrorMessage != null) "\n$originalErrorMessage" else ""}"

    @ExceptionHandler(InvalidRequestDataException::class)
    @ResponseStatus(BAD_REQUEST)
    fun handleInvalidRequestDataException(ex: InvalidRequestDataException, request: HttpServletRequest): String {
        logger.info(constructLogMessage(
            userId = getUserIdFromBearerToken(request.getHeader(AUTHORIZATION)),
            requestMethod = request.method,
            requestPath = request.requestURI,
            serverLog = ex.serverLog
        ))
        return ex.clientInfo
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(CONFLICT)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException, request: HttpServletRequest): String {
        logger.info(constructLogMessage(
            userId = getUserIdFromBearerToken(request.getHeader(AUTHORIZATION)),
            requestMethod = request.method,
            requestPath = request.requestURI,
            serverLog = ex.serverLog,
            originalErrorMessage = ex.cause!!.message
        ))
        return ex.clientInfo
    }

    @ExceptionHandler(UserAlreadyRegisteredException::class, UserNotRegisteredException::class)
    @ResponseStatus(CONFLICT)
    fun handleUserAlreadyRegisteredException(ex: InsightsException, request: HttpServletRequest): String {
        logger.warn(constructLogMessage(
            userId = getUserIdFromBearerToken(request.getHeader(AUTHORIZATION)),
            requestMethod = request.method,
            requestPath = request.requestURI,
            serverLog = ex.serverLog,
            originalErrorMessage = ex.cause?.message
        ))
        return ex.clientInfo
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    fun handleUnknownException(ex: Exception, request: HttpServletRequest): String {
        logger.error(constructLogMessage(
            userId = getUserIdFromBearerToken(request.getHeader(AUTHORIZATION)),
            requestMethod = request.method,
            requestPath = request.requestURI,
            serverLog = "An unknown error occurred"
        ))
        ex.printStackTrace()
        return "An unknown error occurred"
    }

}

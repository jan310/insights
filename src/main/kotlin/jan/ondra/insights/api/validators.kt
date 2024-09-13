package jan.ondra.insights.api

import jan.ondra.insights.exception.InvalidRequestDataException

fun EmailDto.validate() {
    if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$".toRegex())) {
        throw InvalidRequestDataException(
            serverLog = "Provided email has invalid format: $email",
            clientInfo = "Provided email has invalid format: $email"
        )
    }
}

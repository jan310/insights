package jan.ondra.insights.api

import jan.ondra.insights.exception.InvalidRequestDataException

private const val EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x" +
        "08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0" +
        "-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]" +
        "?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x" +
        "21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])"

private const val MAX_SOURCE_NAME_LENGTH = 100
private const val MAX_SOURCE_DESCRIPTION_LENGTH = 300
private const val ISBN_13_LENGTH = 13

private const val MAX_INSIGHT_NOTE_LENGTH = 1000
private const val MAX_INSIGHT_QUOTE_LENGTH = 1000

fun UserDto.validate() {
    if (!email.matches(Regex(EMAIL_REGEX))) {
        throw InvalidRequestDataException()
    }
}

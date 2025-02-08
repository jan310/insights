package jan.ondra.insights.api

import jan.ondra.insights.exception.InvalidRequestDataException
import jan.ondra.insights.api.ValidationConstants.ISBN_13_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_INSIGHT_NOTE_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_INSIGHT_QUOTE_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_SOURCE_DESCRIPTION_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_SOURCE_NAME_LENGTH

fun UserDto.validate() {
    @Suppress("MaxLineLength")
    val emailRegex = Regex(
        """(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\u0001-\u0008\u000b\u000c\u000e-\u001f\u0021\u0023-\u005b\u005d-\u007f]|\\[\u0001-\u0009\u000b\u000c\u000e-\u007f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\u0001-\u0008\u000b\u000c\u000e-\u001f\u0021-\u005a\u0053-\u007f]|\\[\u0001-\u0009\u000b\u000c\u000e-\u007f])+)])"""
    )

    @Suppress("MagicNumber")
    if (!email.matches(emailRegex) || notificationTime < 0 || notificationTime > 23) {
        throw InvalidRequestDataException()
    }
}

fun SourceDto.validate() {
    if (name.length > MAX_SOURCE_NAME_LENGTH ||
        (description?.length ?: 0) > MAX_SOURCE_DESCRIPTION_LENGTH ||
        (isbn13?.length ?: ISBN_13_LENGTH) != ISBN_13_LENGTH
        ) {
        throw InvalidRequestDataException()
    }
}

fun InsightDto.validate() {
    if (note.length > MAX_INSIGHT_NOTE_LENGTH || (quote?.length ?: 0) > MAX_INSIGHT_QUOTE_LENGTH) {
        throw InvalidRequestDataException()
    }
}

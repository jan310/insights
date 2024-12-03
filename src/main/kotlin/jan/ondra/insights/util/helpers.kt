package jan.ondra.insights.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Base64

private val base64Decoder = Base64.getUrlDecoder()
private val objectMapper = ObjectMapper()

fun getUserIdFromBearerToken(bearerToken: String): String {
    val jwtPayload = bearerToken.split(".")[1]
    val decodedJwtPayload = String(base64Decoder.decode(jwtPayload))
    return objectMapper.readTree(decodedJwtPayload).get("sub").asText()
}

package jan.ondra.insights.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Base64

private val objectMapper = ObjectMapper()

fun getUserIdFromBearerToken(bearerToken: String): String {
    val jwtPayload = bearerToken.split(".")[1]
    val decodedJwtPayload = String(Base64.getUrlDecoder().decode(jwtPayload))
    return objectMapper.readTree(decodedJwtPayload).get("sub").asText()
}

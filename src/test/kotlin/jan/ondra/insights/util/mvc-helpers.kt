package jan.ondra.insights.util

import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.http.HttpHeaders.HOST
import org.springframework.http.HttpHeaders.VARY
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.test.web.servlet.ResultActions

fun ResultActions.andDocument(identifier: String): ResultActions {
    return andDo(
        document(
            identifier,
            preprocessRequest(prettyPrint(),  modifyHeaders().remove(HOST).remove(CONTENT_LENGTH)),
            preprocessResponse(prettyPrint(), modifyHeaders().remove(VARY).remove(CONTENT_LENGTH))
        )
    )
}

package jan.ondra.insights.api

import jan.ondra.insights.business.InsightService
import jan.ondra.insights.models.Insight
import jan.ondra.insights.util.getUserIdFromBearerToken
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/insights")
@CrossOrigin("http://localhost:3000")
class InsightController(private val insightService: InsightService) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun createInsight(@RequestHeader(AUTHORIZATION) bearerToken: String, @RequestBody insightDto: InsightDto): IdDto {
        insightDto.validate()
        return insightService.createInsight(
            Insight(
                userId = getUserIdFromBearerToken(bearerToken),
                sourceId = insightDto.sourceId,
                lastModifiedDate = LocalDate.now(),
                filterTags = insightDto.filterTags,
                note = insightDto.note,
                quote = insightDto.quote
            )
        ).let { IdDto(it) }
    }

    @GetMapping
    @ResponseStatus(OK)
    fun getSources(@RequestHeader(AUTHORIZATION) bearerToken: String): List<Insight> {
        return insightService.getInsights(userId = getUserIdFromBearerToken(bearerToken))
    }

    @PutMapping("/{id}")
    @ResponseStatus(ACCEPTED)
    fun updateInsight(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @PathVariable id: Long,
        @RequestBody insightDto: InsightDto
    ) {
        insightDto.validate()
        insightService.updateInsight(
            Insight(
                id = id,
                userId = getUserIdFromBearerToken(bearerToken),
                sourceId = insightDto.sourceId,
                lastModifiedDate = LocalDate.now(),
                filterTags = insightDto.filterTags,
                note = insightDto.note,
                quote = insightDto.quote
            )
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun deleteInsight(@RequestHeader(AUTHORIZATION) bearerToken: String, @PathVariable id: Long) {
        insightService.deleteInsight(id = id, userId = getUserIdFromBearerToken(bearerToken))
    }

}

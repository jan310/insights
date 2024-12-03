package jan.ondra.insights.api

import jan.ondra.insights.business.SourceService
import jan.ondra.insights.models.Source
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

@RestController
@RequestMapping("/api/v1/sources")
@CrossOrigin("http://localhost:3000")
class SourceController(private val sourceService: SourceService) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun createSource(@RequestHeader(AUTHORIZATION) bearerToken: String, @RequestBody sourceDto: SourceDto): IdDto {
        sourceDto.validate()
        return sourceService.createSource(
            Source(
                userId = getUserIdFromBearerToken(bearerToken),
                name = sourceDto.name,
                description = sourceDto.description,
                isbn13 = sourceDto.isbn13
            )
        ).let { IdDto(it) }
    }

    @GetMapping
    @ResponseStatus(OK)
    fun getSources(@RequestHeader(AUTHORIZATION) bearerToken: String): List<Source> {
        return sourceService.getSources(userId = getUserIdFromBearerToken(bearerToken))
    }

    @PutMapping("/{id}")
    @ResponseStatus(ACCEPTED)
    fun updateSource(
        @RequestHeader(AUTHORIZATION) bearerToken: String,
        @PathVariable id: Long,
        @RequestBody sourceDto: SourceDto
    ) {
        sourceDto.validate()
        sourceService.updateSource(
            Source(
                id = id,
                userId = getUserIdFromBearerToken(bearerToken),
                name = sourceDto.name,
                description = sourceDto.description,
                isbn13 = sourceDto.isbn13
            )
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun deleteSource(@RequestHeader(AUTHORIZATION) bearerToken: String, @PathVariable id: Long) {
        sourceService.deleteSource(id = id, userId = getUserIdFromBearerToken(bearerToken))
    }

}

package jan.ondra.insights.business

import jan.ondra.insights.models.Source
import jan.ondra.insights.persistence.SourceRepository
import org.springframework.stereotype.Service

@Service
class SourceService(private val sourceRepository: SourceRepository) {

    fun createSource(source: Source): Long {
        return sourceRepository.createSource(source)
    }

    fun getSources(userId: String): List<Source> {
        return sourceRepository.getSources(userId)
    }

    fun updateSource(source: Source) {
        sourceRepository.updateSource(source)
    }

    fun deleteSource(id: Long, userId: String) {
        sourceRepository.deleteSource(id, userId)
    }

}

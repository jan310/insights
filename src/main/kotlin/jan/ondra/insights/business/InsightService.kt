package jan.ondra.insights.business

import jan.ondra.insights.models.Insight
import jan.ondra.insights.persistence.InsightRepository
import org.springframework.stereotype.Service

@Service
class InsightService(private val insightRepository: InsightRepository) {

    fun createInsight(insight: Insight):Long {
        return insightRepository.createInsight(insight)
    }

    fun getInsights(userId: String): List<Insight> {
        return insightRepository.getInsights(userId)
    }

    fun updateInsight(insight: Insight) {
        insightRepository.updateInsight(insight)
    }

    fun deleteInsight(id: Long, userId: String) {
        insightRepository.deleteInsight(id, userId)
    }

}
